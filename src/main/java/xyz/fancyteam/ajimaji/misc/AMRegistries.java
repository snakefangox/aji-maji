package xyz.fancyteam.ajimaji.misc;

import com.mojang.serialization.Lifecycle;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;

import xyz.fancyteam.ajimaji.recipe.TopHatRecipeResult;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMRegistries {
    public static final RegistryKey<Registry<TopHatRecipeResult.Type>> TOP_HAT_RECIPE_OUTPUT_KEY =
        RegistryKey.ofRegistry(id("top_hat_recipe_output"));
    public static final Registry<TopHatRecipeResult.Type> TOP_HAT_RECIPE_OUTPUT =
        new SimpleRegistry<>(TOP_HAT_RECIPE_OUTPUT_KEY, Lifecycle.stable());

    public static void register() {
        Registry.register(registries(), TOP_HAT_RECIPE_OUTPUT_KEY.getValue(), TOP_HAT_RECIPE_OUTPUT);
    }

    @SuppressWarnings("unchecked")
    private static <T> Registry<Registry<T>> registries() {
        return (Registry<Registry<T>>) Registries.REGISTRIES;
    }
}
