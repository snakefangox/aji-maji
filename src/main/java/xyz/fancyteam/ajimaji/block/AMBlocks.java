package xyz.fancyteam.ajimaji.block;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMBlocks {
    public static final List<ItemStack> CREATIVE_TAB_ITEMS = new ArrayList<>();

    public static final TopHatBlock TOP_HAT = new TopHatBlock(
        AbstractBlock.Settings.create().mapColor(MapColor.BLACK).nonOpaque().instrument(NoteBlockInstrument.HAT)
            .sounds(BlockSoundGroup.WOOL));
    public static final CardBoxBlock CARD_BOX =
        new CardBoxBlock(AbstractBlock.Settings.create().mapColor(MapColor.RED).nonOpaque());
    public static final Block CLOTH_FOLDS = new Block(
        AbstractBlock.Settings.create().mapColor(MapColor.BLACK).instrument(NoteBlockInstrument.BASEDRUM)
            .sounds(BlockSoundGroup.WOOL));

    public static void register() {
        register0("top_hat", TOP_HAT);
        register1("card_box", CARD_BOX);
        register1("cloth_folds", CLOTH_FOLDS);

        registerCodec("top_hat", TopHatBlock.MAP_CODEC);
        registerCodec("card_box", CardBoxBlock.MAP_CODEC);
    }

    private static void register0(String path, Block block) {
        Registry.register(Registries.BLOCK, id(path), block);
    }

    private static void register1(String path, Block block) {
        Registry.register(Registries.BLOCK, id(path), block);
        Item blockItem = new BlockItem(block, new Item.Settings());
        Registry.register(Registries.ITEM, id(path), blockItem);
        CREATIVE_TAB_ITEMS.add(new ItemStack(blockItem));
    }

    private static void registerCodec(String path, MapCodec<? extends Block> codec) {
        Registry.register(Registries.BLOCK_TYPE, id(path), codec);
    }
}
