package xyz.fancyteam.ajimaji.misc;

import xyz.fancyteam.ajimaji.item.AMItems;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static xyz.fancyteam.ajimaji.AjiMaji.id;
import static xyz.fancyteam.ajimaji.AjiMaji.tt;

public class AMCreativeTabs {
    public static final ItemGroup MAIN =
        FabricItemGroup.builder().displayName(tt("itemGroup", "main")).entries((displayContext, entries) -> {
            entries.addAll(AMItems.CREATIVE_TAB_ITEMS);
        }).build();

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, id("main"), MAIN);
    }
}
