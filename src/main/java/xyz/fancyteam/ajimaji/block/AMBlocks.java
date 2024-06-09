package xyz.fancyteam.ajimaji.block;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMBlocks {
    public static final TopHatBlock TOP_HAT = new TopHatBlock(
        AbstractBlock.Settings.create().mapColor(MapColor.BLACK).nonOpaque().instrument(NoteBlockInstrument.HAT)
            .sounds(BlockSoundGroup.WOOL));

    public static void register() {
        register("top_hat", TOP_HAT, TopHatBlock.MAP_CODEC);
    }

    private static void register(String path, Block block, MapCodec<? extends TopHatBlock> codec) {
        Registry.register(Registries.BLOCK, id(path), block);
        Registry.register(Registries.BLOCK_TYPE, id(path), codec);
    }
}
