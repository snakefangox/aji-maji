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
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import xyz.fancyteam.ajimaji.block_entity.TopHatBlockEntity;

public class TopHatBlock extends BlockWithEntity {
    private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union( //
        cuboid(3.0, 0.0, 3.0, 13.0, 8.0, 13.0), //
        cuboid(2.75, 8.0, 2.75, 13.25, 11, 13.25), //
        cuboid(0.5, 10.75, 0.5, 15.5, 11, 15.5) //
    );
    private static final VoxelShape COLLISION_SHAPE =
        VoxelShapes.combine(OUTLINE_SHAPE, cuboid(3.25, 0.25, 3.25, 12.75, 12.0, 12.75),
            BooleanBiFunction.ONLY_FIRST);

    public static final MapCodec<TopHatBlock> MAP_CODEC = createCodec(TopHatBlock::new);

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

    private static VoxelShape cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return VoxelShapes.cuboid(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
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
        // :3
        super.onLandedUpon(world, state, pos, entity, fallDistance);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        // TODO: item insertion & crafting
        super.onEntityCollision(state, world, pos, entity);
    }
}
