package xyz.fancyteam.ajimaji.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.network.ClientPlayerEntity;

import net.minecraft.entity.LivingEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @Shadow
    public abstract boolean isUsingItem();

    @ModifyExpressionValue(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean ajimaji$fastDeckWielding(boolean original) {
        return original && !((LivingEntity) (Object) this).getActiveItem().isOf(AMItems.CARD_DECK);
    }

    @Inject(method = "canStartSprinting", cancellable = true, at = @At("HEAD"))
    private void ajimaji$canSprintWhenWieldingDeck(CallbackInfoReturnable<Boolean> cir) {
        if (isUsingItem() && ((LivingEntity) (Object) this).getActiveItem().isOf(AMItems.CARD_DECK)) {
            cir.setReturnValue(true);
        }
    }
}
