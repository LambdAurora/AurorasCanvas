/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class AurorasCanvasSoundEvents {
	public static final SoundEvent EASEL_BREAK = register(AurorasCanvas.id("entity.aurorascanvas.easel.break"));
	public static final SoundEvent EASEL_FALL = register(AurorasCanvas.id("entity.aurorascanvas.easel.fall"));
	public static final SoundEvent EASEL_HIT = register(AurorasCanvas.id("entity.aurorascanvas.easel.hit"));
	public static final SoundEvent EASEL_PLACE = register(AurorasCanvas.id("entity.aurorascanvas.easel.place"));

	private AurorasCanvasSoundEvents() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions");
	}

	private static SoundEvent register(Identifier id) {
		return register(id, id);
	}

	private static SoundEvent register(Identifier name, Identifier location) {
		return Registry.register(BuiltInRegistries.SOUND_EVENT, name, SoundEvent.createVariableRangeEvent(location));
	}

	static void init() {
		// To make sure the class initializes at the right time.
	}
}
