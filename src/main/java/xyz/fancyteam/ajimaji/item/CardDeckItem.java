package xyz.fancyteam.ajimaji.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import xyz.fancyteam.ajimaji.entity.PlayingCardEntity;

public class CardDeckItem extends Item implements FabricItem {
    public CardDeckItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack,
                                                  ItemStack newStack) {
        return false;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.getCount() > 1;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return (int) (13.0f * (stack.getCount() - 1) / 63.0f);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x0000FF; // TODO: actual color
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        super.use(world, user, hand);
        var stack = user.getStackInHand(hand);
        if (stack.getCount() <= 1) {
            return TypedActionResult.fail(stack);
        }
        var entity = new PlayingCardEntity(world, user, stack);
        entity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 2.0f, 1.0f);
        entity.initialVelocity = entity.getVelocity();
        world.spawnEntity(entity);
        stack.decrement(1);
        return TypedActionResult.consume(stack);
    }
}
