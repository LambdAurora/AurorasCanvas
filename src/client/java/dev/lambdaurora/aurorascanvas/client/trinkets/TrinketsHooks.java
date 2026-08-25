/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.trinkets;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

public final class TrinketsHooks implements ClientModInitializer {
	private static final boolean HAS_TRINKETS = FabricLoader.getInstance().isModLoaded("trinkets");

	@Override
	public void onInitializeClient() {
		init(
				BLACKBOARD.asItem(), WAXED_BLACKBOARD.asItem(),
				CHALKBOARD.asItem(), WAXED_CHALKBOARD.asItem(),
				WHITEBOARD.asItem(), WAXED_WHITEBOARD.asItem(),
				GLASSBOARD.asItem(), WAXED_GLASSBOARD.asItem()
		);
	}

	public static void init(Item... blackboards) {
		if (!HAS_TRINKETS)
			return;

		for (var item : blackboards)
			TrinketRendererRegistry.registerRenderer(item, TrinketsHooks::renderCanvasInTrinketsSlot);
	}

	private static void renderCanvasInTrinketsSlot(
			ItemStack stack, SlotReference slotReference,
			EntityModel<? extends LivingEntity> contextModel,
			PoseStack matrices, MultiBufferSource vertexConsumers, int light,
			LivingEntity entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress,
			float headYaw, float headPitch
	) {
		if (!slotReference.inventory().getSlotType().getGroup().equals("head"))
			return;

		boolean villager = entity instanceof Villager || entity instanceof ZombieVillager;
		if (entity.isBaby() && !(entity instanceof Villager)) {
			matrices.translate(0.0, 0.03125, 0.0);
			matrices.scale(.7f, .7f, .7f);
			matrices.translate(0.0, 1.0, 0.0);
		}
		if (contextModel instanceof HeadedModel withHead)
			withHead.getHead().translateAndRotate(matrices);

		CustomHeadLayer.translateToHead(matrices, villager);
		Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
				entity, stack, ItemDisplayContext.HEAD,
				false,
				matrices, vertexConsumers, light
		);
	}
}
