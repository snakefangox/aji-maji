package xyz.fancyteam.ajimaji.client.armor_renderer;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class TopHatArmorRenderer implements ArmorRenderer {
    public static final ModelIdentifier TOP_HAT_ARMOR = new ModelIdentifier(id("top_hat"), "wearing=true");

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
                       LivingEntity entity, EquipmentSlot slot, int light,
                       BipedEntityModel<LivingEntity> contextModel) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();
        BakedModelManager modelManager = client.getBakedModelManager();
        BakedModel model = modelManager.getModel(TOP_HAT_ARMOR);

        matrices.push();
        contextModel.getHead().rotate(matrices);
        HeadFeatureRenderer.translate(matrices, false);

        itemRenderer.renderItem(stack, ModelTransformationMode.HEAD, false, matrices, vertexConsumers, light,
            OverlayTexture.DEFAULT_UV, model);

        matrices.pop();
    }
}
