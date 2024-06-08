package xyz.fancyteam.ajimaji.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;

public class TopHatBlock extends Block {
    public TopHatBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        // for wearing model
        builder.add(BooleanProperty.of("wearing"));
    }
}
