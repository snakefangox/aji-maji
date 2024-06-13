package xyz.fancyteam.ajimaji.misc;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMDimensions {
    public static final RegistryKey<World> TOP_HAT_DIMENSION = RegistryKey.of(RegistryKeys.WORLD, id("top_hat"));
}
