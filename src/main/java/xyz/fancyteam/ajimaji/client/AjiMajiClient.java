package xyz.fancyteam.ajimaji.client;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import xyz.fancyteam.ajimaji.client.armor_renderer.AMArmorRenderers;
import xyz.fancyteam.ajimaji.client.entity_renderer.AMEntityRenderers;

public class AjiMajiClient implements ClientModInitializer {
    public static int rotationTicks = 0;

    @Override
    public void onInitializeClient() {
        AMEntityRenderers.registerEntityRenderers();
        AMEntityRenderers.registerModelLayers();
        AMArmorRenderers.register();
        AMClientBlocks.register();
        ClientTickEvents.START_CLIENT_TICK.register(world -> rotationTicks++);
    }
}
