package xyz.fancyteam.ajimaji.client.entity_renderer;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;

import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

import xyz.fancyteam.ajimaji.entity.PlayingCardEntity;

// Made with Blockbench 4.10.3
public class PlayingCardEntityModel extends EntityModel<PlayingCardEntity> {
    private final ModelPart card;

    public PlayingCardEntityModel(ModelPart root) {
        card = root.getChild("bb_main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();
        modelPartData.addChild("bb_main",
            ModelPartBuilder.create().uv(0, 0).cuboid(-4.0f, -12.0f, 0.0f, 8.0f, 12.0f, 0.0f, new Dilation(0.0f)),
            ModelTransform.pivot(0.0f, 24.0f, 0.0f));
        return TexturedModelData.of(modelData, 16, 16);
    }

    @Override
    public void setAngles(PlayingCardEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        card.render(matrices, vertices, light, overlay);
    }
}
