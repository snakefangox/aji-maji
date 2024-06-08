package xyz.fancyteam.ajimaji.client.armor_renderer;

import xyz.fancyteam.ajimaji.item.AMItems;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

public class AMArmorRenderers {
    public static void register() {
        ArmorRenderer.register(new TopHatArmorRenderer(), AMItems.TOP_HAT);
    }
}
