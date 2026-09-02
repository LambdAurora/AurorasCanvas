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
import com.mojang.serialization.Codec;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.compat.AurorasDecoDataUpper;
import dev.lambdaurora.aurorascanvas.compat.supplementaries.SupplementariesCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
					target = "Lnet/minecraft/nbt/CompoundTag;read(Ljava/lang/String;Lcom/mojang/serialization/Codec;)Ljava/util/Optional;"
			)
	)
	private static Optional<BlockEntityType<?>> aurorascanvas$backwardsCompat(
			CompoundTag instance, String key, Codec<?> codec, Operation<Optional<BlockEntityType<?>>> original, BlockPos pos, BlockState state, final CompoundTag nbt
	) {
		return original.call(instance, key, codec).or(() -> instance.getString(key).map(Identifier::tryParse).flatMap(id -> {
			if (id.getNamespace().equals(AurorasDecoDataUpper.OLD_NAMESPACE) && id.getPath().equals("blackboard")) {
				var source = new AurorasDecoDataUpper.FixSource.BlockEntity(id.toString(), pos);

				if (state.is(AurorasCanvasRegistry.GLASSBOARD.block().value()) || state.is(AurorasCanvasRegistry.GLASSBOARD.block().value())) {
					AurorasDecoDataUpper.fixGlassNbt(nbt, source);
					return Optional.of(AurorasCanvasRegistry.GLASS_CANVAS_BLOCK_ENTITY_TYPE);
				}

				AurorasDecoDataUpper.fixNbt(nbt, source);
				return Optional.of(AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
			} else if (SupplementariesCompat.SHOULD_DATAFIX && id.equals(SupplementariesCompat.BLACKBOARD_ID)) {
				SupplementariesCompat.fixNbt(instance);
				return Optional.of(AurorasCanvasRegistry.CANVAS_BLOCK_ENTITY_TYPE);
			} else {
				return Optional.empty();
			}
		}));
	}
}
