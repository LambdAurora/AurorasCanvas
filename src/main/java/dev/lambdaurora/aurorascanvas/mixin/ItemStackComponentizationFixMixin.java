/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.mixin;

import com.mojang.serialization.Dynamic;
import dev.lambdaurora.aurorascanvas.compat.ItemStackComponentizationFixer;
import net.minecraft.util.datafix.fixes.ItemStackComponentizationFix;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStackComponentizationFix.class)
public class ItemStackComponentizationFixMixin {
	@Inject(
			method = "fixItemStack",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/datafix/fixes/ItemStackComponentizationFix$ItemStackData;moveTagToComponent(Ljava/lang/String;Ljava/lang/String;)V",
					ordinal = 2
			)
	)
	private static void aurorascanvas$onFixItemStack(ItemStackComponentizationFix.ItemStackData itemStack, Dynamic<?> dynamic, CallbackInfo ci) {
		ItemStackComponentizationFixer.fixItemStack(itemStack, dynamic);
	}
}
