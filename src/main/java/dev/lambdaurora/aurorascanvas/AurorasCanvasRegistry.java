/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas;

import dev.lambdaurora.aurorascanvas.block.CanvasBlock;
import dev.lambdaurora.aurorascanvas.block.CanvasPressBlock;
import dev.lambdaurora.aurorascanvas.block.GlassCanvasBlock;
import dev.lambdaurora.aurorascanvas.block.entity.CanvasPressBlockEntity;
import dev.lambdaurora.aurorascanvas.block.entity.GlassCanvasBlockEntity;
import dev.lambdaurora.aurorascanvas.block.entity.SimpleCanvasBlockEntity;
import dev.lambdaurora.aurorascanvas.dispenser.CanvasDispenseItemBehavior;
import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.lambdaurora.aurorascanvas.item.EaselEntityItem;
import dev.lambdaurora.aurorascanvas.item.GlassCanvasItem;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import dev.lambdaurora.aurorascanvas.menu.PainterPaletteMenu;
import dev.lambdaurora.aurorascanvas.recipe.CanvasCloneRecipe;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.*;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.lambdaurora.aurorascanvas.AurorasCanvas.id;
import static dev.lambdaurora.aurorascanvas.AurorasCanvasIds.*;

public final class AurorasCanvasRegistry {
	private AurorasCanvasRegistry() {
		throw new UnsupportedOperationException("AurorasCanvasRegistry only contains static definitions.");
	}

	//region Advancement Triggers
	//endregion

