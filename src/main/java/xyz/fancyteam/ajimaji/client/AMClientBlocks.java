package xyz.fancyteam.ajimaji.client;

import xyz.fancyteam.ajimaji.block.AMBlocks;

import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;

import net.minecraft.client.render.RenderLayer;

public class AMClientBlocks {
    public static void register() {
        BlockRenderLayerMap.INSTANCE.putBlock(AMBlocks.TOP_HAT, RenderLayer.getCutout());
    }
}
