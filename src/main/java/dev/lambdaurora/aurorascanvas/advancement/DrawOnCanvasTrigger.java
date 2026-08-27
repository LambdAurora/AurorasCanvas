/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class DrawOnCanvasTrigger extends SimpleCriterionTrigger<DrawOnCanvasTrigger.TriggerInstance> {
	public static final Identifier ID = AurorasCanvas.id("draw_on_canvas");

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, ItemStack tool) {
		this.trigger(player, triggerInstance -> triggerInstance.matches(tool));
	}

	public record TriggerInstance(
			Optional<ContextAwarePredicate> player,
			Optional<ItemPredicate> toolPredicate
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				ItemPredicate.CODEC.optionalFieldOf("tool").forGetter(TriggerInstance::toolPredicate)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> drawAny() {
			return AurorasCanvasRegistry.DRAW_ON_CANVAS_TRIGGER.createCriterion(
					new TriggerInstance(Optional.empty(), Optional.empty())
			);
		}

		public boolean matches(ItemStack tool) {
			return this.toolPredicate.isEmpty() || this.toolPredicate.get().test(tool);
		}
	}
}
