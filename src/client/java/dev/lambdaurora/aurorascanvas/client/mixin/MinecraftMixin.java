/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.mixin;

import dev.lambdaurora.aurorascanvas.client.AurorasCanvasClient;
import dev.lambdaurora.aurorascanvas.client.ClientCanvasBlockEntityData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(method = "setLevel", at = @At("HEAD"))
	private void aurorascanvas$onSetLevel(ClientLevel level, CallbackInfo ci) {
		ClientCanvasBlockEntityData.onLevelChange(level);
	}

	@Inject(method = "close", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/MapTextureManager;close()V"))
	private void aurorascanvas$onClose(CallbackInfo ci) {
		AurorasCanvasClient.CANVAS_TEXTURE_MANAGER.close();
	}
}
