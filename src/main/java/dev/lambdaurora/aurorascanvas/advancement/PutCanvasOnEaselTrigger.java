/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.advancement;

import com.google.gson.JsonObject;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import net.minecraft.advancements.criterion.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PutCanvasOnEaselTrigger extends SimpleCriterionTrigger<PutCanvasOnEaselTrigger.TriggerInstance> {
	public static final Identifier ID = AurorasCanvas.id("put_canvas_on_easel");

	@Override
	public Identifier getId() {
		return ID;
	}

	public TriggerInstance createInstance(
			JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate easelPredicate = EntityPredicate.fromJson(jsonObject, "easel", deserializationContext);
		ItemPredicate canvasPredicate = ItemPredicate.fromJson(jsonObject.get("canvas"));
		return new TriggerInstance(contextAwarePredicate, easelPredicate, canvasPredicate);
	}

	public void trigger(ServerPlayer player, EaselEntity easel, ItemStack tool) {
		LootContext easelLootContext = EntityPredicate.createContext(player, easel);
		this.trigger(player, triggerInstance -> triggerInstance.matches(easelLootContext, tool));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ContextAwarePredicate easelPredicate;
		private final ItemPredicate toolPredicate;

		public TriggerInstance(ContextAwarePredicate player, ContextAwarePredicate easelPredicate, ItemPredicate canvasPredicate) {
			super(PutCanvasOnEaselTrigger.ID, player);
			this.easelPredicate = easelPredicate;
			this.toolPredicate = canvasPredicate;
		}

		public static PutCanvasOnEaselTrigger.TriggerInstance put(ItemPredicate canvasPredicate) {
			return new PutCanvasOnEaselTrigger.TriggerInstance(
					ContextAwarePredicate.ANY,
					ContextAwarePredicate.ANY,
					canvasPredicate
			);
		}

		public boolean matches(LootContext easel, ItemStack tool) {
			return this.easelPredicate.matches(easel) && this.toolPredicate.matches(tool);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject json = super.serializeToJson(serializationContext);
			json.add("canvas", this.toolPredicate.serializeToJson());
			return json;
		}
	}
}
