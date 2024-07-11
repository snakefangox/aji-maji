package xyz.fancyteam.ajimaji.client;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

import xyz.fancyteam.ajimaji.client.armor_renderer.AMArmorRenderers;
import xyz.fancyteam.ajimaji.client.entity_renderer.AMEntityRenderers;
import xyz.fancyteam.ajimaji.item.AMItems;
import xyz.fancyteam.ajimaji.net.AMNet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AjiMajiClient implements ClientModInitializer {
    public static int rotationTicks = 0;

    @Override
    public void onInitializeClient() {
        AMEntityRenderers.registerEntityRenderers();
        AMEntityRenderers.registerModelLayers();
        AMArmorRenderers.register();
        AMClientBlocks.register();
        ClientTickEvents.START_CLIENT_TICK.register(world -> rotationTicks++);

        UseItemCallback.EVENT.register((player, world, hand) -> {
            // allow the top hat to be used in adventure mode, but only to retrieve an entity
            // This sends a custom packet to the server to request use of the top hat.
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isOf(AMItems.TOP_HAT)) {
                HitResult crosshairTarget = MinecraftClient.getInstance().crosshairTarget;
                if (crosshairTarget instanceof BlockHitResult hit) {
                    AMNet.useTopHat(hit, hand);
                    return TypedActionResult.consume(stack);
                }
            }

            return TypedActionResult.pass(stack);
        });
    }
}
