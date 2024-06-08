package xyz.fancyteam.ajimaji.client;

import net.fabricmc.api.ClientModInitializer;

import xyz.fancyteam.ajimaji.client.armor_renderer.AMArmorRenderers;
import xyz.fancyteam.ajimaji.client.entity_renderer.AMEntityRenderers;

public class AjiMajiClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AMEntityRenderers.registerEntityRenderers();
        AMEntityRenderers.registerModelLayers();
        AMArmorRenderers.register();
        AMClientBlocks.register();
    }
}
