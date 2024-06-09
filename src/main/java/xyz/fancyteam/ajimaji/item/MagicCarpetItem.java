package xyz.fancyteam.ajimaji.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import xyz.fancyteam.ajimaji.entity.AMEntities;
import xyz.fancyteam.ajimaji.entity.MagicCarpetEntity;

public class MagicCarpetItem extends Item {
    public MagicCarpetItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if (!ctx.getWorld().isClient && !ctx.hitsInsideBlock()) {
            spawnCarpetFromUsageContext(ctx);
        }

        if (ctx.hitsInsideBlock()) {
            return ActionResult.PASS;
        } else {
            return ActionResult.SUCCESS;
        }
    }

    private static void spawnCarpetFromUsageContext(ItemUsageContext ctx) {
        var carpetType = AMEntities.MAGIC_CARPET;
        MagicCarpetEntity magicCarpet = carpetType.create(ctx.getWorld());
        // Carpet has been disabled somehow U_U
        if (magicCarpet == null) return;

        // Offset on spawn to avoid clipping into the block we place it on
        var sideNormal = ctx.getSide().getUnitVector();
        var halfWidth = carpetType.getWidth() / 2.0;
        var height = ctx.getSide() == Direction.DOWN ? -0.8 : 0;
        var spawnPos = ctx.getHitPos().add(sideNormal.x * halfWidth, height, sideNormal.z * halfWidth);
        magicCarpet.setPosition(spawnPos);

        var stack = ctx.getStack();
        magicCarpet.readDataFromItemStack(stack);

        if (ctx.getPlayer() != null) {
            // If the carpet isn't owned already then it's ours! Unless we're a dispenser or something
            if (!magicCarpet.hasOwner()) {
                magicCarpet.setOwner(ctx.getPlayer().getUuid());
            }

            // We want to remove it from the players hand even in creative otherwise it fills the empty hand you need to pick it up
            // and despite the existence of decrementIfNotCreative, decrement still doesn't work in creative
            ctx.getPlayer().setStackInHand(ctx.getHand(), ItemStack.EMPTY);
        }

        ctx.getWorld().spawnEntity(magicCarpet);
    }
}
