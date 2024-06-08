package xyz.fancyteam.ajimaji.client.entity_renderer;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.entity.MagicCarpet;

public class MagicCarpetRenderer extends EntityRenderer<MagicCarpet> {
    private static final Identifier TEXTURE = AjiMaji.id("textures/entity/magic_carpet.png");
    private static final RenderLayer MAGIC_CARPET = RenderLayer.getEntityCutout(TEXTURE);
    private final ModelPart model;

    protected MagicCarpetRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
        model = ctx.getPart(AMEntityRenderers.MODEL_MAGIC_CARPET_LAYER);
    }

    @Override
    public void render(MagicCarpet entity, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        int overlay = OverlayTexture.DEFAULT_UV;

        matrices.push();
        double cycle = Math.sin((entity.age + tickDelta) / 20.0) / 8.0;
        matrices.translate(0, cycle + 0.15, 0);
        model.render(matrices, vertexConsumers.getBuffer(MAGIC_CARPET), light, overlay);
        matrices.pop();

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(MagicCarpet entity) {
        return TEXTURE;
    }
}
