/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.test;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.state.BlockState;

class TestHelper {
	/**
	 * Expects the given block state at the given block position.
	 *
	 * @param state the expected block state
	 * @param pos   the position to check for
	 */
	static void assertBlockState(GameTestHelper context, BlockState state, BlockPos pos) {
		context.assertBlockState(
				pos,
				s -> s.equals(state),
				() -> "Expected block state " + state + " at position " + pos.toShortString() + '.'
		);
	}
}
