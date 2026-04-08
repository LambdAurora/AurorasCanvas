package dev.lambdaurora.aurorascanvas.client.mixin;

import dev.lambdaurora.aurorascanvas.client.ClientBlackboardBlockEntityData;
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
		ClientBlackboardBlockEntityData.onLevelChange(level);
	}
}
