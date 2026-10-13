package aeind.cross_thread;

import aeind.compat.MIParallelHatchCompat;
import aeind.isolation.RoomInputStorage;
import aeind.isolation.ThreadIsolationAccess;
import aeind.isolation.ThreadIsolationRoom;
import appeng.api.config.Actionable;
import appeng.api.stacks.AEKey;
import aztech.modern_industrialization.machines.MachineBlockEntity;
import aztech.modern_industrialization.machines.components.CrafterComponent;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.slf4j.Logger;

/** Runs Modern Mekanistics chemical crafters against per-room ME input views. */
public final class ModernMekChemicalThreadManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG = "aeindModernMekChemicalThreads";
    private static final String CHEMICAL_KEY = "me.ramidzkh.mekae2.ae2.MekanismKey";
    private static final String CHEMICAL_CRAFTER = "icu.kudikan.modern_mekanistics.machine.component.ChemicalCrafterComponent";
    private static final String CHEMICAL_INVENTORY = "icu.kudikan.modern_mekanistics.machine.component.AbstractChemicalCrafterComponent$Inventory";
    private static final String CHEMICAL_STACK = "icu.kudikan.modern_mekanistics.inventory.ConfigurableChemicalStack";
    private static final String CHEMICAL_VARIANT = "icu.kudikan.modern_mekanistics.thirdparty.fabrictransfer.api.chemical.ChemicalVariant";
    private static final String MODULAR_BEHAVIOR = "net.swedz.tesseract.neoforge.compat.mi.component.craft.ModularCrafterAccessBehavior";

    private final Map<String, ThreadState> states = new LinkedHashMap<>();
    private ListTag pendingState;
    private HolderLookup.Provider pendingLookup;
    private long lastEuPerTick;

    public long lastEuPerTick() {
        return this.lastEuPerTick;
    }

    public boolean hasWork() {
        return this.states.values().stream().anyMatch(state -> isActive(state.crafter))
                || this.pendingState != null && !this.pendingState.isEmpty();
    }

    public int activeThreadCount() {
        return (int) this.states.values().stream().filter(state -> isActive(state.crafter)).count();
    }

    public List<CrossThreadRecipeManager.ThreadProgress> progress() {
        return this.states.values().stream()
                .filter(state -> isActive(state.crafter))
                .map(state -> new CrossThreadRecipeManager.ThreadProgress(
                        floatValue(invokeQuietly(state.crafter, "getProgress")),
                        Math.max(1, intValue(invokeQuietly(state.crafter, "getRecipeMultiplier"))),
                        false))
                .toList();
    }

    public boolean tick(MachineBlockEntity machine, Object nativeCrafter, boolean crossThread) {
        Object sourceInventory = invoke(machine, "getMultiblockInventoryComponent");
        if (!(sourceInventory instanceof ThreadIsolationAccess access)) {
            return booleanValue(invoke(nativeCrafter, "tickRecipe"));
        }
        List<ThreadIsolationRoom> rooms = access.aeind$isolationRooms();
        boolean hasMapRooms = rooms.stream().anyMatch(ThreadIsolationRoom::hasMapStorage);
        if (!access.aeind$crossThreadEnabled() && !hasMapRooms && !this.hasWork()) {
            return booleanValue(invoke(nativeCrafter, "tickRecipe"));
        }
        if (!(sourceInventory instanceof CrafterComponent.Inventory outputInventory)) {
            return booleanValue(invoke(nativeCrafter, "tickRecipe"));
        }
        Map<String, ThreadIsolationRoom> byId = new LinkedHashMap<>();
        for (ThreadIsolationRoom room : rooms) byId.putIfAbsent(room.id(), room);
        for (ThreadIsolationRoom room : byId.values()) this.states.computeIfAbsent(room.id(), ignored -> createState(machine, nativeCrafter, room, outputInventory, access));
        this.restorePending(machine, nativeCrafter, outputInventory, access, this.pendingLookup);

        this.lastEuPerTick = 0L;
        boolean anyActive = false;
        boolean didWork = false;
        for (ThreadIsolationRoom room : byId.values()) {
            ThreadState state = this.states.get(room.id());
            if (state == null) continue;
            state.inputView.refresh(room);
            boolean ticked = booleanValue(invoke(state.crafter, "tickRecipe"));
            didWork |= state.inputView.commit(room);
            anyActive |= isActive(state.crafter);
            if (ticked) {
                this.lastEuPerTick = saturatedAdd(this.lastEuPerTick,
                        Math.max(0L, longValue(invokeQuietly(state.crafter, "getCurrentRecipeEu"))));
                didWork = true;
            }
            if (!crossThread && (anyActive || ticked)) break;
        }

        this.states.entrySet().removeIf(entry -> !byId.containsKey(entry.getKey()) && !isActive(entry.getValue().crafter));
        if (didWork) machine.setChanged();
        return anyActive || this.lastEuPerTick > 0L;
    }

    public static boolean resumeNative(Object crafter) {
        return booleanValue(invoke(crafter, "tryContinueRecipe"));
    }

    public void writeNbt(CompoundTag tag, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        if (this.pendingState != null) {
            for (int i = 0; i < this.pendingState.size(); i++) list.add(this.pendingState.getCompound(i).copy());
        }
        for (Map.Entry<String, ThreadState> entry : this.states.entrySet()) {
            if (!isActive(entry.getValue().crafter)) continue;
            CompoundTag state = new CompoundTag();
            state.putString("room", entry.getKey());
            CompoundTag crafterTag = new CompoundTag();
            invoke(entry.getValue().crafter, "writeNbt", new Class<?>[]{CompoundTag.class, HolderLookup.Provider.class}, crafterTag, lookup);
            state.put("crafter", crafterTag);
            list.add(state);
        }
        if (!list.isEmpty()) tag.put(TAG, list);
    }

    public void readNbt(CompoundTag tag, MachineBlockEntity machine, Object nativeCrafter, HolderLookup.Provider lookup) {
        if (!tag.contains(TAG, 9)) return;
        this.pendingState = tag.getList(TAG, 10).copy();
        this.pendingLookup = lookup;
        Object inventory = invoke(machine, "getMultiblockInventoryComponent");
        if (inventory instanceof ThreadIsolationAccess access) {
            this.restorePending(machine, nativeCrafter, (CrafterComponent.Inventory) inventory, access, lookup);
        }
    }

    private void restorePending(MachineBlockEntity machine, Object nativeCrafter, CrafterComponent.Inventory outputs,
                                ThreadIsolationAccess access) {
        this.restorePending(machine, nativeCrafter, outputs, access, null);
    }

    private void restorePending(MachineBlockEntity machine, Object nativeCrafter, CrafterComponent.Inventory outputs,
                                ThreadIsolationAccess access, HolderLookup.Provider lookup) {
        if (this.pendingState == null || this.pendingState.isEmpty() || lookup == null) return;
        Map<String, ThreadIsolationRoom> rooms = new LinkedHashMap<>();
        for (ThreadIsolationRoom room : access.aeind$isolationRooms()) rooms.putIfAbsent(room.id(), room);
        ListTag remaining = new ListTag();
        for (int i = 0; i < this.pendingState.size(); i++) {
            CompoundTag saved = this.pendingState.getCompound(i);
            String roomId = saved.getString("room");
            ThreadIsolationRoom room = rooms.get(roomId);
            if (room == null) {
                remaining.add(saved.copy());
                continue;
            }
            ThreadState state = this.states.computeIfAbsent(roomId, ignored -> createState(machine, nativeCrafter, room, outputs, access));
            if (state == null || lookup == null) {
                remaining.add(saved.copy());
                continue;
            }
            invoke(state.crafter, "readNbt", new Class<?>[]{CompoundTag.class, HolderLookup.Provider.class, boolean.class}, saved.getCompound("crafter"), lookup, false);
        }
        this.pendingState = remaining.isEmpty() ? null : remaining;
        if (this.pendingState == null) this.pendingLookup = null;
    }

    private static ThreadState createState(MachineBlockEntity machine, Object sourceCrafter, ThreadIsolationRoom room,
                                           CrafterComponent.Inventory outputs, ThreadIsolationAccess access) {
        try {
            ClassLoader loader = sourceCrafter.getClass().getClassLoader();
            Class<?> inventoryType = Class.forName(CHEMICAL_INVENTORY, true, loader);
            Class<?> behaviorType = Class.forName(MODULAR_BEHAVIOR, true, loader);
            Object behavior = invoke(sourceCrafter, "getBehavior");
            ThreadInputView inputView = new ThreadInputView(loader);
            InvocationHandler handler = (proxy, method, args) -> switch (method.getName()) {
                case "getItemInputs" -> inputView.itemInputs(room);
                case "getFluidInputs" -> inputView.fluidInputs(room);
                case "getChemicalInputs" -> inputView.chemicalInputs(room);
                case "getItemOutputs" -> outputs.getItemOutputs();
                case "getFluidOutputs" -> outputs.getFluidOutputs();
                case "getChemicalOutputs" -> invoke(outputs, "getChemicalOutputs");
                case "hash" -> inputView.hash(room);
                case "toString" -> "AppliedIndustrializationChemicalThread[" + room.id() + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == (args == null ? null : args[0]);
                default -> invoke(outputs, method.getName(), method.getParameterTypes(), args == null ? new Object[0] : args);
            };
            Object threadInventory = Proxy.newProxyInstance(loader, new Class<?>[]{inventoryType}, handler);
            Supplier<Object> recipeTypeSupplier = () -> invoke(sourceCrafter, "getRecipeType");
            Supplier<Integer> multiplierSupplier = () -> {
                int nativeLimit = Math.max(1, intValue(invokeQuietly(sourceCrafter, "getMaxMultiplier")));
                if (!access.aeind$crossThreadEnabled()) return nativeLimit;
                return Math.max(Math.max(1, access.aeind$maxParallelPerThread()), MIParallelHatchCompat.getParallelLimit(machine));
            };
            Supplier<Object> transformerSupplier = () -> invoke(machine, "getEuCostTransformer");
            Class<?> crafterType = Class.forName(CHEMICAL_CRAFTER, true, loader);
            Constructor<?> constructor = crafterType.getConstructor(
                    MachineBlockEntity.class, inventoryType, behaviorType, Supplier.class, Supplier.class, Supplier.class);
            Object crafter = constructor.newInstance(machine, threadInventory, behavior, recipeTypeSupplier, multiplierSupplier, transformerSupplier);
            return new ThreadState(crafter, inputView);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            LOGGER.error("Unable to create Modern Mekanistics cross-thread recipe state for room {}", room.id(), exception);
            return null;
        }
    }

    private static boolean isActive(Object crafter) {
        return crafter != null && booleanValue(invokeQuietly(crafter, "hasActiveRecipe"));
    }

    private static Object invoke(Object target, String name) {
        return invoke(target, name, new Class<?>[0]);
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method method = target.getClass().getMethod(name, parameterTypes);
            return method.invoke(target, args);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Modern Mekanistics compatibility call failed: " + name, exception);
        }
    }

    private static Object invokeQuietly(Object target, String name) {
        try {
            return invoke(target, name);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private static float floatValue(Object value) {
        return value instanceof Number number ? number.floatValue() : 0F;
    }

    private static boolean booleanValue(Object value) {
        return value instanceof Boolean bool && bool;
    }

    private static long saturatedAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    private record ThreadState(Object crafter, ThreadInputView inputView) {
    }

    private static final class ThreadInputView {
        private final ClassLoader loader;
        private RoomInputStorage.MiInputView miView;
        private List<Object> chemicalInputs = List.of();
        private final IdentityHashMap<Object, AEKey> chemicalSources = new IdentityHashMap<>();
        private final Map<Object, Long> chemicalStartingAmounts = new IdentityHashMap<>();

        private ThreadInputView(ClassLoader loader) {
            this.loader = loader;
        }

        private void refresh(ThreadIsolationRoom room) {
            this.chemicalSources.clear();
            this.chemicalStartingAmounts.clear();
            this.chemicalInputs = room.hasMapStorage() ? this.createChemicalInputs(room.inputStorage()) : List.of();
            this.miView = room.hasMapStorage() ? room.inputStorage().createMiView() : null;
        }

        private List<?> itemInputs(ThreadIsolationRoom room) {
            return room.hasMapStorage() ? this.miView.itemInputs() : room.itemInputs();
        }

        private List<?> fluidInputs(ThreadIsolationRoom room) {
            return room.hasMapStorage() ? this.miView.fluidInputs() : room.fluidInputs();
        }

        private List<?> chemicalInputs(ThreadIsolationRoom room) {
            return room.hasMapStorage() ? this.chemicalInputs : List.of();
        }

        private int hash(ThreadIsolationRoom room) {
            return room.hasMapStorage() ? Long.hashCode(room.inputStorage().revision()) : room.hashCode();
        }

        private List<Object> createChemicalInputs(RoomInputStorage storage) {
            ArrayList<Object> stacks = new ArrayList<>();
            try {
                Class<?> keyClass = Class.forName(CHEMICAL_KEY, false, this.loader);
                Class<?> stackClass = Class.forName(CHEMICAL_STACK, true, this.loader);
                Class<?> variantClass = Class.forName(CHEMICAL_VARIANT, true, this.loader);
                Constructor<?> stackConstructor = stackClass.getConstructor(long.class);
                Method setKey = stackClass.getMethod("setKey", Class.forName("aztech.modern_industrialization.thirdparty.fabrictransfer.api.storage.TransferVariant", true, this.loader));
                Method setAmount = stackClass.getMethod("setAmount", long.class);
                Method getStack = keyClass.getMethod("getStack");
                Method getChemical = Class.forName("mekanism.api.chemical.ChemicalStack", true, this.loader).getMethod("getChemical");
                Method ofChemical = variantClass.getMethod("of", Class.forName("mekanism.api.chemical.Chemical", true, this.loader));
                for (Map.Entry<AEKey, Long> entry : storage.snapshot().entrySet()) {
                    if (entry.getValue() <= 0 || !keyClass.isInstance(entry.getKey())) continue;
                    Object chemicalStack = getStack.invoke(entry.getKey());
                    Object chemical = getChemical.invoke(chemicalStack);
                    Object variant = ofChemical.invoke(null, chemical);
                    Object configurable = stackConstructor.newInstance(entry.getValue());
                    setKey.invoke(configurable, variant);
                    setAmount.invoke(configurable, entry.getValue());
                    stacks.add(configurable);
                    this.chemicalSources.put(configurable, entry.getKey());
                    this.chemicalStartingAmounts.put(configurable, entry.getValue());
                }
            } catch (ReflectiveOperationException | LinkageError exception) {
                LOGGER.error("Unable to create a Modern Mekanistics chemical input view", exception);
            }
            return stacks;
        }

        private boolean commit(ThreadIsolationRoom room) {
            if (!room.hasMapStorage()) return false;
            Map<AEKey, Long> consumed = this.miView.consumedAmounts();
            try {
                Method getAmount = Class.forName(CHEMICAL_STACK, true, this.loader).getMethod("getAmount");
                for (Object configurable : this.chemicalInputs) {
                    long before = this.chemicalStartingAmounts.getOrDefault(configurable, 0L);
                    long after = longValue(getAmount.invoke(configurable));
                    long used = Math.max(0L, before - after);
                    AEKey key = this.chemicalSources.get(configurable);
                    if (key != null && used > 0L) consumed.merge(key, used, ModernMekChemicalThreadManager::saturatedAdd);
                }
            } catch (ReflectiveOperationException | LinkageError exception) {
                LOGGER.error("Unable to read consumed Modern Mekanistics chemical inputs", exception);
                return false;
            }
            if (consumed.isEmpty()) return false;
            RoomInputStorage storage = room.inputStorage();
            if (!storage.extractAll(consumed, Actionable.SIMULATE) || !storage.extractAll(consumed, Actionable.MODULATE)) {
                LOGGER.error("Modern Mekanistics cross-thread input changed during recipe start in room {}", room.id());
                return false;
            }
            return true;
        }
    }
}
