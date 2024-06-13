package xyz.fancyteam.ajimaji.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import xyz.fancyteam.ajimaji.block.AMBlocks;
import xyz.fancyteam.ajimaji.misc.AMArmorMaterials;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMItems {
    public static final List<ItemStack> CREATIVE_TAB_ITEMS = new ArrayList<>();

    public static final TopHatBlockItem TOP_HAT =
        new TopHatBlockItem(AMBlocks.TOP_HAT, AMArmorMaterials.MAGICIANS_ARMOR_MATERIAL,
            new Item.Settings().maxCount(1));
    public static final MagicCarpetItem MAGIC_CARPET =
        new MagicCarpetItem(new Item.Settings().maxCount(1));
    public static final CardDeckItem CARD_DECK =
        new CardDeckItem(new Item.Settings());

    public static void register() {
        register("top_hat", TOP_HAT);
        register("magic_carpet", MAGIC_CARPET);
        register("card_deck", CARD_DECK);
    }

    public static void register(String path, Item item) {
        Registry.register(Registries.ITEM, id(path), item);
        CREATIVE_TAB_ITEMS.add(item.getDefaultStack());
    }
}
