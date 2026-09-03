/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.lambdaurora.aurorascanvas.client.model.AurorasCanvasBlockStateDefinitions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(BlockStateDefinitions.class)
public class BlockStateDefinitionsMixin {
	@WrapOperation(
			method = "definitionLocationToBlockStateMapper",
			at = @At(value = "NEW", target = "(Ljava/util/Map;)Ljava/util/HashMap;")
	)
	private static HashMap<Identifier, StateDefinition<Block, BlockState>> aurorascanvas$wrapStaticDefinitions(
			Map<Identifier, StateDefinition<Block, BlockState>> staticDefinitions,
			Operation<HashMap<Identifier, StateDefinition<Block, BlockState>>> operation
	) {
		var map = operation.call(staticDefinitions);
		map.putAll(AurorasCanvasBlockStateDefinitions.STATIC_DEFINITIONS);
		return map;
	}
}
