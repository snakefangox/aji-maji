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

import net.minecraft.util.math.Direction;

import net.minecraft.util.math.RotationAxis;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.entity.MagicCarpetEntity;

public class MagicCarpetRenderer extends EntityRenderer<MagicCarpetEntity> {
    private static final Identifier TEXTURE = AjiMaji.id("textures/entity/magic_carpet.png");

    protected MagicCarpetRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public Identifier getTexture(MagicCarpetEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(MagicCarpetEntity entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        var renderLayer = RenderLayer.getEntityCutout(getTexture(entity));
        var vertexConsumer = vertexConsumers.getBuffer(renderLayer);

        matrices.push();
        matrices.scale(0.125F, 0.125F, 0.125F);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180F - yaw));

        for (int z = 0; z < 16; z++) {
            float wave = entity.isGrounded() ? 0 : entity.age + tickDelta + z;
            float height = (float) (Math.sin(wave / 4.0F) / 3.0F);
            drawCarpetSegment(matrices, light, vertexConsumer, 8 - z, height, (z + 1) / 16.0F);
        }

        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    private static void drawCarpetSegment(MatrixStack matrices, int light, VertexConsumer vertexConsumer, float z,
                                          float y, float vProgress) {
        var uvScale = 1F / 32F;
        float zn = z - 1F;
        float vStartProgress = Math.min(vProgress + (1F / 16F), 1F);
        float vNextProgress = vProgress - (1F / 16F);
        float vEndProgress = Math.max(vNextProgress - (1F / 16F), 0F);

        // Up
        vertex(vertexConsumer, matrices.peek(), -6F, .5F + y, zn, 0, vProgress, light, Direction.UP);
        vertex(vertexConsumer, matrices.peek(), -6F, .5F + y, z, 0, vNextProgress, light, Direction.UP);
        vertex(vertexConsumer, matrices.peek(), 6F, .5F + y, z, 0.75F, vNextProgress, light, Direction.UP);
        vertex(vertexConsumer, matrices.peek(), 6F, .5F + y, zn, 0.75F, vProgress, light, Direction.UP);

        // Down
        vertex(vertexConsumer, matrices.peek(), 6F, 0 + y, zn, 0.75F, vProgress, light, Direction.DOWN);
        vertex(vertexConsumer, matrices.peek(), 6F, 0 + y, z, 0.75F, vNextProgress, light, Direction.DOWN);
        vertex(vertexConsumer, matrices.peek(), -6F, 0 + y, z, 0, vNextProgress, light, Direction.DOWN);
        vertex(vertexConsumer, matrices.peek(), -6F, 0 + y, zn, 0, vProgress, light, Direction.DOWN);

        // Forwards
        vertex(vertexConsumer, matrices.peek(), -6F, .5F + y, z, 0, vNextProgress, light, Direction.NORTH);
        vertex(vertexConsumer, matrices.peek(), -6F, 0 + y, z, 0, vEndProgress, light, Direction.NORTH);
        vertex(vertexConsumer, matrices.peek(), 6F, 0 + y, z, 0.75F, vEndProgress, light, Direction.NORTH);
        vertex(vertexConsumer, matrices.peek(), 6F, .5F + y, z, 0.75F, vNextProgress, light, Direction.NORTH);

        // Backwards
        vertex(vertexConsumer, matrices.peek(), 6F, .5F + y, zn, 0.75F, vProgress, light, Direction.SOUTH);
        vertex(vertexConsumer, matrices.peek(), 6F, 0 + y, zn, 0.75F, vStartProgress, light, Direction.SOUTH);
        vertex(vertexConsumer, matrices.peek(), -6F, 0 + y, zn, 0, vStartProgress, light, Direction.SOUTH);
        vertex(vertexConsumer, matrices.peek(), -6F, .5F + y, zn, 0, vProgress, light, Direction.SOUTH);

        // Left
        vertex(vertexConsumer, matrices.peek(), -6F, .5F + y, zn, uvScale, vProgress, light, Direction.EAST);
        vertex(vertexConsumer, matrices.peek(), -6F, 0 + y, zn, uvScale, vNextProgress, light, Direction.EAST);
        vertex(vertexConsumer, matrices.peek(), -6F, 0 + y, z, 0, vNextProgress, light, Direction.EAST);
        vertex(vertexConsumer, matrices.peek(), -6F, .5F + y, z, 0, vProgress, light, Direction.EAST);

        // Right
        vertex(vertexConsumer, matrices.peek(), 6F, .5F + y, z, 0, vProgress, light, Direction.WEST);
        vertex(vertexConsumer, matrices.peek(), 6F, 0 + y, z, 0, vNextProgress, light, Direction.WEST);
        vertex(vertexConsumer, matrices.peek(), 6F, 0 + y, zn, uvScale, vNextProgress, light, Direction.WEST);
        vertex(vertexConsumer, matrices.peek(), 6F, .5F + y, zn, uvScale, vProgress, light, Direction.WEST);
    }

    public static void vertex(VertexConsumer vertexConsumer, MatrixStack.Entry matrix, float x, float y, float z,
                              float u, float v, int light, Direction direction) {
        vertexConsumer.vertex(matrix, x, y, z).texture(u, v).overlay(OverlayTexture.DEFAULT_UV).light(light)
            .color(Colors.WHITE).normal(matrix, (float) direction.getOffsetX(), (float) direction.getOffsetY(),
                (float) direction.getOffsetZ());
    }
}
