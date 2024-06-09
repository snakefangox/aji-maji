package xyz.fancyteam.ajimaji.net;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public final class PacketCodecUtils {
    private PacketCodecUtils() {}

    public static <T> PacketCodec<ByteBuf, TagKey<T>> tagKeyPacketCodec(RegistryKey<? extends Registry<T>> registry) {
        return Identifier.PACKET_CODEC.xmap(id -> TagKey.of(registry, id), TagKey::id);
    }
}
