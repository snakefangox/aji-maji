package xyz.fancyteam.ajimaji.item;

import java.util.List;

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

import xyz.fancyteam.ajimaji.component.AMDataComponents;
import xyz.fancyteam.ajimaji.component.TopHatIdComponent;
import xyz.fancyteam.ajimaji.top_hat.TopHatManager;
import xyz.fancyteam.ajimaji.top_hat.TopHatRetrieveListener;

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
                TopHatIdComponent component = TopHatIdComponent.getOrCreate(context.getStack());
                boolean result =
                    TopHatManager.retrieveEntity(world.getServer(), component.topHatId(), world, context.getHitPos(),
                        new TopHatRetrieveListener() {
                            @Override
                            public void acceptRetrieved(Entity retrieved) {
                                // TODO: particle effects
                            }

                            @Override
                            public void notifyMissing() {
                                if (player != null) {
                                    player.sendMessage(tt("msg", "top_hat.entity_missing"), true);
                                }
                            }
                        });
                return result ? ActionResult.SUCCESS : ActionResult.FAIL;
            } else {
                return ActionResult.CONSUME;
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
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
