/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.platform.neoforge;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.yumi.mc.core.api.ModContainer;
import dev.yumi.mc.core.api.entrypoint.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class NeoAurorasCanvas implements ModInitializer {
	static boolean doCacheInit = true;

	@Override
	public void onInitialize(ModContainer mod) {
		RegistryEntryAddedCallback.allEntries(BuiltInRegistries.BLOCK, ref -> {
			if (doCacheInit && ref.key().identifier().getNamespace().equals(AurorasCanvas.NAMESPACE)) {
				this.doStateCacheInit(ref.value());
			}
		});
	}

	private void doStateCacheInit(Block block) {
		for (BlockState state : block.getStateDefinition().getPossibleStates()) {
			state.initCache();
		}
	}
}
