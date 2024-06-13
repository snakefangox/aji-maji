package xyz.fancyteam.ajimaji.client.entity_renderer;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.client.AjiMajiClient;
import xyz.fancyteam.ajimaji.entity.PlayingCardEntity;

public class PlayingCardEntityRenderer extends EntityRenderer<PlayingCardEntity> {
    public static final Identifier TEXTURE = AjiMaji.id("textures/item/playing_card.png");
    public static final RenderLayer LAYER = RenderLayer.getEntityCutout(TEXTURE);

    private final ModelPart model;

    public PlayingCardEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
        model = context.getPart(AMEntityRenderers.MODEL_PLAYING_CARD_LAYER);
    }

    @Override
    public void render(PlayingCardEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(
            RotationAxis.POSITIVE_Y.rotationDegrees(
                MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()))
        );
        if (entity.getState() == PlayingCardEntity.State.FALLING) {
            float degrees = ((AjiMajiClient.rotationTicks + tickDelta) * 10.0f) % 360.0f;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(degrees));
        }
        else if (entity.getState() == PlayingCardEntity.State.FLYING) {
            matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));
        }
        float shake = (float) entity.shake - tickDelta;
        if (shake > 0.0f) {
            float shakeDegrees = -MathHelper.sin(shake * 3.0f) * shake;
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(shakeDegrees));
        }
        matrices.translate(0, 0.025f, -1.125f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
        model.render(matrices, vertexConsumers.getBuffer(LAYER), light, OverlayTexture.DEFAULT_UV);
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(PlayingCardEntity entity) {
        return TEXTURE;
    }
}
