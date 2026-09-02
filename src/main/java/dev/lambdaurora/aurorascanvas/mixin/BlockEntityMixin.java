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
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.compat.AurorasDecoDataUpper;
import dev.lambdaurora.aurorascanvas.compat.supplementaries.SupplementariesCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
	@WrapOperation(
			method = "loadStatic",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/core/Registry;getOptional(Lnet/minecraft/resources/Identifier;)Ljava/util/Optional;"
			)
	)
	private static Optional aurorascanvas$backwardsCompat(
			Registry<BlockEntity> instance, Identifier id, Operation<Optional> original, BlockPos pos, BlockState state, CompoundTag nbt
	) {
		if (id.getNamespace().equals(AurorasDecoDataUpper.OLD_NAMESPACE) && id.getPath().equals("blackboard")) {
			var source = new AurorasDecoDataUpper.FixSource.BlockEntity(id.toString(), pos);

			if (state.is(AurorasCanvasRegistry.GLASSBOARD.block().value()) || state.is(AurorasCanvasRegistry.GLASSBOARD.block().value())) {
				AurorasDecoDataUpper.fixGlassNbt(nbt, source);
				return Optional.of(AurorasCanvasRegistry.GLASS_CANVAS_BLOCK_ENTITY_TYPE);
			}

			AurorasDecoDataUpper.fixNbt(nbt, source);
			return Optional.of(AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
		} else if (SupplementariesCompat.SHOULD_DATAFIX && id.equals(SupplementariesCompat.BLACKBOARD_ID)) {
			SupplementariesCompat.fixNbt(nbt);
			return Optional.of(AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
		} else {
			return original.call(instance, id);
		}
	}
}
