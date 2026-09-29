package aeind.blockentity;

import aztech.modern_industrialization.inventory.ConfigurableItemStack;
import aztech.modern_industrialization.inventory.MIInventory;
import aztech.modern_industrialization.inventory.SlotPositions;
import aztech.modern_industrialization.inventory.SlotPositions.Builder;
import aztech.modern_industrialization.machines.BEP;
import aztech.modern_industrialization.machines.components.OrientationComponent.Params;
import aztech.modern_industrialization.machines.models.MachineModelClientData;
import aztech.modern_industrialization.machines.multiblocks.HatchBlockEntity;
import aztech.modern_industrialization.machines.multiblocks.HatchType;
import aztech.modern_industrialization.machines.multiblocks.HatchTypes;
import aeind.isolation.ThreadIsolationHatch;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ThreadWarehouseBlockEntity extends HatchBlockEntity implements ThreadIsolationHatch {
   private final MIInventory inventory = new MIInventory(
      List.of(ConfigurableItemStack.standardInputSlot()), List.of(), new Builder().addSlot(0, 0).build(), SlotPositions.empty()
   );

   public ThreadWarehouseBlockEntity(BlockPos var1, BlockState var2) {
      this(var1, var2, ModBlockEntities.THREAD_WAREHOUSE.get(), "thread_warehouse");
   }

   protected ThreadWarehouseBlockEntity(BlockPos var1, BlockState var2, BlockEntityType<?> var3, String var4) {
      super(
         new BEP(var3, var1, var2),
         new aztech.modern_industrialization.machines.gui.MachineGuiParameters.Builder(ResourceLocation.fromNamespaceAndPath("aeind", var4), false).build(),
         Params.noFacing(true, false)
      );
      this.registerComponents(this.inventory);
   }

   @Override
   public HatchType getHatchType() {
      return HatchTypes.ITEM_INPUT;
   }

   @Override
   public boolean upgradesToSteel() {
      return false;
   }

   @Override
   public MIInventory getInventory() {
      return this.inventory;
   }

   @Override
   public void appendItemInputs(List<ConfigurableItemStack> var1) {
      var1.addAll(this.inventory.getItemStacks());
   }

   @Override
   public MachineModelClientData getMachineModelData() {
      return new MachineModelClientData();
   }
}
