package xyz.fancyteam.ajimaji.client;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

import net.minecraft.client.render.RenderLayer;

import xyz.fancyteam.ajimaji.block.AMBlocks;

public class AMClientBlocks {
    public static void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(AMBlocks.TOP_HAT, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(AMBlocks.CARD_BOX, RenderLayer.getCutout());
    }
}
