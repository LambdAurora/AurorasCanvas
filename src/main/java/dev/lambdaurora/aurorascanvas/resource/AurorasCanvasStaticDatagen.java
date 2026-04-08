/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.resource;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

public final class AurorasCanvasStaticDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(BlockTagProvider::new);
		pack.addProvider(ItemTagProvider::new);
		pack.addProvider(LootDataProvider::new);
		pack.addProvider(AdvancementProvider::new);
	}

	private static class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
		public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void addTags(HolderLookup.Provider arg) {
			this.tag(BLACKBOARD_BLOCKS)
					.add(
							BLACKBOARD.block().key(), WAXED_BLACKBOARD.block().key(),
							CHALKBOARD.block().key(), WAXED_CHALKBOARD.block().key()
					)
					.addTag(GLASSBOARD_BLOCKS);

			this.tag(GLASSBOARD_BLOCKS)
					.add(GLASSBOARD.block().key(), WAXED_GLASSBOARD.block().key());
		}
	}

	private static class ItemTagProvider extends FabricTagProvider.ItemTagProvider {
		public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void addTags(HolderLookup.Provider arg) {
			this.tag(BLACKBOARD_ITEMS)
					.add(
							BLACKBOARD.item().key(), WAXED_BLACKBOARD.item().key(),
							CHALKBOARD.item().key(), WAXED_CHALKBOARD.item().key(),
							GLASSBOARD.item().key(), WAXED_GLASSBOARD.item().key()
					);
		}
	}

	private static class LootDataProvider extends FabricBlockLootTableProvider {
		public LootDataProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generate() {
		}
	}

	private static class AdvancementProvider extends FabricAdvancementProvider {
		public AdvancementProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateAdvancement(Consumer<Advancement> consumer) {
			Advancement root = Advancement.Builder.advancement()
					.display(
							Items.MAP,
							Component.translatable("advancements.adventure.root.title"),
							Component.translatable("advancements.adventure.root.description"),
							new Identifier("textures/gui/advancements/backgrounds/adventure.png"),
							FrameType.TASK,
							false,
							false,
							false
					)
					.requirements(RequirementsStrategy.OR)
					.addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity())
					.addCriterion("killed_by_something", KilledTrigger.TriggerInstance.entityKilledPlayer())
					.build(new Identifier(Identifier.DEFAULT_NAMESPACE, "adventure/root"));
		}
	}
}
