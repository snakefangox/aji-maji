package xyz.fancyteam.ajimaji.item;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import xyz.fancyteam.ajimaji.block.AMBlocks;
import xyz.fancyteam.ajimaji.misc.AMArmorMaterials;

import static xyz.fancyteam.ajimaji.AjiMaji.id;

public class AMItems {
    public static final List<ItemStack> CREATIVE_TAB_ITEMS = new ArrayList<>();

    public static final TopHatBlockItem TOP_HAT =
        new TopHatBlockItem(AMBlocks.TOP_HAT, AMArmorMaterials.MAGICIANS_ARMOR_MATERIAL,
            new Item.Settings().maxCount(1));
    public static final MagicCarpetItem MAGIC_CARPET =
        new MagicCarpetItem(new Item.Settings().maxCount(1));
    public static final CardDeckItem CARD_DECK =
        new CardDeckItem(new Item.Settings());
    public static final BlockItem CARD_BOX =
        new BlockItem(AMBlocks.CARD_BOX, new Item.Settings());
    public static final Item PLAYING_CARD =
        new Item(new Item.Settings());

    public static void register() {
        register("top_hat", TOP_HAT);
        register("magic_carpet", MAGIC_CARPET);
        register("card_deck", CARD_DECK);
        register("card_box", CARD_BOX);
        register("playing_card", PLAYING_CARD);

        DispenserBlock.registerBehavior(TOP_HAT, new ItemDispenserBehavior() {
            @Override
            protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
                Direction direction = pointer.state().get(DispenserBlock.FACING);
                BlockPos pos = pointer.pos().offset(direction);
                ServerWorld world = pointer.world();

                List<LivingEntity> entities =
                    world.getNonSpectatingEntities(LivingEntity.class, Box.of(Vec3d.ofCenter(pos), 1.0, 1.0, 1.0));
                if (!entities.isEmpty()) {
                    for (LivingEntity entity : entities) {
                        TopHatBlockItem.insertEntity(stack, entity);
                    }
                } else {
                    TopHatBlockItem.retrieveEntity(stack, world, Vec3d.ofBottomCenter(pos));
                }

                return stack;
            }
        });
    }

    public static void register(String path, Item item) {
        Registry.register(Registries.ITEM, id(path), item);
        CREATIVE_TAB_ITEMS.add(item.getDefaultStack());
    }
}
