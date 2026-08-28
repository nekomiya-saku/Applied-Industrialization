package cn.autoforged.me_pattern_input_hatch_mod_1786194568;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.block.ModBlocks;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.blockentity.ModBlockEntities;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.gui.ModMenuTypes;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.item.ModCreativeTabs;
import cn.autoforged.me_pattern_input_hatch_mod_1786194568.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MePatternInputHatchMod.MODID)
public class MePatternInputHatchMod {
    public static final String MODID = "aeind";
    private static boolean upgradesRegistered = false;

    public MePatternInputHatchMod(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        modEventBus.addListener(MePatternInputHatchMod::registerUpgrades);
    }

    private static void registerUpgrades(RegisterEvent event) {
        if (upgradesRegistered || !event.getRegistryKey().equals(Registries.ITEM)) {
            return;
        }
        upgradesRegistered = true;
        Upgrades.add((ItemLike) AEItems.REDSTONE_CARD, (ItemLike) ModBlocks.ME_PATTERN_INPUT_HATCH.get(), 1);
        Upgrades.add((ItemLike) AEItems.CAPACITY_CARD, (ItemLike) ModBlocks.ME_PATTERN_INPUT_HATCH.get(), 2);
        Upgrades.add((ItemLike) AEItems.CRAFTING_CARD, (ItemLike) ModBlocks.ME_PATTERN_INPUT_HATCH.get(), 1);
        if (ModList.get().isLoaded("extendedae") && ModBlocks.EXTENDED_PATTERN_INPUT_HATCH != null) {
            Upgrades.add((ItemLike) AEItems.REDSTONE_CARD, (ItemLike) ModBlocks.EXTENDED_PATTERN_INPUT_HATCH.get(), 1);
            Upgrades.add((ItemLike) AEItems.CAPACITY_CARD, (ItemLike) ModBlocks.EXTENDED_PATTERN_INPUT_HATCH.get(), 2);
            Upgrades.add((ItemLike) AEItems.CRAFTING_CARD, (ItemLike) ModBlocks.EXTENDED_PATTERN_INPUT_HATCH.get(), 1);
        }
    }
}
