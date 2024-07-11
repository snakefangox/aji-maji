package xyz.fancyteam.ajimaji.item;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import me.lucko.fabric.api.permissions.v0.Permissions;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.component.TopHatIdComponent;
import xyz.fancyteam.ajimaji.top_hat.TopHatManager;

import static xyz.fancyteam.ajimaji.AjiMaji.tt;

public class TopHatBlockItem extends ArmorBlockItem {
    public TopHatBlockItem(Block block, RegistryEntry<ArmorMaterial> material,
                           Settings settings) {
        super(block, material, Type.HELMET, settings);
    }

    @Override
    public void postProcessComponents(ItemStack stack) {
        TopHatIdComponent.getOrCreate(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || !player.isSneaking()) {
            if (context.getWorld() instanceof ServerWorld world) {
                Vec3d entityPos;
                if (context.getSide() == Direction.UP) {
                    entityPos = context.getHitPos();
                } else {
                    entityPos = Vec3d.ofBottomCenter(context.getBlockPos().offset(context.getSide()));
                }

                return retrieveEntity(context.getStack(), world, entityPos);
            } else {
                return ActionResult.CONSUME;
            }
        }
        return super.useOnBlock(context);
    }

    public static @NotNull ActionResult retrieveEntity(ItemStack stack, ServerWorld world, Vec3d entityPos) {
        TopHatIdComponent component = TopHatIdComponent.getOrCreate(stack);

        boolean result =
            TopHatManager.retrieveEntity(world.getServer(), component.topHatId(),
                new TeleportTarget(world, entityPos, Vec3d.ZERO, 0f, 0f, entity -> {}));
        return result ? ActionResult.SUCCESS : ActionResult.FAIL;
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        return useOnEntity(stack, user, entity);
    }

    public static ActionResult useOnEntity(ItemStack stack, PlayerEntity user, Entity entity) {
        if (!entity.getWorld().isClient) {
            if (Permissions.check(user, AjiMaji.FORCE_USE_TOP_HAT_PERM, AjiMaji.FORCE_USE_TOP_HAT_PERM_DEFAULT)) {
                return insertEntity(stack, entity);
            } else if (entity instanceof PlayerEntity playerEntity &&
                Permissions.check(user, AjiMaji.USE_TOP_HAT_ON_PLAYERS_PERM,
                    AjiMaji.USE_TOP_HAT_ON_PLAYERS_PERM_DEFAULT)) {
                if (playerEntity.getInventory().getArmorStack(3).isOf(AMItems.BUNNY_EARS)) {
                    return insertEntity(stack, entity);
                }
            } else if (Permissions.check(user, AjiMaji.USE_TOP_HAT_ON_ENTITIES_PERM,
                AjiMaji.USE_TOP_HAT_ON_ENTITIES_PERM_DEFAULT)) {
                return insertEntity(stack, entity);
            }
        }
        return ActionResult.CONSUME;
    }

    public static ActionResult insertEntity(ItemStack stack, Entity entity) {
        if (entity.getWorld() instanceof ServerWorld world) {
            TopHatIdComponent component = TopHatIdComponent.getOrCreate(stack);
            TopHatManager.insertEntity(world.getServer(), component.topHatId(), entity);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        if (type.isAdvanced()) {
            TopHatIdComponent component = stack.get(AMDataComponents.TOP_HAT_ID);
            Text id;
            if (component == null) {
                id = tt("tooltip", "top_hat.id.none").formatted(Formatting.RED);
            } else {
                id = Text.literal(component.topHatId().toString()).formatted(Formatting.GREEN);
            }
            tooltip.add(tt("tooltip", "top_hat.id", id));
        }
    }
}
