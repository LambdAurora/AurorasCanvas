/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.mixin;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Identifier.class)
public class IdentifierMixin {
	@Mutable
	@Shadow
	@Final
	private String namespace;

	@Shadow
	@Final
	private String path;

	@Inject(
			method = "<init>(Ljava/lang/String;Ljava/lang/String;Lnet/minecraft/resources/Identifier$Dummy;)V",
			at = @At("TAIL")
	)
	private void aurorascanvas$onInit(String namespace, String path, @Coerce Object dummy, CallbackInfo ci) {
		if (this.namespace.equals("aurorasdeco")) {
			if (this.path.equals("canvas")) {
				this.namespace = AurorasCanvas.NAMESPACE;
			}
		}
	}
}
