package xyz.fancyteam.ajimaji.item;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ArmorBlockItem extends ArmorItem {
    private final Block block;

    public ArmorBlockItem(Block block, RegistryEntry<ArmorMaterial> material, Type type,
                          Settings settings) {
        super(material, type, settings);
        this.block = block;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player != null && player.isSneaking()) {
            ActionResult actionResult1 = this.place(new ItemPlacementContext(context));
            if (!actionResult1.isAccepted()) {
                ActionResult actionResult2 =
                    super.use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
                return actionResult2 == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : actionResult2;
            } else {
                return actionResult1;
            }
        } else {
            ActionResult actionResult2 =
                super.use(context.getWorld(), context.getPlayer(), context.getHand()).getResult();
            return actionResult2 == ActionResult.CONSUME ? ActionResult.CONSUME_PARTIAL : actionResult2;
        }
    }

    public ActionResult place(ItemPlacementContext context) {
        if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
            return ActionResult.FAIL;
        } else if (!context.canPlace()) {
            return ActionResult.FAIL;
        } else {
            ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
            if (itemPlacementContext == null) {
                return ActionResult.FAIL;
            } else {
                BlockState blockState = this.getPlacementState(itemPlacementContext);
                if (blockState == null) {
                    return ActionResult.FAIL;
                } else if (!this.place(itemPlacementContext, blockState)) {
                    return ActionResult.FAIL;
                } else {
                    BlockPos blockPos = itemPlacementContext.getBlockPos();
                    World world = itemPlacementContext.getWorld();
                    PlayerEntity playerEntity = itemPlacementContext.getPlayer();
                    ItemStack itemStack = itemPlacementContext.getStack();
                    BlockState blockState2 = world.getBlockState(blockPos);
                    if (blockState2.isOf(blockState.getBlock())) {
                        blockState2 = this.placeFromNbt(blockPos, world, itemStack, blockState2);
                        this.postPlacement(blockPos, world, playerEntity, itemStack, blockState2);
                        copyComponentsToBlockEntity(world, blockPos, itemStack);
                        blockState2.getBlock().onPlaced(world, blockPos, blockState2, playerEntity, itemStack);
                        if (playerEntity instanceof ServerPlayerEntity) {
                            Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity) playerEntity, blockPos, itemStack);
                        }
                    }

                    BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
                    world.playSound(
                        playerEntity,
                        blockPos,
                        this.getPlaceSound(blockState2),
                        SoundCategory.BLOCKS,
                        (blockSoundGroup.getVolume() + 1.0F) / 2.0F,
                        blockSoundGroup.getPitch() * 0.8F
                    );
                    world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos,
                        GameEvent.Emitter.of(playerEntity, blockState2));
                    itemStack.decrementUnlessCreative(1, playerEntity);
                    return ActionResult.success(world.isClient);
                }
            }
        }
    }

    protected SoundEvent getPlaceSound(BlockState state) {
        return state.getSoundGroup().getPlaceSound();
    }

    @Nullable
    public ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        return context;
    }

    private static void copyComponentsToBlockEntity(World world, BlockPos pos, ItemStack stack) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.readComponents(stack);
            blockEntity.markDirty();
        }
    }

    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack,
                                    BlockState state) {
        return writeNbtToBlockEntity(world, player, pos, stack);
    }

    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockState = this.getBlock().getPlacementState(context);
        return blockState != null && this.canPlace(context, blockState) ? blockState : null;
    }

    private BlockState placeFromNbt(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockStateComponent blockStateComponent =
            stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
        if (blockStateComponent.isEmpty()) {
            return state;
        } else {
            BlockState blockState = blockStateComponent.applyToState(state);
            if (blockState != state) {
                world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS);
            }

            return blockState;
        }
    }

    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        PlayerEntity playerEntity = context.getPlayer();
        ShapeContext shapeContext = playerEntity == null ? ShapeContext.absent() : ShapeContext.of(playerEntity);
        return (!this.checkStatePlacement() || state.canPlaceAt(context.getWorld(), context.getBlockPos()))
            && context.getWorld().canPlace(state, context.getBlockPos(), shapeContext);
    }

    protected boolean checkStatePlacement() {
        return true;
    }

    protected boolean place(ItemPlacementContext context, BlockState state) {
        return context.getWorld().setBlockState(context.getBlockPos(), state, Block.NOTIFY_ALL_AND_REDRAW);
    }

    public static boolean writeNbtToBlockEntity(World world, @Nullable PlayerEntity player, BlockPos pos,
                                                ItemStack stack) {
        MinecraftServer minecraftServer = world.getServer();
        if (minecraftServer == null) {
            return false;
        } else {
            NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT);
            if (!nbtComponent.isEmpty()) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity != null) {
                    if (world.isClient || !blockEntity.copyItemDataRequiresOperator() ||
                        player != null && player.isCreativeLevelTwoOp()) {
                        return nbtComponent.applyToBlockEntity(blockEntity, world.getRegistryManager());
                    }

                    return false;
                }
            }

            return false;
        }
    }

    @Override
    public String getTranslationKey() {
        return this.getBlock().getTranslationKey();
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        this.getBlock().appendTooltip(stack, context, tooltip, type);
    }

    public Block getBlock() {
        return this.block;
    }

    public void appendBlocks(Map<Block, Item> map, Item item) {
        map.put(this.getBlock(), item);
    }

    @Override
    public boolean canBeNested() {
        return !(this.getBlock() instanceof ShulkerBoxBlock);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ContainerComponent containerComponent =
            entity.getStack().set(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        if (containerComponent != null) {
            ItemUsage.spawnItemContents(entity, containerComponent.iterateNonEmptyCopy());
        }
    }

    public static void setBlockEntityData(ItemStack stack, BlockEntityType<?> type, NbtCompound nbt) {
        nbt.remove("id");
        if (nbt.isEmpty()) {
            stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
        } else {
            BlockEntity.writeIdToNbt(nbt, type);
            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(nbt));
        }
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.getBlock().getRequiredFeatures();
    }
}
