package xyz.fancyteam.ajimaji.component;

import com.mojang.serialization.Codec;

import net.minecraft.util.Uuids;

import java.util.UUID;

public record MagicCarpetComponent(UUID owner) {
    public static final Codec<MagicCarpetComponent> CODEC =
        Uuids.CODEC.xmap(MagicCarpetComponent::new, MagicCarpetComponent::owner);
    public static final MagicCarpetComponent DEFAULT = new MagicCarpetComponent(null);
}
