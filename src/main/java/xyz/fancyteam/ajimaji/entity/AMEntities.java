package xyz.fancyteam.ajimaji.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMEntities {
    public static final EntityType<MagicCarpet> MAGIC_CARPET =
        EntityType.Builder.create(MagicCarpet::new, SpawnGroup.MISC)
            .dimensions(1.25f, 0.25f).build();

    public static final EntityType<PlayingCardEntity> PLAYING_CARD =
        EntityType.Builder.<PlayingCardEntity>create(PlayingCardEntity::new, SpawnGroup.MISC)
            .dimensions(0.5F, 0.5F).eyeHeight(0.13F).maxTrackingRange(4).trackingTickInterval(20).build();

    public static void register() {
        register("magic_carpet", MAGIC_CARPET);
        register("playing_card", PLAYING_CARD);
    }

    private static void register(String path, EntityType<?> entityType) {
        Registry.register(Registries.ENTITY_TYPE, id(path), entityType);
    }
}
