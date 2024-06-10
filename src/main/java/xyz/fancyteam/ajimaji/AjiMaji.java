package xyz.fancyteam.ajimaji;

import java.util.logging.Logger;

import net.fabricmc.api.ModInitializer;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import xyz.fancyteam.ajimaji.block.AMBlocks;
import xyz.fancyteam.ajimaji.block_entity.AMBlockEntities;
import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.entity.AMEntities;
import xyz.fancyteam.ajimaji.item.AMItems;
import xyz.fancyteam.ajimaji.misc.AMArmorMaterials;
import xyz.fancyteam.ajimaji.misc.AMCreativeTabs;
import xyz.fancyteam.ajimaji.misc.AMDensityFunctions;
import xyz.fancyteam.ajimaji.misc.AMRegistries;
import xyz.fancyteam.ajimaji.recipe.AMRecipes;

public class AjiMaji implements ModInitializer {

    public static final String MOD_ID = "aji-maji";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        AMRegistries.register();
        AMBlocks.register();
        AMArmorMaterials.register();
        AMItems.register();
        AMBlockEntities.register();
        AMEntities.register();
        AMCreativeTabs.register();
        AMDataComponents.register();
        AMRecipes.register();
        AMDensityFunctions.register();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static MutableText tt(String prefix, String suffix) {
        return Text.translatable(prefix + "." + MOD_ID + "." + suffix);
    }
}
