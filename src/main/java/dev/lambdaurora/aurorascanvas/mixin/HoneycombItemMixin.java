/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
	@WrapMethod(
			method = "method_34719(Lnet/minecraft/world/item/context/UseOnContext;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/InteractionResult;"
	)
	private static InteractionResult aurorascanvas$onWax(UseOnContext useOnContext, BlockPos pos, Level level, BlockState state, Operation<InteractionResult> original) {
		CompoundTag blockEntityData = null;

		if (state.getBlock() instanceof CanvasBlock block) {
			var blockEntity = block.getCanvasEntity(level, pos);
			if (blockEntity != null && !(blockEntity.isEmpty() && !blockEntity.hasCustomName())) {
				blockEntityData = new CompoundTag();
				blockEntity.saveAdditional(blockEntityData);
			}
		}

		var result = original.call(useOnContext, pos, level, state);

		if (state.getBlock() instanceof CanvasBlock block) {
			var blockEntity = block.getCanvasEntity(level, pos);
			if (blockEntity != null && blockEntityData != null) {
				blockEntity.load(blockEntityData);
				blockEntity.setChanged();
			}
		}

		return result;
	}
}
