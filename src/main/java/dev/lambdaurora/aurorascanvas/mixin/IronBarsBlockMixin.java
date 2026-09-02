/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IronBarsBlock.class)
public class IronBarsBlockMixin {
	@WrapOperation(
			method = "getStateForPlacement",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/state/BlockState;isFaceSturdy(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"
			)
	)
	private boolean aurorascanvas$onGetStateForPlacement(
			BlockState targetState, BlockGetter level, BlockPos pos, Direction direction, Operation<Boolean> original
	) {
		if (targetState.getBlock() instanceof GlassCanvasBlock
				&& targetState.getValue(GlassCanvasBlock.PANE)
				&& targetState.getValue(GlassCanvasBlock.FACING).getClockWise().getAxis() == direction.getAxis()) {
			return true;
		}

		return original.call(targetState, level, pos, direction);
	}

	@WrapOperation(
			method = "updateShape",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/IronBarsBlock;attachsTo(Lnet/minecraft/world/level/block/state/BlockState;Z)Z"
			)
	)
	private boolean aurorascanvas$onUpdateShape$attachsTo(
			IronBarsBlock instance, BlockState targetState, boolean faceSolid,
			Operation<Boolean> original,
			BlockState state, @Local(name = "directionToNeighbour", argsOnly = true) Direction directionToNeighbour
	) {
		if (targetState.getBlock() instanceof GlassCanvasBlock
				&& targetState.getValue(GlassCanvasBlock.PANE)
				&& targetState.getValue(GlassCanvasBlock.FACING).getClockWise().getAxis() == directionToNeighbour.getAxis()) {
			return true;
		}

		return original.call(instance, targetState, faceSolid);
	}
}
