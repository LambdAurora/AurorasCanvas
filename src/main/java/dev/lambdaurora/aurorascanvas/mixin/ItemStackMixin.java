/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.mixin;

import dev.lambdaurora.aurorascanvas.AurorasCanvasIds;
import dev.lambdaurora.aurorascanvas.compat.supplementaries.SupplementariesCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
	@Inject(method = "of", at = @At(value = "HEAD"))
	private static void aurorascanvas$of(CompoundTag nbt, CallbackInfoReturnable<ItemStack> cir) {
		if (SupplementariesCompat.SHOULD_DATAFIX) {
			if (nbt.getString("id").equals(SupplementariesCompat.NAMESPACE + ":blackboard")) {
				nbt.putString("id", AurorasCanvasIds.BLACKBOARD_ID.toString());
				if (nbt.contains("tag", Tag.TAG_COMPOUND)) {
					var tag = nbt.getCompound("tag");
					if (tag.contains(BlockItem.BLOCK_ENTITY_TAG, Tag.TAG_COMPOUND)) {
						var blockEntityTag = tag.getCompound(BlockItem.BLOCK_ENTITY_TAG);
						SupplementariesCompat.fixNbt(blockEntityTag);
					}
				}
			}
		}
	}
}
