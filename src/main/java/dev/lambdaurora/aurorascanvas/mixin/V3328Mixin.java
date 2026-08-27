/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.mixin;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.V3328;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(V3328.class)
public class V3328Mixin {
	@Inject(method = "registerEntities", at = @At("RETURN"))
	private void aurorascanvas$onRegisterEntities(Schema schema, CallbackInfoReturnable<Map<String, Supplier<TypeTemplate>>> cir) {
		var map = cir.getReturnValue();

		schema.register(map, AurorasCanvas.NAMESPACE + ":easel", () -> DSL.optionalFields(
				"item",
				References.ITEM_STACK.in(schema)
		));
	}
}
