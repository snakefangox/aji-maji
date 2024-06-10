package xyz.fancyteam.ajimaji.misc;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMDensityFunctions {
    public static void register() {
        Registry.register(Registries.DENSITY_FUNCTION_TYPE, id("scaled"), ScaledDensityFunction.CODEC);
    }
}
