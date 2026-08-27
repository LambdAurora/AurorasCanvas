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
import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Optional;

public class PutCanvasOnEaselTrigger extends SimpleCriterionTrigger<PutCanvasOnEaselTrigger.TriggerInstance> {
	public static final Identifier ID = AurorasCanvas.id("put_canvas_on_easel");

	@Override
	public Codec<TriggerInstance> codec() {
		return TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer player, EaselEntity easel, ItemStack tool) {
		LootContext easelLootContext = EntityPredicate.createContext(player, easel);
		this.trigger(player, triggerInstance -> triggerInstance.matches(easelLootContext, tool));
	}

	public record TriggerInstance(
			Optional<ContextAwarePredicate> player,
			Optional<ContextAwarePredicate> easel,
			Optional<ItemPredicate> canvas
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("easel").forGetter(TriggerInstance::easel),
				ItemPredicate.CODEC.optionalFieldOf("canvas").forGetter(TriggerInstance::canvas)
		).apply(instance, TriggerInstance::new));

		public static Criterion<TriggerInstance> put(ItemPredicate canvasPredicate) {
			return AurorasCanvasRegistry.PUT_CANVAS_ON_EASEL_TRIGGER.createCriterion(
					new TriggerInstance(
							Optional.empty(),
							Optional.empty(),
							Optional.of(canvasPredicate)
					)
			);
		}

		public boolean matches(LootContext easel, ItemStack tool) {
			return (this.easel.isEmpty() || this.easel.get().matches(easel)) && (this.canvas.isEmpty() || this.canvas.get().test(tool));
		}
	}
}
