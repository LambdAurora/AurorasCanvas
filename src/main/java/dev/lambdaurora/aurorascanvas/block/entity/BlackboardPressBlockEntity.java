package dev.lambdaurora.aurorascanvas.block.entity;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlackboardPressBlockEntity extends BasicBlockEntity {
	public BlackboardPressBlockEntity(BlockPos pos, BlockState blockState) {
		super(AurorasCanvasRegistry.BLACKBOARD_PRESS_BLOCK_ENTITY, pos, blockState);
	}
}
