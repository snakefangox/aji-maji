package xyz.fancyteam.ajimaji.item;

import java.util.ArrayList;
import java.util.List;

import xyz.fancyteam.ajimaji.block.AMBlocks;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMItems {
    public static final List<ItemStack> CREATIVE_TAB_ITEMS = new ArrayList<>();

    public static final TopHatBlockItem TOP_HAT =
        new TopHatBlockItem(AMBlocks.TOP_HAT, new Item.Settings().maxCount(1));
    public static final MagicCarpetItem MAGIC_CARPET =
            new MagicCarpetItem(new Item.Settings().maxCount(1));

    public static void register() {
        register("top_hat", TOP_HAT);
        register("magic_carpet", MAGIC_CARPET);
    }

    public static void register(String path, Item item) {
        Registry.register(Registries.ITEM, id(path), item);
        CREATIVE_TAB_ITEMS.add(item.getDefaultStack());
    }
}
