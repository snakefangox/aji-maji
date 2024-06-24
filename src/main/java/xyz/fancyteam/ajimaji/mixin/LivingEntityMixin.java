package xyz.fancyteam.ajimaji.mixin;

import net.minecraft.entity.LivingEntity;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract boolean isUsingItem();

    @Shadow
    public abstract ItemStack getActiveItem();

    @Inject(method = "getHandSwingDuration", cancellable = true, at = @At("HEAD"))
    private void ajimaji$shortCardAttack(CallbackInfoReturnable<Integer> cir) {
        if (isUsingItem() && getActiveItem().isOf(AMItems.CARD_DECK)) {
            cir.setReturnValue(3);
        }
    }
}
