package xyz.fancyteam.ajimaji.block;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import xyz.fancyteam.ajimaji.AjiMaji;
import xyz.fancyteam.ajimaji.block_entity.TopHatBlockEntity;
import xyz.fancyteam.ajimaji.misc.AMDimensions;

public class TopHatBlock extends BlockWithEntity {
    private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union( //
        cuboid(3.0, 0.0, 3.0, 13.0, 8.0, 13.0), //
        cuboid(2.75, 8.0, 2.75, 13.25, 11, 13.25), //
        cuboid(0.5, 10.75, 0.5, 15.5, 11, 15.5) //
    );
    private static final VoxelShape COLLISION_SHAPE =
        VoxelShapes.combine(OUTLINE_SHAPE, cuboid(3.25, 0.25, 3.25, 12.75, 12.0, 12.75),
            BooleanBiFunction.ONLY_FIRST);
    private static final Box ITEM_SHAPE = box(3.25, 0.25, 3.25, 12.75, 8.0, 12.75);

    public static final MapCodec<TopHatBlock> MAP_CODEC = createCodec(TopHatBlock::new);

    private static VoxelShape cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return VoxelShapes.cuboid(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
    }

    private static Box box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return new Box(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
    }

    public TopHatBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return MAP_CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        // for wearing model
        builder.add(BooleanProperty.of("wearing"));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPE;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TopHatBlockEntity(pos, state);
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (world instanceof ServerWorld serverWorld) {
            ServerWorld topHatDim = serverWorld.getServer().getWorld(AMDimensions.TOP_HAT_DIMENSION);
            if (topHatDim == null) {
                AjiMaji.LOGGER.error("Missing top hat dimension {}", AMDimensions.TOP_HAT_DIMENSION);
                return;
            }

            BlockPos entryPoint =
                new BlockPos(world.random.nextInt(2000) - 1000, 256, world.random.nextInt(2000) - 1000);
            
            // TODO: entity tracking
            entity.teleportTo(
                new TeleportTarget(topHatDim, Vec3d.ofBottomCenter(entryPoint), Vec3d.ZERO, entity.getYaw(),
                    entity.getPitch(), newEntity -> {}));
        }
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (!world.isClient && entity instanceof ItemEntity itemEntity) {
            Vec3d itemPos = itemEntity.getPos().subtract(Vec3d.of(pos));
            if (ITEM_SHAPE.contains(itemPos)) {
                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof TopHatBlockEntity be) {
                    be.insertItem(itemEntity.getStack().copy());
                    itemEntity.discard();
                }
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (player.isSneaking()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof TopHatBlockEntity be) {
                if (world.isClient) return ActionResult.SUCCESS;
                be.dropAllStacks();
                return ActionResult.CONSUME;
            }
        }
        return ActionResult.FAIL;
    }
}
