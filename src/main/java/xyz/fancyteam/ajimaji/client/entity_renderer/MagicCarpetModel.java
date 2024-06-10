package xyz.fancyteam.ajimaji.client.entity_renderer;

import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;

import xyz.fancyteam.ajimaji.entity.MagicCarpetEntity;

public class MagicCarpetModel extends EntityModel<MagicCarpetEntity> {
    private final ModelPart main;

    public MagicCarpetModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static TexturedModelData getTexturedModelData() {
        ModelData modelData = new ModelData();
        ModelPartData modelPartData = modelData.getRoot();

        modelPartData.addChild("main", ModelPartBuilder.create().uv(-30, -30)
                .cuboid(-12.0F, -1.0F, -16.0F, 24.0F, 1.0F, 32.0F),
            ModelTransform.pivot(0F, 0F, 0F));

        return TexturedModelData.of(modelData, 16, 16);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        main.render(matrices, vertices, light, overlay, color);
    }

    @Override
    public void setAngles(MagicCarpetEntity entity, float limbAngle, float limbDistance, float animationProgress,
                          float headYaw, float headPitch) {}
}