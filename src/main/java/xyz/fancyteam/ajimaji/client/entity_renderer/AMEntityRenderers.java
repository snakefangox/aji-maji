package xyz.fancyteam.ajimaji.client.entity_renderer;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.entity.AMEntities;

public class AMEntityRenderers {
    public static final EntityModelLayer MODEL_MAGIC_CARPET_LAYER = new EntityModelLayer(AjiMaji.id("magic_carpet"), "main");

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(AMEntities.MAGIC_CARPET, MagicCarpetRenderer::new);
    }

    public static void registerModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(MODEL_MAGIC_CARPET_LAYER, MagicCarpetModel::getTexturedModelData);
    }
}
