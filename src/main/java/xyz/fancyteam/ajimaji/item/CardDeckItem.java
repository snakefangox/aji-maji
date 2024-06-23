package xyz.fancyteam.ajimaji.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import xyz.fancyteam.ajimaji.entity.PlayingCardEntity;
import xyz.fancyteam.ajimaji.misc.AMSoundEvents;

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
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        super.usageTick(world, user, stack, remainingUseTicks);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        super.use(world, user, hand);
        if (!user.isUsingItem() || !user.getActiveItem().isOf(AMItems.CARD_DECK) || user.getActiveHand() != hand) {
            user.setCurrentHand(hand);
            return TypedActionResult.consume(user.getStackInHand(hand));
        }
        var stack = user.getStackInHand(hand);
        if (stack.getCount() <= 1) {
            return TypedActionResult.fail(stack);
        }
        var entity = new PlayingCardEntity(world, user, stack);
        entity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0f, 2.0f, 1.0f);
        entity.initialVelocity = entity.getVelocity();
        world.spawnEntity(entity);
        stack.decrementUnlessCreative(1, user);
        world.playSound(user, user.getX(), user.getY(), user.getZ(), AMSoundEvents.CARD_THROW, SoundCategory.PLAYERS, 1.0f, 0.9f + world.getRandom().nextFloat() * 0.2f);
        user.swingHand(hand);
        user.getItemCooldownManager().set(AMItems.CARD_DECK, 5);
        return TypedActionResult.consume(user.getStackInHand(hand));
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }
}
