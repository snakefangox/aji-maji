package xyz.fancyteam.ajimaji.misc;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import xyz.fancyteam.ajimaji.block.AMBlocks;
import xyz.fancyteam.ajimaji.item.AMItems;

import static xyz.fancyteam.ajimaji.AjiMaji.id;
import static xyz.fancyteam.ajimaji.AjiMaji.tt;

public class AMCreativeTabs {
    public static final ItemGroup MAIN =
        FabricItemGroup.builder().displayName(tt("itemGroup", "main")).icon(() -> new ItemStack(AMItems.TOP_HAT))
            .entries((displayContext, entries) -> {
                entries.addAll(AMBlocks.CREATIVE_TAB_ITEMS);
                entries.addAll(AMItems.CREATIVE_TAB_ITEMS);
            }).build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, id("main"), MAIN);
    }
}
