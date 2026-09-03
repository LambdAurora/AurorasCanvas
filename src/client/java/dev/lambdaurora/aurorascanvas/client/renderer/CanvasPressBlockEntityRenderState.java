/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/// Represents the canvas press block entity render state.
///
/// @author LambdAurora
/// @version 1.2.0
/// @since 1.2.0
@Environment(EnvType.CLIENT)
public class CanvasPressBlockEntityRenderState extends BlockEntityRenderState {
	public final BlockModelRenderState pressPlateModel = new BlockModelRenderState();
	public final BlockModelRenderState screwModel = new BlockModelRenderState();
	public long gameTime = 0;
}
