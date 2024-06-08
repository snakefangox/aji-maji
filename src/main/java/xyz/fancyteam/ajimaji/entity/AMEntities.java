package xyz.fancyteam.ajimaji.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMEntities {
    public static final EntityType<MagicCarpet> MAGIC_CARPET = EntityType.Builder.create(MagicCarpet::new, SpawnGroup.MISC)
            .dimensions(1.25f, 0.25f).build();

    public static void register() {
        register("magic_carpet", MAGIC_CARPET);
    }

    private static void register(String path, EntityType<?> entityType) {
        Registry.register(Registries.ENTITY_TYPE, id(path), entityType);
    }
}
