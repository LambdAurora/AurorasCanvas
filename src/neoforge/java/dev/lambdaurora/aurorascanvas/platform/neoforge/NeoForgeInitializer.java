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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;

@Mod(value = AurorasCanvas.NAMESPACE)
public final class NeoForgeInitializer {
	public NeoForgeInitializer(ModContainer nativeContainer, IEventBus modBus) {
		modBus.addListener(ModifyRegistriesEvent.class, event -> {
			// Now we can stop doing manual block state cache init.
			NeoAurorasCanvas.doCacheInit = false;
		});
	}
}
