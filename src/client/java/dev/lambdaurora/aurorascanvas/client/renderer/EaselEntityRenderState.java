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
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

/// Represents the Easel entity render state.
///
/// @author LambdAurora
/// @version 1.2.0
/// @since 1.2.0
@Environment(EnvType.CLIENT)
public class EaselEntityRenderState extends LivingEntityRenderState {
	public final ItemStackRenderState item = new ItemStackRenderState();
	public float wiggle;
}
