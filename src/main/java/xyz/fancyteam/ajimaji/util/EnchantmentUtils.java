package xyz.fancyteam.ajimaji.util;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;

import xyz.fancyteam.ajimaji.AjiMaji;

public class EnchantmentUtils {
    public static final RegistryKey<Enchantment> WIND_SPEED_KEY =
        RegistryKey.of(RegistryKeys.ENCHANTMENT, AjiMaji.id("wind_speed"));
    public static final RegistryKey<Enchantment> RESCUE_BLANKET_KEY =
        RegistryKey.of(RegistryKeys.ENCHANTMENT, AjiMaji.id("rescue_blanket"));
    public static final RegistryKey<Enchantment> BROAD_KEY =
        RegistryKey.of(RegistryKeys.ENCHANTMENT, AjiMaji.id("broad"));
    public static final RegistryKey<Enchantment> FOUR_WINDS =
        RegistryKey.of(RegistryKeys.ENCHANTMENT, AjiMaji.id("four_winds"));

    public static RegistryEntry.Reference<Enchantment> getEnchantmentEntry(World world, RegistryKey<Enchantment> registryKey) {
        return world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).entryOf(registryKey);
    }
}
