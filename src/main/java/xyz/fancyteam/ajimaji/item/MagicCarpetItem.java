package xyz.fancyteam.ajimaji.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.entity.AMEntities;
import xyz.fancyteam.ajimaji.entity.MagicCarpetEntity;
import xyz.fancyteam.ajimaji.misc.AMSoundEvents;
import xyz.fancyteam.ajimaji.mixin.DamageTrackerAccessor;
import xyz.fancyteam.ajimaji.mixin.LivingEntityInvoker;
import xyz.fancyteam.ajimaji.util.EnchantmentUtils;

import java.util.Objects;

public class MagicCarpetItem extends Item {
    public MagicCarpetItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if (!ctx.getWorld().isClient && !ctx.hitsInsideBlock()) {
            int slot =
                ctx.getHand() == Hand.MAIN_HAND ? Objects.requireNonNull(ctx.getPlayer()).getInventory().selectedSlot :
                    PlayerInventory.OFF_HAND_SLOT;

            spawnCarpetFromItem(ctx.getWorld(), ctx.getPlayer(), ctx.getStack(), ctx.getSide(), ctx.getHitPos(), slot,
                false);
        }

        if (ctx.hitsInsideBlock()) {
            return ActionResult.PASS;
        } else {
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (world.isClient) return;
        var ownerData = stack.get(AMDataComponents.MAGIC_CARPET_DATA);
        if (ownerData != null && !ownerData.owner().equals(entity.getUuid())) return;

        int rescueLevel = stack.getEnchantments()
            .getLevel(EnchantmentUtils.getEnchantmentEntry(world, EnchantmentUtils.RESCUE_BLANKET_KEY));
        if (rescueLevel >= 1 && !entity.hasVehicle() && entity instanceof PlayerEntity player) {
            float damage = ((LivingEntityInvoker) player).aji_maji_computeFallDamage(player.fallDistance, 1.0F);
            // Trigger if a fall would put us on 1 heart or less
            if (damage >= player.getHealth() - 2 && player.canTakeDamage()) {
                var hit = world.raycast(new RaycastContext(player.getPos(), player.getPos().subtract(0, 3, 0),
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
                if (hit.getType() == HitResult.Type.BLOCK) {
                    var carpet = spawnCarpetFromItem(world, player, stack, Direction.UP, player.getPos(), slot, true);
                    if (carpet != null) player.startRiding(carpet);
                }
            }

            if (rescueLevel >= 2 && player.getHealth() <= player.getMaxHealth() * 0.2F) {
                boolean damagedThisTick =
                    player.age - ((DamageTrackerAccessor) player.getDamageTracker()).getAgeOnLastDamage() <= 1;
                if (damagedThisTick) {
                    var hit = world.raycast(new RaycastContext(player.getPos().add(0, 150, 0),
                        player.getPos().add(0, 15, 0), RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE, player));
                    var openPosition = hit.getPos().add(0, 2, 0);
                    var carpet = spawnCarpetFromItem(world, player, stack, Direction.UP, openPosition, slot, true);

                    if (carpet != null) {
                        player.startRiding(carpet);
                        world.playSound(null, openPosition.x, openPosition.y, openPosition.z,
                            AMSoundEvents.CARPET_TELEPORT, SoundCategory.NEUTRAL, 1.5F, 1F);
                    }
                }
            }
        }
    }

    private static MagicCarpetEntity spawnCarpetFromItem(World world, PlayerEntity player, ItemStack stack,
                                                         Direction side, Vec3d hitPos, int slot, boolean violent) {
        var carpetType = AMEntities.MAGIC_CARPET;
        MagicCarpetEntity magicCarpet = carpetType.create(world);
        // Carpet has been disabled somehow U_U
        if (magicCarpet == null) return null;

        // Offset on spawn to avoid clipping into the block we place it on
        var sideNormal = side.getUnitVector();
        var halfWidth = carpetType.getWidth() / 2.0;
        var height = side == Direction.DOWN ? -0.8 : 0;
        var spawnPos = hitPos.add(sideNormal.x * halfWidth, height, sideNormal.z * halfWidth);
        magicCarpet.setPosition(spawnPos);

        magicCarpet.readDataFromItemStack(stack);

        if (player != null) {
            // If the carpet isn't owned already then it's ours! Unless we're a dispenser or something
            if (!magicCarpet.hasOwner()) {
                magicCarpet.setOwner(player.getUuid());
            }

            // We want to remove it from the players hand/inventory even in creative otherwise it fills the empty hand you need to pick it up
            // and despite the existence of decrementIfNotCreative, decrement still doesn't work in creative
            player.getInventory().setStack(slot, ItemStack.EMPTY);
        }

        world.spawnEntity(magicCarpet);
        world.playSound(null, hitPos.x, hitPos.y, hitPos.z,
            violent ? AMSoundEvents.CARPET_UNFURL_VIOLENT : AMSoundEvents.CARPET_UNFURL, SoundCategory.NEUTRAL,
            violent ? 1.5F : 0.7F, 1F);

        return magicCarpet;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantability() {
        return 10;
    }
}
