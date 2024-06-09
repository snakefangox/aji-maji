package xyz.fancyteam.ajimaji.recipe;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import xyz.fancyteam.ajimaji.misc.AMRegistries;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMRecipes {
    public static void register() {
        Registry.register(Registries.RECIPE_TYPE, id("top_hat_recipe"), TopHatRecipe.TYPE);
        Registry.register(Registries.RECIPE_SERIALIZER, id("top_hat_recipe"), TopHatRecipe.SERIALIZER);

        Registry.register(AMRegistries.TOP_HAT_RECIPE_OUTPUT, id("item"), TopHatRecipeResultItem.TYPE);
        Registry.register(AMRegistries.TOP_HAT_RECIPE_OUTPUT, id("any_of_tag"), TopHatRecipeResultAnyOfTag.TYPE);
        Registry.register(AMRegistries.TOP_HAT_RECIPE_OUTPUT, id("any_of"), TopHatRecipeResultAnyOf.TYPE);
        Registry.register(AMRegistries.TOP_HAT_RECIPE_OUTPUT, id("all_of"), TopHatRecipeResultAllOf.TYPE);
    }
}
