package xyz.fancyteam.ajimaji.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.math.random.Random;

public record TopHatRecipeResultItem(ItemStack stack) implements TopHatRecipeResult {
    public static final MapCodec<TopHatRecipeResultItem> MAP_CODEC =
        RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.UNCOUNTED_CODEC.fieldOf("item").forGetter(TopHatRecipeResultItem::stack),
            Codec.INT.lenientOptionalFieldOf("count", 1).forGetter(result -> result.stack.getCount())
        ).apply(instance, TopHatRecipeResultItem::new));

    public static final PacketCodec<RegistryByteBuf, TopHatRecipeResultItem> PACKET_CODEC =
        ItemStack.PACKET_CODEC.xmap(TopHatRecipeResultItem::new, TopHatRecipeResultItem::stack);
    
    public static final Type TYPE = new Type(MAP_CODEC, PACKET_CODEC);

    private TopHatRecipeResultItem(ItemStack stack, int count) {
        this(stack.copyWithCount(count));
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    @Override
    public void addResultItems(List<ItemStack> toAddTo, Random random) {
        toAddTo.add(stack.copy());
    }
}
