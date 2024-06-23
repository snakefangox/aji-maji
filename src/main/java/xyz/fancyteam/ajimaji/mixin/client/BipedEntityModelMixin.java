package xyz.fancyteam.ajimaji.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;

import net.minecraft.entity.LivingEntity;

import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
    @Shadow
    @Final
    public ModelPart rightArm;

    @Shadow
    @Final
    public ModelPart leftArm;

    @Inject(method = "positionRightArm", at = @At("HEAD"))
    private void ajimaji$wieldDeckRight(T entity, CallbackInfo ci) {
        if (!entity.isUsingItem() || !entity.getActiveItem().isOf(AMItems.CARD_DECK)) {
            return;
        }
        float rot = entity.getActiveHand() == Hand.MAIN_HAND ? 10.0f : 5.0f;
        rightArm.pitch = rightArm.pitch * 0.5f - (float) Math.PI + rot;
        rightArm.yaw = 0.0f;
    }

    @Inject(method = "positionLeftArm", at = @At("HEAD"))
    private void ajimaji$wieldDeckLeft(T entity, CallbackInfo ci) {
        if (!entity.isUsingItem() || !entity.getActiveItem().isOf(AMItems.CARD_DECK)) {
            return;
        }
        float rot = entity.getActiveHand() == Hand.OFF_HAND ? 10.0f : 5.0f;
        leftArm.pitch = leftArm.pitch * 0.5f - (float) Math.PI + rot;
        leftArm.yaw = 0.0f;
    }
}
