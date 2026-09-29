/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  appeng.api.upgrades.Upgrades
 *  appeng.core.definitions.AEItems
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.world.level.ItemLike
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.ModList
 *  net.neoforged.fml.common.Mod
 *  net.neoforged.neoforge.registries.RegisterEvent
 */
package aeind;

import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import aeind.block.ModBlocks;
import aeind.blockentity.ModBlockEntities;
import aeind.gui.ModMenuTypes;
import aeind.item.ModCreativeTabs;
import aeind.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(value="aeind")
public class MePatternInputHatchMod {
    public static final String MODID = "aeind";
    private static boolean upgradesRegistered = false;

    public MePatternInputHatchMod(IEventBus iEventBus, ModContainer modContainer) {
        ModBlocks.register(iEventBus);
        ModItems.ITEMS.register(iEventBus);
        ModBlockEntities.register(iEventBus);
        ModMenuTypes.register(iEventBus);
        ModCreativeTabs.register(iEventBus);
        iEventBus.addListener(MePatternInputHatchMod::registerUpgrades);
    }

    private static void registerUpgrades(RegisterEvent registerEvent) {
        if (upgradesRegistered || !registerEvent.getRegistryKey().equals(Registries.ITEM)) {
            return;
        }
        upgradesRegistered = true;
        Upgrades.add((ItemLike)AEItems.REDSTONE_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()), (int)1);
        Upgrades.add((ItemLike)AEItems.CAPACITY_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()), (int)2);
        Upgrades.add((ItemLike)AEItems.CRAFTING_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()), (int)1);
        Upgrades.add((ItemLike)AEItems.REDSTONE_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()), (int)1);
        Upgrades.add((ItemLike)AEItems.CAPACITY_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()), (int)2);
        Upgrades.add((ItemLike)AEItems.CRAFTING_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_PATTERN_INPUT_HATCH.get()), (int)1);
        if (ModList.get().isLoaded("extendedae") && ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH != null) {
            Upgrades.add((ItemLike)AEItems.REDSTONE_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()), (int)1);
            Upgrades.add((ItemLike)AEItems.CAPACITY_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()), (int)2);
            Upgrades.add((ItemLike)AEItems.CRAFTING_CARD, (ItemLike)((ItemLike)ModBlocks.ADVANCED_EXTENDED_PATTERN_INPUT_HATCH.get()), (int)1);
        }
    }
}

