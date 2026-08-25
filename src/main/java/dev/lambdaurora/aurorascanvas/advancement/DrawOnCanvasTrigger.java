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
import net.minecraft.advancements.criterion.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class DrawOnCanvasTrigger extends SimpleCriterionTrigger<DrawOnCanvasTrigger.TriggerInstance> {
	public static final Identifier ID = AurorasCanvas.id("draw_on_canvas");

	@Override
	public Identifier getId() {
		return ID;
	}

	public TriggerInstance createInstance(
			JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ItemPredicate toolPredicate = ItemPredicate.fromJson(jsonObject.get("tool"));
		return new TriggerInstance(contextAwarePredicate, toolPredicate);
	}

	public void trigger(ServerPlayer player, ItemStack tool) {
		this.trigger(player, triggerInstance -> triggerInstance.matches(tool));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final ItemPredicate toolPredicate;

		public TriggerInstance(ContextAwarePredicate player, ItemPredicate toolPredicate) {
			super(DrawOnCanvasTrigger.ID, player);
			this.toolPredicate = toolPredicate;
		}

		public static DrawOnCanvasTrigger.TriggerInstance drawAny() {
			return new DrawOnCanvasTrigger.TriggerInstance(ContextAwarePredicate.ANY, ItemPredicate.ANY);
		}

		public boolean matches(ItemStack tool) {
			return this.toolPredicate.matches(tool);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject json = super.serializeToJson(serializationContext);
			json.add("tool", this.toolPredicate.serializeToJson());
			return json;
		}
	}
}
