/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.resource;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry.*;

public final class AurorasCanvasStaticDatagen implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		pack.addProvider(BlockTagProvider::new);
		pack.addProvider(ItemTagProvider::new);
		pack.addProvider(LootDataProvider::new);
		pack.addProvider(AdvancementProvider::new);
		pack.addProvider(AurorasRecipeProvider::new);
	}

	private static class BlockTagProvider extends FabricTagProvider.BlockTagProvider {
		public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void addTags(HolderLookup.Provider arg) {
			this.tag(CANVAS_BLOCKS)
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
			this.tag(CANVAS_ITEMS)
					.add(
							BLACKBOARD.item().key(), WAXED_BLACKBOARD.item().key(),
							CHALKBOARD.item().key(), WAXED_CHALKBOARD.item().key(),
							GLASSBOARD.item().key(), WAXED_GLASSBOARD.item().key()
					);
		}
	}

	private static class LootDataProvider extends FabricBlockLootTableProvider {
		protected final Set<Item> explosionResistant = Stream.of(
				BLACKBOARD,
				WAXED_BLACKBOARD,
				CHALKBOARD,
				WAXED_CHALKBOARD,
				GLASSBOARD,
				WAXED_GLASSBOARD
		).map(ItemLike::asItem).collect(Collectors.toUnmodifiableSet());

		public LootDataProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike item, FunctionUserBuilder<T> functionBuilder) {
			return !this.explosionResistant.contains(item.asItem()) ? functionBuilder.apply(ApplyExplosionDecay.explosionDecay()) : functionBuilder.unwrap();
		}

		@Override
		public <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike item, ConditionUserBuilder<T> conditionBuilder) {
			return !this.explosionResistant.contains(item.asItem()) ? conditionBuilder.when(ExplosionCondition.survivesExplosion()) : conditionBuilder.unwrap();
		}

		@Override
		public void generate() {
			this.add(BLACKBOARD.block().value(), this::createCanvasDrop);
			this.add(WAXED_BLACKBOARD.block().value(), this::createCanvasDrop);
			this.add(CHALKBOARD.block().value(), this::createCanvasDrop);
			this.add(WAXED_CHALKBOARD.block().value(), this::createCanvasDrop);
			this.add(GLASSBOARD.block().value(), this::createGlassCanvasDrop);
			this.add(WAXED_GLASSBOARD.block().value(), this::createGlassCanvasDrop);
			this.add(CANVAS_PRESS.block().value(), this::createSingleItemTable);
		}

		private LootTable.Builder createCanvasDrop(Block block) {
			return LootTable.lootTable()
					.withPool(
							this.applyExplosionCondition(
									block,
									LootPool.lootPool()
											.setRolls(ConstantValue.exactly(1.f))
											.add(
													LootItem.lootTableItem(block)
															.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
															.apply(
																	CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
																			.copy("pixels", "BlockEntityTag.pixels")
																			.copy("version", "BlockEntityTag.version")
																			.copy("lit", "BlockEntityTag.lit")
															)
											)
							)
					);
		}

		private LootTable.Builder createGlassCanvasDrop(Block block) {
			return LootTable.lootTable()
					.withPool(
							this.applyExplosionCondition(
									block,
									LootPool.lootPool()
											.setRolls(ConstantValue.exactly(1.f))
											.add(
													LootItem.lootTableItem(block)
															.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
															.apply(
																	CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
																			.copy("front", "BlockEntityTag.front")
																			.copy("back", "BlockEntityTag.back")
															)
											)
							)
					);
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

	private static class AurorasRecipeProvider extends FabricRecipeProvider {
		public AurorasRecipeProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void buildRecipes(Consumer<FinishedRecipe> exporter) {
			ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BLACKBOARD)
					.group(CANVAS_ITEMS.location().toString())
					.define('S', Items.STICK)
					.define('C', Items.BLACK_CONCRETE)
					.pattern("SSS")
					.pattern("SCS")
					.pattern("SSS")
					.unlockedBy("has_stick", has(Items.STICK))
					.unlockedBy("has_concrete", has(Items.BLACK_CONCRETE))
					.unlockedBy("has_self", has(CANVAS_ITEMS))
					.save(exporter);
			ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, CHALKBOARD)
					.group(CANVAS_ITEMS.location().toString())
					.define('S', Items.STICK)
					.define('C', Items.GREEN_CONCRETE)
					.pattern("SSS")
					.pattern("SCS")
					.pattern("SSS")
					.unlockedBy("has_stick", has(Items.STICK))
					.unlockedBy("has_concrete", has(Items.GREEN_CONCRETE))
					.unlockedBy("has_self", has(CANVAS_ITEMS))
					.save(exporter);
			ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, GLASSBOARD)
					.group(CANVAS_ITEMS.location().toString())
					.define('S', Items.STICK)
					.define('G', Items.GLASS_PANE)
					.pattern("SSS")
					.pattern("SGS")
					.pattern("SSS")
					.unlockedBy("has_stick", has(Items.STICK))
					.unlockedBy("has_glass_pane", has(Items.GLASS_PANE))
					.unlockedBy("has_self", has(CANVAS_ITEMS))
					.save(exporter);
			SpecialRecipeBuilder.special(CANVAS_CLONE_RECIPE_SERIALIZER)
					.save(exporter, AurorasCanvas.id("canvas_clone").toString());
		}
	}
}
