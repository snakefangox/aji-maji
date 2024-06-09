package xyz.fancyteam.ajimaji.recipe;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.random.Random;

public record TopHatRecipeResultAllOf(List<TopHatRecipeResult> results) implements TopHatRecipeResult {
    public static final MapCodec<TopHatRecipeResultAllOf> MAP_CODEC =
        TopHatRecipeResult.CODEC.listOf().fieldOf("results")
            .xmap(TopHatRecipeResultAllOf::new, TopHatRecipeResultAllOf::results);

    public static final PacketCodec<RegistryByteBuf, TopHatRecipeResultAllOf> PACKET_CODEC =
        TopHatRecipeResult.PACKET_CODEC.collect(PacketCodecs.toList())
            .xmap(TopHatRecipeResultAllOf::new, TopHatRecipeResultAllOf::results);
    
    public static final Type TYPE = new Type(MAP_CODEC, PACKET_CODEC);

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void addResultItems(List<ItemStack> toAddTo, Random random) {
        for (TopHatRecipeResult result : results) {
            result.addResultItems(toAddTo, random);
        }
    }
}
