package xyz.fancyteam.ajimaji.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.random.Random;

import xyz.fancyteam.ajimaji.misc.AMRegistries;

public interface TopHatRecipeResult {
    Codec<TopHatRecipeResult> CODEC = AMRegistries.TOP_HAT_RECIPE_OUTPUT.getCodec()
        .dispatch("type", TopHatRecipeResult::getType, Type::mapCodec);

    PacketCodec<RegistryByteBuf, TopHatRecipeResult> PACKET_CODEC =
        PacketCodecs.registryValue(AMRegistries.TOP_HAT_RECIPE_OUTPUT_KEY)
            .dispatch(TopHatRecipeResult::getType, Type::packetCodec);

    Type getType();

    void addResultItems(List<ItemStack> toAddTo, Random random);

    record Type(MapCodec<? extends TopHatRecipeResult> mapCodec,
                PacketCodec<RegistryByteBuf, ? extends TopHatRecipeResult> packetCodec) {}
}
