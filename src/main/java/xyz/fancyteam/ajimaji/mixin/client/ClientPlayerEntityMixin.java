package xyz.fancyteam.ajimaji.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean ajimaji$fastCardWielding(boolean original) {
        return original && !((LivingEntity) (Object) this).getActiveItem().isOf(AMItems.CARD_DECK);
    }
}
