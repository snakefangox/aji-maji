package xyz.fancyteam.ajimaji.util;

import com.mojang.serialization.Codec;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class CodecUtils {
    private CodecUtils() {}

    public static <T> Codec<TagKey<T>> tagKeyCodec(RegistryKey<Registry<T>> key) {
        return Identifier.CODEC.xmap(id -> TagKey.of(key, id), TagKey::id);
    }
}
