package xyz.fancyteam.ajimaji.client.entity_renderer;

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

import xyz.fancyteam.ajimaji.client.AjiMajiClient;
import xyz.fancyteam.ajimaji.entity.PlayingCardEntity;

public class PlayingCardEntityRenderer extends EntityRenderer<PlayingCardEntity> {

    public static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/projectiles/arrow.png");

    public PlayingCardEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public void render(PlayingCardEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.multiply(
            RotationAxis.POSITIVE_Y.rotationDegrees(
                MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0f)
        );
        if (entity.getState() == PlayingCardEntity.State.FALLING) {
            float degrees = ((AjiMajiClient.rotationTicks + tickDelta) * 10.0f) % 360.0f;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(degrees));
        }
        else if (entity.getState() == PlayingCardEntity.State.FLYING) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));
        }
        float shake = (float) entity.shake - tickDelta;
        if (shake > 0.0f) {
            float shakeDegrees = -MathHelper.sin(shake * 3.0f) * shake;
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(shakeDegrees));
        }
        matrices.scale(0.05625f, 0.05625f, 0.05625f);
        //matrices.translate(-4.0f, 0.0f, 0.0f);
        var vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(getTexture(entity)));
        var entry = matrices.peek();
        vertex(entry, vertexConsumer, -7, -2, -2, 0.0f, 0.15625f, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, -2, 2, 0.15625f, 0.15625f, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, 2, 0.15625f, 0.3125f, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, -2, 0.0f, 0.3125f, -1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, -2, 0.0f, 0.15625f, 1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, 2, 2, 0.15625f, 0.15625f, 1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, -2, 2, 0.15625f, 0.3125f, 1, 0, 0, light);
        vertex(entry, vertexConsumer, -7, -2, -2, 0.0f, 0.3125f, 1, 0, 0, light);
        for (int i = 0; i < 4; i++) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
            vertex(entry, vertexConsumer, -8, -2, 0, 0.0f, 0.0f, 0, 1, 0, light);
            vertex(entry, vertexConsumer, 8, -2, 0, 0.5f, 0.0f, 0, 1, 0, light);
            vertex(entry, vertexConsumer, 8, 2, 0, 0.5f, 0.15625f, 0, 1, 0, light);
            vertex(entry, vertexConsumer, -8, 2, 0, 0.0f, 0.15625f, 0, 1, 0, light);
        }
        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    public void vertex(
        MatrixStack.Entry matrix, VertexConsumer vertexConsumer, int x, int y, int z, float u, float v, int normalX, int normalZ, int normalY, int light
    ) {
        vertexConsumer.vertex(matrix, (float)x, (float)y, (float)z)
            .color(Colors.WHITE)
            .texture(u, v)
            .overlay(OverlayTexture.DEFAULT_UV)
            .light(light)
            .normal(matrix, (float)normalX, (float)normalY, (float)normalZ);
    }

    @Override
    public Identifier getTexture(PlayingCardEntity entity) {
        return TEXTURE;
    }
}
