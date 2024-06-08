package xyz.fancyteam.ajimaji.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class TopHatBlock extends Block {
    private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union( //
        cuboid(3.0, 0.0, 3.0, 13.0, 8.0, 13.0), //
        cuboid(2.75, 8.0, 2.75, 13.25, 11, 13.25), //
        cuboid(0.5, 10.75, 0.5, 15.5, 11, 15.5) //
    );
    private static final VoxelShape COLLISION_SHAPE =
        VoxelShapes.combine(OUTLINE_SHAPE, cuboid(3.25, 0.25, 3.25, 12.75, 12.0, 12.75),
            BooleanBiFunction.ONLY_FIRST);

    public TopHatBlock(Settings settings) {
        super(settings);
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
}
