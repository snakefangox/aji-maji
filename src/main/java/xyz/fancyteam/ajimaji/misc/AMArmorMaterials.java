package xyz.fancyteam.ajimaji.misc;

import java.util.EnumMap;
import java.util.List;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMArmorMaterials {
    public static RegistryEntry<ArmorMaterial> MAGICIANS_ARMOR_MATERIAL;

    public static void register() {
        // TODO: add repair ingredient, investigate 0 layers
        MAGICIANS_ARMOR_MATERIAL = Registry.registerReference(Registries.ARMOR_MATERIAL, id("magicians_armor"),
            new ArmorMaterial(Util.make(new EnumMap<>(ArmorItem.Type.class), map -> map.put(ArmorItem.Type.HELMET, 1)),
                30, Registries.SOUND_EVENT.getEntry(SoundEvents.BLOCK_WOOL_PLACE), () -> Ingredient.EMPTY,
                List.of(), 0f, 0f));
    }
}
