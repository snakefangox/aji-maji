package xyz.fancyteam.ajimaji;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import xyz.fancyteam.ajimaji.misc.AMEvents;
import xyz.fancyteam.ajimaji.misc.AMRegistries;
import xyz.fancyteam.ajimaji.misc.AMSoundEvents;
import xyz.fancyteam.ajimaji.net.AMNet;
import xyz.fancyteam.ajimaji.recipe.AMRecipes;
import xyz.fancyteam.ajimaji.top_hat.TopHatManager;
import xyz.fancyteam.ajimaji.util.ServerTaskQueue;

public class AjiMaji implements ModInitializer {

    public static final String MOD_ID = "aji-maji";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String FORCE_USE_TOP_HAT_PERM = "aji-maji.top-hat.force";
    public static final int FORCE_USE_TOP_HAT_PERM_DEFAULT = 3;
    public static final String USE_TOP_HAT_ON_ENTITIES_PERM = "aji-maji.top-hat.entities";
    public static final boolean USE_TOP_HAT_ON_ENTITIES_PERM_DEFAULT = false;
    public static final String USE_TOP_HAT_ON_PLAYERS_PERM = "aji-maji.top-hat.players";
    public static final boolean USE_TOP_HAT_ON_PLAYERS_PERM_DEFAULT = true;

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
        AMSoundEvents.register();
        AMEvents.register();
        AMNet.register();

        ServerTaskQueue.init();
        TopHatManager.init();
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static MutableText tt(String prefix, String suffix, Object... args) {
        return Text.translatable(prefix + "." + MOD_ID + "." + suffix, args);
    }
}
