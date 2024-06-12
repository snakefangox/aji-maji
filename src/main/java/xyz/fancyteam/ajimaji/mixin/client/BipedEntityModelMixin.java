package xyz.fancyteam.ajimaji.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;

import net.minecraft.entity.LivingEntity;

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

    @Inject(method = "positionRightArm", at = @At("HEAD"))
    private void ajimaji$wieldDeckRight(T entity, CallbackInfo ci) {
        if (entity.isUsingItem() && entity.getActiveItem().isOf(AMItems.CARD_DECK)) {
            rightArm.pitch = rightArm.pitch * 0.5f - (float) Math.PI;
            rightArm.yaw = 0.0f;
        }
    }
}
