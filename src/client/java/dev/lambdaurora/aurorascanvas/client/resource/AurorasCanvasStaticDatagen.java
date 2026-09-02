/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.resource;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.advancement.DrawOnCanvasTrigger;
import dev.lambdaurora.aurorascanvas.advancement.PutCanvasOnEaselTrigger;
import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import dev.lambdaurora.aurorascanvas.client.model.glass.GlassboardModel;
import dev.lambdaurora.aurorascanvas.compat.supplementaries.SupplementariesCompat;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.*;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.*;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
		pack.addProvider(ModelProvider::new);
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
							CHALKBOARD.block().key(), WAXED_CHALKBOARD.block().key(),
							WHITEBOARD.block().key(), WAXED_WHITEBOARD.block().key()
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
			this.tag(WAXED_CANVAS_ITEMS)
					.add(
							WAXED_BLACKBOARD.item().key(),
							WAXED_CHALKBOARD.item().key(),
							WAXED_WHITEBOARD.item().key(),
							WAXED_GLASSBOARD.item().key()
					);

			this.tag(CANVAS_ITEMS)
					.add(
							BLACKBOARD.item().key(),
							CHALKBOARD.item().key(),
							WHITEBOARD.item().key(),
							GLASSBOARD.item().key()
					)
					.addTag(WAXED_CANVAS_ITEMS);

			this.tag(TagKey.create(Registries.ITEM, new Identifier("trinkets", "head/face")))
					.addTag(CANVAS_ITEMS);

			this.tag(CANVAS_COMPATIBLE_ITEMS)
					.addOptional(new Identifier(SupplementariesCompat.NAMESPACE, "blackboard"));
		}
	}

	private static class LootDataProvider extends FabricBlockLootTableProvider {
		protected final Set<Item> explosionResistant = Stream.of(
				BLACKBOARD,
				WAXED_BLACKBOARD,
				CHALKBOARD,
				WAXED_CHALKBOARD,
				WHITEBOARD,
				WAXED_WHITEBOARD,
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
			this.add(WHITEBOARD.block().value(), this::createCanvasDrop);
			this.add(WAXED_WHITEBOARD.block().value(), this::createCanvasDrop);
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
																			.copy("canvas", "BlockEntityTag.canvas")
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
																			.copy("canvas", "BlockEntityTag.canvas")
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

			var drawOnCanvas = Advancement.Builder.advancement()
					.parent(root)
					.display(
							PAINTER_PALETTE_ITEM,
							Component.translatable(
									"advancements.%s.adventure.draw_on_canvas.title".formatted(AurorasCanvas.NAMESPACE)
							),
							Component.translatable(
									"advancements.%s.adventure.draw_on_canvas.description".formatted(AurorasCanvas.NAMESPACE)
							),
							null,
							FrameType.TASK,
							true,
							true,
							false
					)
					.addCriterion("draw", DrawOnCanvasTrigger.TriggerInstance.drawAny())
					.build(AurorasCanvas.id("adventure/draw_on_canvas"));
			consumer.accept(drawOnCanvas);

			var putCanvasOnEasel = Advancement.Builder.advancement()
					.parent(drawOnCanvas)
					.display(
							EASEL_ITEM,
							Component.translatable(
									"advancements.%s.adventure.put_canvas_on_easel.title".formatted(AurorasCanvas.NAMESPACE)
							),
							Component.translatable(
									"advancements.%s.adventure.put_canvas_on_easel.description".formatted(AurorasCanvas.NAMESPACE)
							),
							null,
							FrameType.TASK,
							true,
							true,
							false
					)
					.addCriterion("put", PutCanvasOnEaselTrigger.TriggerInstance.put(ItemPredicate.Builder.item().of(CANVAS_ITEMS).build()))
					.build(AurorasCanvas.id("adventure/put_canvas_on_easel"));
			consumer.accept(putCanvasOnEasel);
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
			ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, WHITEBOARD)
					.group(CANVAS_ITEMS.location().toString())
					.define('S', Items.STICK)
					.define('C', Items.WHITE_CONCRETE)
					.pattern("SSS")
					.pattern("SCS")
					.pattern("SSS")
					.unlockedBy("has_stick", has(Items.STICK))
					.unlockedBy("has_concrete", has(Items.WHITE_CONCRETE))
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

	private static class ModelProvider extends FabricModelProvider {
		private static final TextureSlot BOARD_TEXTURE_SLOT = TextureSlot.create("board");
		private final PackOutput.PathProvider blockStatePathProvider;
		private final Map<Identifier, Supplier<JsonElement>> blockStates = new HashMap<>();

		public ModelProvider(FabricDataOutput output) {
			super(output);
			this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
		}

		@Override
		public void generateBlockStateModels(BlockModelGenerators generator) {
			this.createCanvasBlockStates(generator, BLACKBOARD.block());
			this.createWaxedCanvasBlockStates(generator, BLACKBOARD.block(), WAXED_BLACKBOARD.block());
			this.createCanvasBlockStates(generator, CHALKBOARD.block());
			this.createWaxedCanvasBlockStates(generator, CHALKBOARD.block(), WAXED_CHALKBOARD.block());
			this.createCanvasBlockStates(generator, WHITEBOARD.block());
			this.createWaxedCanvasBlockStates(generator, WHITEBOARD.block(), WAXED_WHITEBOARD.block());

			this.generateGlassboard(generator, "");
			this.generateWaxedGlassboard(generator);
		}

		@Override
		public void generateItemModels(ItemModelGenerators itemModelGenerator) {
			itemModelGenerator.generateFlatItem(EASEL_ITEM, ModelTemplates.FLAT_ITEM);
		}

		private void generateWaxedGlassboard(BlockModelGenerators generator) {
			this.generateGlassboard(generator, "waxed/");

			this.generateBaseWaxedGlassboardModels(generator, "");
			this.generateBaseWaxedGlassboardModels(generator, "pane/");

			var baseTemplate = new ModelTemplate(
					Optional.of(AurorasCanvas.id("block/glassboard")),
					Optional.empty()
			);

			baseTemplate.create(
					AurorasCanvas.id("block/waxed_glassboard"),
					new TextureMapping(),
					generator.modelOutput
			);

			var paneTemplate = new ModelTemplate(
					Optional.of(AurorasCanvas.id("block/glassboard/pane/glassboard")),
					Optional.empty()
			);

			paneTemplate.create(
					AurorasCanvas.id("block/glassboard/pane/waxed/glassboard"),
					new TextureMapping(),
					generator.modelOutput
			);
		}

		private void generateBaseWaxedGlassboardModels(BlockModelGenerators generator, String prefix) {
			for (var corner : GlassboardModel.Corner.CORNERS) {
				var template = new ModelTemplate(
						Optional.of(AurorasCanvas.id("block/" + GlassboardModel.getModelPath(prefix, corner, GlassboardModel.Type.NONE))),
						Optional.empty()
				);

				template.create(
						AurorasCanvas.id("block/" + GlassboardModel.getModelPath(prefix + "waxed/", corner, GlassboardModel.Type.NONE)),
						new TextureMapping(),
						generator.modelOutput
				);
			}
		}

		private void generateGlassboard(BlockModelGenerators generator, String prefix) {
			this.generateGlassboardBlockStates(prefix);

			this.generateGlassboardModels(generator, prefix);
			this.generateGlassboardModels(generator, "pane/" + prefix);
		}

		private void generateGlassboardModels(BlockModelGenerators generator, String prefix) {
			for (var corner : GlassboardModel.Corner.CORNERS) {
				var template = new ModelTemplate(
						Optional.of(AurorasCanvas.id("block/" + GlassboardModel.getModelPath(prefix, corner, GlassboardModel.Type.NONE))),
						Optional.empty(),
						BOARD_TEXTURE_SLOT
				);

				for (var type : GlassboardModel.Type.TYPES) {
					if (type == GlassboardModel.Type.NONE) continue;

					template.create(
							AurorasCanvas.id("block/" + GlassboardModel.getModelPath(prefix, corner, type)),
							TextureMapping.singleSlot(BOARD_TEXTURE_SLOT, AurorasCanvas.id("block/canvas/glassboard" + type.suffix())),
							generator.modelOutput
					);
				}
			}
		}

		private static final VariantProperty<Integer> Y_ROT = new VariantProperty<>("y", JsonPrimitive::new);

		private void createCanvasBlockStates(BlockModelGenerators generator, BlockEntry<?> entry) {
			this.createCanvasBlockStates(generator, entry, entry.key().identifier().withPrefix("block/"));
		}

		private void createCanvasBlockStates(BlockModelGenerators generator, BlockEntry<?> entry, Identifier model) {
			// We do the item block ourselves.
			generator.skipAutoItemBlock(entry.value());
			var blockStateData = MultiVariantGenerator.multiVariant(entry.value())
					.with(PropertyDispatch.property(GlassCanvasBlock.FACING).generate(
							direction -> Variant.variant()
									.with(VariantProperties.MODEL, model)
									.with(Y_ROT, (int) direction.getOpposite().toYRot())
					));
			generator.blockStateOutput.accept(blockStateData);

			generator.modelOutput.accept(
					entry.key().identifier().withPrefix("item/").withSuffix("_base"),
					new DelegatedModel(model)
			);
		}

		private void createWaxedCanvasBlockStates(BlockModelGenerators generator, BlockEntry<?> entry, BlockEntry<?> waxedEntry) {
			this.createCanvasBlockStates(generator, waxedEntry, entry.key().identifier().withPrefix("block/"));
		}

		private void generateGlassboardBlockStates(String prefix) {
			var baseId = AurorasCanvas.id(prefix.replace('/', '_') + "glassboard");
			var baseModelId = baseId.withPrefix("block/");
			var basePaneId = AurorasCanvas.id("block/glassboard/pane/" + prefix + "glassboard");

			this.blockStates.put(baseId, MultiVariantGenerator.multiVariant(GLASSBOARD.block().value())
					.with(PropertyDispatch.properties(GlassCanvasBlock.FACING, GlassCanvasBlock.PANE).generate(
							(direction, pane) -> Variant.variant()
									.with(VariantProperties.MODEL, pane ? basePaneId : baseModelId)
									.with(Y_ROT, (int) direction.getOpposite().toYRot())
					)));

			for (var corner : GlassboardModel.Corner.CORNERS) {
				for (var type : GlassboardModel.Type.TYPES) {
					var id = AurorasCanvas.id(GlassboardModel.getModelPath(prefix, corner, type));
					var modelId = id.withPrefix("block/");

					var paneId = AurorasCanvas.id("block/" + GlassboardModel.getModelPath("pane/" + prefix, corner, type));

					var blockState = MultiVariantGenerator.multiVariant(GLASSBOARD.block().value())
							.with(PropertyDispatch.properties(GlassCanvasBlock.FACING, GlassCanvasBlock.PANE).generate(
									(direction, pane) -> Variant.variant()
											.with(VariantProperties.MODEL, pane ? paneId : modelId)
											.with(Y_ROT, (int) direction.getOpposite().toYRot())
							));
					this.blockStates.put(id, blockState);
				}
			}
		}

		@Override
		public CompletableFuture<?> run(CachedOutput output) {
			return CompletableFuture.allOf(
					super.run(output),
					this.saveCollection(output, this.blockStates, this.blockStatePathProvider::json)
			);
		}

		private <T> CompletableFuture<?> saveCollection(
				CachedOutput output, Map<T, ? extends Supplier<JsonElement>> objectToJsonMap, Function<T, Path> resolveObjectPath
		) {
			return CompletableFuture.allOf(objectToJsonMap.entrySet().stream().map(entry -> {
				var path = resolveObjectPath.apply(entry.getKey());
				var jsonElement = entry.getValue().get();
				return DataProvider.saveStable(output, jsonElement, path);
			}).toArray(CompletableFuture[]::new));
		}
	}
}
