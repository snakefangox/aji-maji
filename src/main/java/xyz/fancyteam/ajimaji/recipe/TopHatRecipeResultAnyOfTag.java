package xyz.fancyteam.ajimaji.recipe;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;

import xyz.fancyteam.ajimaji.util.CodecUtils;
import xyz.fancyteam.ajimaji.util.PacketCodecUtils;

public record TopHatRecipeResultAnyOfTag(TagKey<Item> tag, int count) implements TopHatRecipeResult {
    public static final MapCodec<TopHatRecipeResultAnyOfTag> MAP_CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            CodecUtils.tagKeyCodec(RegistryKeys.ITEM).fieldOf("tag").forGetter(TopHatRecipeResultAnyOfTag::tag),
            Codec.INT.lenientOptionalFieldOf("count", 1).forGetter(TopHatRecipeResultAnyOfTag::count)
        ).apply(instance, TopHatRecipeResultAnyOfTag::new));

    public static final PacketCodec<RegistryByteBuf, TopHatRecipeResultAnyOfTag> PACKET_CODEC = PacketCodec.tuple(
        PacketCodecUtils.tagKeyPacketCodec(RegistryKeys.ITEM), TopHatRecipeResultAnyOfTag::tag,
        PacketCodecs.INTEGER, TopHatRecipeResultAnyOfTag::count,
        TopHatRecipeResultAnyOfTag::new
    );

    public static final Type TYPE = new Type(MAP_CODEC, PACKET_CODEC);

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void addResultItems(List<ItemStack> toAddTo, Random random) {
        ArrayList<RegistryEntry<Item>> registryEntries = Lists.newArrayList(Registries.ITEM.iterateEntries(tag));
        Item result = registryEntries.get(random.nextInt(registryEntries.size())).value();
        toAddTo.add(new ItemStack(result, count));
    }
}
