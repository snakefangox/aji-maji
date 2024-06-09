package xyz.fancyteam.ajimaji.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.util.math.random.Random;

public record TopHatRecipeInput(List<ItemStack> inputs, Random random) implements RecipeInput {
    @Override
    public ItemStack getStackInSlot(int slot) {
        return inputs.get(slot);
    }

    @Override
    public int getSize() {
        return inputs.size();
    }
}
