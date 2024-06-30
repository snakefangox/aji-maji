package xyz.fancyteam.ajimaji.client.armor_renderer;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

import xyz.fancyteam.ajimaji.item.AMItems;

public class AMArmorRenderers {
    public static void register() {
        ArmorRenderer.register(new TopHatArmorRenderer(), AMItems.TOP_HAT);
        ArmorRenderer.register(new BunnyEarsArmorRenderer(), AMItems.BUNNY_EARS);

        ModelLoadingPlugin.register(pluginContext -> pluginContext.addModels(BunnyEarsArmorRenderer.BUNNY_EARS_ARMOR));
    }
}
