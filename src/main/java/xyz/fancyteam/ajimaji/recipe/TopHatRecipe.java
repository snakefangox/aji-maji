package xyz.fancyteam.ajimaji.recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.world.World;

public class TopHatRecipe implements Recipe<TopHatRecipeInput> {
    public static final Serializer SERIALIZER = new Serializer();
    public static final RecipeType<TopHatRecipe> TYPE = new RecipeType<>() {};

    private final boolean orderSpecific;
    private final boolean avoidInputsInOutputs;
    private final List<Ingredient> ingredients;
    private final TopHatRecipeResult result;

    public TopHatRecipe(boolean orderSpecific, boolean avoidInputsInOutputs, List<Ingredient> ingredients,
                        TopHatRecipeResult result) {
        this.orderSpecific = orderSpecific;
        this.avoidInputsInOutputs = avoidInputsInOutputs;
        this.ingredients = ImmutableList.copyOf(ingredients);
        this.result = result;
    }

    @Override
    public boolean matches(TopHatRecipeInput input, World world) {
        if (orderSpecific) {
            int ingredientsSize = ingredients.size();
            if (input.getSize() != ingredientsSize) return false;

            for (int i = 0; i < ingredientsSize; i++) {
                if (!ingredients.get(i).test(input.getStackInSlot(i))) return false;
            }

            return true;
        } else {
            List<Ingredient> mutIngredients = new ArrayList<>(ingredients);

            int inputSize = input.getSize();
            for (int i = 0; i < inputSize; i++) {
                ItemStack stack = input.getStackInSlot(i);

                Iterator<Ingredient> iter = mutIngredients.iterator();
                boolean found = false;
                while (iter.hasNext()) {
                    Ingredient ingredient = iter.next();
                    if (ingredient.test(stack)) {
                        iter.remove();
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    return false;
                }
            }

            return mutIngredients.isEmpty();
        }
    }

    public List<ItemStack> craft(TopHatRecipeInput input) {
        List<ItemStack> results = new ArrayList<>();
        result.addResultItems(results, input.random());
        return results;
    }

    public TopHatRecipeResult getResult() {
        return result;
    }

    @Override
    public ItemStack craft(TopHatRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResult(RegistryWrapper.WrapperLookup registriesLookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        return null;
    }

    public static final class Serializer implements RecipeSerializer<TopHatRecipe> {
        public static final MapCodec<TopHatRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.lenientOptionalFieldOf("order_specific", false).forGetter(recipe -> recipe.orderSpecific),
            Codec.BOOL.lenientOptionalFieldOf("avoid_inputs_in_outputs", true)
                .forGetter(recipe -> recipe.avoidInputsInOutputs),
            Ingredient.DISALLOW_EMPTY_CODEC.listOf().fieldOf("ingredients").forGetter(recipe -> recipe.ingredients),
            TopHatRecipeResult.CODEC.fieldOf("result").forGetter(TopHatRecipe::getResult)
        ).apply(instance, TopHatRecipe::new));

        public static final PacketCodec<RegistryByteBuf, TopHatRecipe> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOL, recipe -> recipe.orderSpecific,
            PacketCodecs.BOOL, recipe -> recipe.avoidInputsInOutputs,
            Ingredient.PACKET_CODEC.collect(PacketCodecs.toList()), recipe -> recipe.ingredients,
            TopHatRecipeResult.PACKET_CODEC, TopHatRecipe::getResult,
            TopHatRecipe::new
        );

        @Override
        public MapCodec<TopHatRecipe> codec() {
            return MAP_CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, TopHatRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}
