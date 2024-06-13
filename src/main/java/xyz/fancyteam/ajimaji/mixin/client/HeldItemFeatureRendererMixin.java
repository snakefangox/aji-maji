package xyz.fancyteam.ajimaji.mixin.client;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import net.minecraft.util.Hand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xyz.fancyteam.ajimaji.block.AMBlocks;
import xyz.fancyteam.ajimaji.item.AMItems;

@Mixin(HeldItemFeatureRenderer.class)
public class HeldItemFeatureRendererMixin<T extends LivingEntity> {
    @Unique
    private T ajimaji$renderingEntity;

    @Inject(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
        at = @At(
            value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/render/entity/feature/HeldItemFeatureRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
        )
    )
    private void ajimaji$captureRenderingEntity(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider,
                                                int i, T livingEntity, float f, float g, float h, float j, float k,
                                                float l, CallbackInfo ci) {
        ajimaji$renderingEntity = livingEntity;
    }

    @ModifyArg(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
        at = @At(
            value = "INVOKE", ordinal = 0,
            target = "Lnet/minecraft/client/render/entity/feature/HeldItemFeatureRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
        ),
        index = 1
    )
    private ItemStack ajimaji$renderFakeCardBoxRight(ItemStack stack) {
        return ajimaji$renderingEntity != null && ajimaji$renderingEntity.isUsingItem()
            && ajimaji$renderingEntity.getActiveItem().isOf(AMItems.CARD_DECK)
            && ajimaji$renderingEntity.getActiveHand() == Hand.OFF_HAND
            ? AMBlocks.CARD_BOX.asItem().getDefaultStack() : stack;
    }

    @ModifyArg(
        method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
        at = @At(
            value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/client/render/entity/feature/HeldItemFeatureRenderer;renderItem(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/render/model/json/ModelTransformationMode;Lnet/minecraft/util/Arm;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
        ),
        index = 1
    )
    private ItemStack ajimaji$renderFakeCardBoxLeft(ItemStack stack) {
        return ajimaji$renderingEntity != null && ajimaji$renderingEntity.isUsingItem()
            && ajimaji$renderingEntity.getActiveItem().isOf(AMItems.CARD_DECK)
            && ajimaji$renderingEntity.getActiveHand() == Hand.MAIN_HAND
            ? AMBlocks.CARD_BOX.asItem().getDefaultStack() : stack;
    }
}
