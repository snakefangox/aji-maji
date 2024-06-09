package xyz.fancyteam.ajimaji.block_entity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import xyz.fancyteam.ajimaji.block.AMBlocks;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMBlockEntities {
    public static final BlockEntityType<TopHatBlockEntity> TOP_HAT =
        BlockEntityType.Builder.create(TopHatBlockEntity::new, AMBlocks.TOP_HAT).build(null);

    public static void register() {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id("top_hat"), TOP_HAT);
    }
}
