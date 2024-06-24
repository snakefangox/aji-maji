package xyz.fancyteam.ajimaji.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.fancyteam.ajimaji.entity.MagicCarpetEntity;
import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean ajimaji$fastDeckWielding(boolean original) {
        return original && !((LivingEntity) (Object) this).getActiveItem().isOf(AMItems.CARD_DECK);
    }

    @ModifyExpressionValue(method = "canStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean ajimaji$canSprintWhenWieldingDeck(boolean original) {
        return original && !((LivingEntity) (Object) this).getActiveItem().isOf(AMItems.CARD_DECK);
    }
}