	public static final BlockItemEntry<CanvasBlock, CanvasItem> BLACKBOARD = registerBlockWithItem(
			BLACKBOARD_ID,
			properties -> new CanvasBlock(properties, false),
			FabricBlockSettings.create()
					.strength(.2f)
					.nonOpaque()
					.pistonBehavior(PushReaction.DESTROY)
					.sounds(SoundType.WOOD),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);
	public static final BlockItemEntry<CanvasBlock, CanvasItem> WAXED_BLACKBOARD = registerBlockWithItem(
			WAXED_BLACKBOARD_ID,
			properties -> new CanvasBlock(properties, true),
			FabricBlockSettings.copyOf(BLACKBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlockItemEntry<CanvasBlock, CanvasItem> CHALKBOARD = registerBlockWithItem(
			CHALKBOARD_ID,
			properties -> new CanvasBlock(properties, false),
			FabricBlockSettings.copyOf(BLACKBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);
	public static final BlockItemEntry<CanvasBlock, CanvasItem> WAXED_CHALKBOARD = registerBlockWithItem(
			WAXED_CHALKBOARD_ID,
			properties -> new CanvasBlock(properties, true),
			FabricBlockSettings.copyOf(CHALKBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlockItemEntry<CanvasBlock, CanvasItem> WHITEBOARD = registerBlockWithItem(
			WHITEBOARD_ID,
			properties -> new CanvasBlock(properties, false),
			FabricBlockSettings.create()
					.strength(.2f)
					.nonOpaque()
					.pistonBehavior(PushReaction.DESTROY)
					.sounds(SoundType.WOOD),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);
	public static final BlockItemEntry<CanvasBlock, CanvasItem> WAXED_WHITEBOARD = registerBlockWithItem(
			WAXED_WHITEBOARD_ID,
			properties -> new CanvasBlock(properties, true),
			FabricBlockSettings.copyOf(WHITEBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlockItemEntry<GlassCanvasBlock, CanvasItem> GLASSBOARD = registerBlockWithItem(
			GLASSBOARD_ID,
			properties -> new GlassCanvasBlock(properties, false),
			FabricBlockSettings.copyOf(BLACKBOARD.block.value).nonOpaque().sounds(SoundType.GLASS),
			GlassCanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);
	public static final BlockItemEntry<GlassCanvasBlock, CanvasItem> WAXED_GLASSBOARD = registerBlockWithItem(
			WAXED_GLASSBOARD_ID,
			properties -> new GlassCanvasBlock(properties, true),
			FabricBlockSettings.copyOf(GLASSBOARD.block.value),
			GlassCanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlockItemEntry<CanvasPressBlock, BlockItem> CANVAS_PRESS = registerBlockWithItem(
			CANVAS_PRESS_ID,
			CanvasPressBlock::new,
			FabricBlockSettings.create().mapColor(MapColor.METAL),
			BlockItem::new,
			new FabricItemSettings()
	);

	public static final EaselEntityItem EASEL_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			id("easel"),
			new EaselEntityItem(new Item.Properties().stacksTo(16))
	);

	public static final PainterPaletteItem PAINTER_PALETTE_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			id("painter_palette"),
			new PainterPaletteItem(new Item.Properties().stacksTo(1))
	);

	public static final BlockEntityType<SimpleCanvasBlockEntity> CANVAS_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			CANVAS_ID,
			FabricBlockEntityTypeBuilder.create(
					SimpleCanvasBlockEntity::new,
					BLACKBOARD.block.value, CHALKBOARD.block.value, WHITEBOARD.block.value,
					WAXED_BLACKBOARD.block.value, WAXED_CHALKBOARD.block.value, WAXED_WHITEBOARD.block.value
			).build()
	);
	public static final BlockEntityType<GlassCanvasBlockEntity> GLASS_CANVAS_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			GLASSBOARD_ID,
			FabricBlockEntityTypeBuilder.create(
					GlassCanvasBlockEntity::new,
					GLASSBOARD.block.value, WAXED_GLASSBOARD.block.value
			).build()
	);
	public static final BlockEntityType<CanvasPressBlockEntity> BLACKBOARD_PRESS_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			CANVAS_PRESS_ID,
			FabricBlockEntityTypeBuilder.create(
					CanvasPressBlockEntity::new,
					CANVAS_PRESS.block.value
			).build()
	);

	public static final EntityType<EaselEntity> EASEL_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.ENTITY_TYPE,
			id("easel"),
			FabricEntityTypeBuilder.create(MobCategory.MISC, EaselEntity::new)
					.dimensions(EntityDimensions.scalable(1.f, 2.f))
					.trackRangeChunks(10)
					.build()
	);

	public static final MenuType<PainterPaletteMenu> PAINTER_PALETTE_MENU_TYPE = Registry.register(
			BuiltInRegistries.MENU,
			id("painter_palette"),
			new ExtendedScreenHandlerType<>(PainterPaletteMenu::new)
	);

	public static final RecipeSerializer<CanvasCloneRecipe> CANVAS_CLONE_RECIPE_SERIALIZER = Registry.register(
			BuiltInRegistries.RECIPE_SERIALIZER,
			id("crafting_special_canvas_clone"),
			new SimpleCraftingRecipeSerializer<>(CanvasCloneRecipe::new)
	);

	public static final TagKey<Item> CANVAS_ITEMS = TagKey.create(Registries.ITEM, id("canvases"));
	public static final TagKey<Item> WAXED_CANVAS_ITEMS = TagKey.create(Registries.ITEM, id("waxed_canvases"));

	public static final TagKey<Block> CANVAS_BLOCKS = TagKey.create(Registries.BLOCK, id("canvases"));
	public static final TagKey<Block> GLASSBOARD_BLOCKS = TagKey.create(Registries.BLOCK, id("glassboards"));

	public static final TagKey<DamageType> IGNITES_EASELS = TagKey.create(Registries.DAMAGE_TYPE, id("ignites_easels"));
	public static final TagKey<DamageType> BURNS_EASELS = TagKey.create(Registries.DAMAGE_TYPE, id("burns_easels"));

	static <T extends Block> BlockEntry<T> registerBlock(
			Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties
	) {
		var key = ResourceKey.create(Registries.BLOCK, id);
		var block = factory.apply(properties);
		return new BlockEntry<>(key, Registry.register(BuiltInRegistries.BLOCK, key, block));
	}

	static <B extends Block, I extends BlockItem> BlockItemEntry<B, I> registerBlockWithItem(
			Identifier id, Function<BlockBehaviour.Properties, B> factory, BlockBehaviour.Properties properties,
			BiFunction<B, Item.Properties, I> itemFactory, Item.Properties itemProperties
	) {
		var block = registerBlock(id, factory, properties);

		var key = ResourceKey.create(Registries.ITEM, id);
		var item = Registry.register(BuiltInRegistries.ITEM, key, itemFactory.apply(block.value, itemProperties));

		return new BlockItemEntry<>(block, new ItemEntry<>(key, item));
	}

	public record BlockEntry<T extends Block>(ResourceKey<Block> key, T value) {}

	public record ItemEntry<T extends Item>(ResourceKey<Item> key, T value) implements ItemLike {
		@Override
		public Item asItem() {
			return this.value;
		}
	}

	public record BlockItemEntry<B extends Block, I extends BlockItem>(BlockEntry<B> block, ItemEntry<I> item)
			implements ItemLike {
		@Override
		public Item asItem() {
			return this.item.value();
		}
	}

	static void init() {
		OxidizableBlocksRegistry.registerWaxableBlockPair(BLACKBOARD.block.value, WAXED_BLACKBOARD.block.value);
		OxidizableBlocksRegistry.registerWaxableBlockPair(CHALKBOARD.block.value, WAXED_CHALKBOARD.block.value);
		OxidizableBlocksRegistry.registerWaxableBlockPair(WHITEBOARD.block.value, WAXED_WHITEBOARD.block.value);
		OxidizableBlocksRegistry.registerWaxableBlockPair(GLASSBOARD.block.value, WAXED_GLASSBOARD.block.value);

		FabricDefaultAttributeRegistry.register(EASEL_ENTITY_TYPE, LivingEntity.createLivingAttributes());

		DispenserBlock.registerBehavior(EASEL_ITEM, new DefaultDispenseItemBehavior() {
			@Override
			public ItemStack execute(BlockSource source, ItemStack stack) {
				Direction direction = source.getBlockState().getValue(DispenserBlock.FACING);
				BlockPos blockPos = source.getPos().relative(direction);
				ServerLevel serverLevel = source.getLevel();
				Consumer<EaselEntity> consumer = EntityType.appendDefaultStackConfig(
						easel -> easel.setYRot(direction.toYRot()), serverLevel, stack, null
				);
				var easel = EASEL_ENTITY_TYPE.spawn(
						serverLevel, stack.getTag(), consumer, blockPos, MobSpawnType.DISPENSER, false, false
				);
				if (easel != null) {
					stack.shrink(1);
				}

				return stack;
			}
		});
		DispenserBlock.registerBehavior(BLACKBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);
		DispenserBlock.registerBehavior(CHALKBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);
		DispenserBlock.registerBehavior(WHITEBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);
		DispenserBlock.registerBehavior(GLASSBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);
		DispenserBlock.registerBehavior(WAXED_BLACKBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);
		DispenserBlock.registerBehavior(WAXED_CHALKBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);
		DispenserBlock.registerBehavior(WAXED_WHITEBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);
		DispenserBlock.registerBehavior(WAXED_GLASSBOARD.item(), CanvasDispenseItemBehavior.INSTANCE);

		AurorasCanvasSoundEvents.init();
	}
}
