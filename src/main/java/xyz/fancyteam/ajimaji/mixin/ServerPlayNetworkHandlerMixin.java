package xyz.fancyteam.ajimaji.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.PlayerAssociatedNetworkHandler;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xyz.fancyteam.ajimaji.entity.MagicCarpetEntity;

import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler
    implements ServerPlayPacketListener, PlayerAssociatedNetworkHandler, TickablePacketListener {

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection,
                                         ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Inject(method = "getMaxAllowedFloatingTicks", at = @At("HEAD"), cancellable = true)
    private void getMaxAllowedFloatingTicks(Entity entity, CallbackInfoReturnable<Integer> cir) {
        if (entity instanceof MagicCarpetEntity) {
            cir.setReturnValue(Integer.MAX_VALUE);
            cir.cancel();
        } else if (entity instanceof PlayerEntity player) {
            List<Entity>
                otherEntities = player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0),
                Entity::isCollidable);

            if (!otherEntities.isEmpty()) {
                cir.setReturnValue(Integer.MAX_VALUE);
                cir.cancel();
            }
        }
    }
}
