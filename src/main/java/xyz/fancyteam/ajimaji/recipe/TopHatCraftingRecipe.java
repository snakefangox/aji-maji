package xyz.fancyteam.ajimaji.recipe;

import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.component.TopHatIdComponent;
import xyz.fancyteam.ajimaji.item.AMItems;
import xyz.fancyteam.ajimaji.item.TopHatBlockItem;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class TopHatCraftingRecipe extends SpecialCraftingRecipe {
    public static final RecipeSerializer<TopHatCraftingRecipe> SERIALIZER =
        new SpecialRecipeSerializer<>(TopHatCraftingRecipe::new);

    public TopHatCraftingRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingRecipeInput input, World world) {
        if (input.getSize() < 9) return false;

        for (int i = 0; i < 3; i++) {
            if (!input.getStackInSlot(i, 0).isOf(Blocks.BLACK_CARPET.asItem())) return false;
        }

        if (!input.getStackInSlot(0, 1).isOf(Blocks.BLACK_WOOL.asItem())) return false;
        if (!input.getStackInSlot(2, 1).isOf(Blocks.BLACK_WOOL.asItem())) return false;

        if (!input.getStackInSlot(0, 2).isOf(Blocks.BLACK_CARPET.asItem())) return false;
        if (!input.getStackInSlot(2, 2).isOf(Blocks.BLACK_CARPET.asItem())) return false;

        return input.getStackInSlot(1, 1).isOf(AMItems.TOP_HAT) || input.getStackInSlot(1, 1).isOf(Items.LAPIS_LAZULI);
    }

    @Override
    public ItemStack craft(CraftingRecipeInput input, RegistryWrapper.WrapperLookup lookup) {
        if (input.getSize() < 9) return ItemStack.EMPTY;

        for (int i = 0; i < 3; i++) {
            if (!input.getStackInSlot(i, 0).isOf(Blocks.BLACK_CARPET.asItem())) return ItemStack.EMPTY;
        }

        if (!input.getStackInSlot(0, 1).isOf(Blocks.BLACK_WOOL.asItem())) return ItemStack.EMPTY;
        if (!input.getStackInSlot(2, 1).isOf(Blocks.BLACK_WOOL.asItem())) return ItemStack.EMPTY;

        if (!input.getStackInSlot(0, 2).isOf(Blocks.BLACK_CARPET.asItem())) return ItemStack.EMPTY;
        if (!input.getStackInSlot(2, 2).isOf(Blocks.BLACK_CARPET.asItem())) return ItemStack.EMPTY;

        if (input.getStackInSlot(1, 1).isOf(AMItems.TOP_HAT)) {
            // top hat cloning - creates linked top hats
            ItemStack oldHat = input.getStackInSlot(1, 1);

            ItemStack newHat = AMItems.TOP_HAT.getDefaultStack().copy();
            newHat.set(AMDataComponents.TOP_HAT_ID, oldHat.get(AMDataComponents.TOP_HAT_ID));

            return newHat;
        } else if (input.getStackInSlot(1, 1).isOf(Items.LAPIS_LAZULI)) {
            // new top hat creation
            ItemStack newHat = AMItems.TOP_HAT.getDefaultStack().copy();
            TopHatIdComponent.getOrCreate(newHat);
            return newHat;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput craftingRecipeInput) {
        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(craftingRecipeInput.getSize(), ItemStack.EMPTY);

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack itemStack = craftingRecipeInput.getStackInSlot(i);
            if (itemStack.getItem().hasRecipeRemainder()) {
                assert itemStack.getItem().getRecipeRemainder() != null;
                defaultedList.set(i, new ItemStack(itemStack.getItem().getRecipeRemainder()));
            } else if (itemStack.getItem() instanceof TopHatBlockItem) {
                defaultedList.set(i, itemStack.copyWithCount(1));
                break;
            }
        }

        return defaultedList;
    }

    @Override
    public boolean fits(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }
}
