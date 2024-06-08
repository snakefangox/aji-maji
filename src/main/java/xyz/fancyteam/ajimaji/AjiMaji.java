package xyz.fancyteam.ajimaji;

import java.util.logging.Logger;

import xyz.fancyteam.ajimaji.block.AMBlocks;
import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.entity.AMEntities;
import xyz.fancyteam.ajimaji.item.AMItems;
import xyz.fancyteam.ajimaji.misc.AMArmorMaterials;
import xyz.fancyteam.ajimaji.misc.AMCreativeTabs;

import net.fabricmc.api.ModInitializer;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AjiMaji implements ModInitializer {

    public static final String MOD_ID = "aji-maji";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AMBlocks.register();
        AMArmorMaterials.register();
        AMItems.register();
        AMEntities.register();
        AMCreativeTabs.register();
        AMDataComponents.register();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static MutableText tt(String prefix, String suffix) {
        return Text.translatable(prefix + "." + MOD_ID + "." + suffix);
    }
}
