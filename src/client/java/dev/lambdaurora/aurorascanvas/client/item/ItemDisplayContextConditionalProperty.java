/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/// Represents a conditional item model property to target an item display context.
///
/// @author LambdAurora
/// @version 1.2.0
/// @since 1.2.0
public record ItemDisplayContextConditionalProperty(ItemDisplayContext target) implements ConditionalItemModelProperty {
	public static final MapCodec<ItemDisplayContextConditionalProperty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			ItemDisplayContext.CODEC.fieldOf("target").forGetter(ItemDisplayContextConditionalProperty::target)
	).apply(instance, ItemDisplayContextConditionalProperty::new));

	@Override
	public MapCodec<? extends ConditionalItemModelProperty> type() {
		return CODEC;
	}

	@Override
	public boolean get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
		return displayContext == this.target;
	}
}
