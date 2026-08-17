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
import dev.lambdaurora.aurorascanvas.block.entity.CanvasBlockEntity;
import dev.lambdaurora.aurorascanvas.block.entity.CanvasPressBlockEntity;
import dev.lambdaurora.aurorascanvas.item.CanvasItem;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import dev.lambdaurora.aurorascanvas.menu.PainterPaletteMenu;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.BiFunction;
import java.util.function.Function;

import static dev.lambdaurora.aurorascanvas.AurorasCanvas.id;

public final class AurorasCanvasRegistry {
	private AurorasCanvasRegistry() {
		throw new UnsupportedOperationException("AurorasCanvasRegistry only contains static definitions.");
	}

	//region Advancement Triggers
	//endregion

	public static final BlockItemEntry<CanvasBlock, CanvasItem> BLACKBOARD = registerBlockWithItem(
			id("blackboard"),
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
			id("waxed_blackboard"),
			properties -> new CanvasBlock(properties, true),
			FabricBlockSettings.copyOf(BLACKBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlockItemEntry<CanvasBlock, CanvasItem> CHALKBOARD = registerBlockWithItem(
			id("chalkboard"),
			properties -> new CanvasBlock(properties, false),
			FabricBlockSettings.copyOf(BLACKBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);
	public static final BlockItemEntry<CanvasBlock, CanvasItem> WAXED_CHALKBOARD = registerBlockWithItem(
			id("waxed_chalkboard"),
			properties -> new CanvasBlock(properties, true),
			FabricBlockSettings.copyOf(CHALKBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlockItemEntry<CanvasBlock, CanvasItem> GLASSBOARD = registerBlockWithItem(
			id("glassboard"),
			properties -> new CanvasBlock(properties, false),
			FabricBlockSettings.copyOf(BLACKBOARD.block.value).nonOpaque().sounds(SoundType.GLASS),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);
	public static final BlockItemEntry<CanvasBlock, CanvasItem> WAXED_GLASSBOARD = registerBlockWithItem(
			id("waxed_glassboard"),
			properties -> new CanvasBlock(properties, true),
			FabricBlockSettings.copyOf(GLASSBOARD.block.value),
			CanvasItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlockItemEntry<CanvasPressBlock, BlockItem> BLACKBOARD_PRESS = registerBlockWithItem(
			id("blackboard_press"),
			CanvasPressBlock::new,
			FabricBlockSettings.create().mapColor(MapColor.METAL),
			BlockItem::new,
			new FabricItemSettings()
	);

	public static final PainterPaletteItem PAINTER_PALETTE_ITEM = Registry.register(
			BuiltInRegistries.ITEM,
			id("painter_palette"),
			new PainterPaletteItem(new Item.Properties().stacksTo(1))
	);

	public static final BlockEntityType<CanvasBlockEntity> BLACKBOARD_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			id("canvas"),
			FabricBlockEntityTypeBuilder.create(
					CanvasBlockEntity::new,
					BLACKBOARD.block.value, CHALKBOARD.block.value, GLASSBOARD.block.value,
					WAXED_BLACKBOARD.block.value, WAXED_CHALKBOARD.block.value, WAXED_GLASSBOARD.block.value
			).build()
	);
	public static final BlockEntityType<CanvasPressBlockEntity> BLACKBOARD_PRESS_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			id("blackboard_press"),
			FabricBlockEntityTypeBuilder.create(
					CanvasPressBlockEntity::new,
					BLACKBOARD_PRESS.block.value
			).build()
	);

	public static final MenuType<PainterPaletteMenu> PAINTER_PALETTE_MENU_TYPE = Registry.register(
			BuiltInRegistries.MENU,
			id("painter_palette"),
			new ExtendedScreenHandlerType<>(PainterPaletteMenu::new)
	);

	public static final TagKey<Item> BLACKBOARD_ITEMS = TagKey.create(Registries.ITEM, id("blackboards"));

	public static final TagKey<Block> BLACKBOARD_BLOCKS = TagKey.create(Registries.BLOCK, id("blackboards"));
	public static final TagKey<Block> GLASSBOARD_BLOCKS = TagKey.create(Registries.BLOCK, id("glassboards"));

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

	public record ItemEntry<T extends Item>(ResourceKey<Item> key, T value) {}

	public record BlockItemEntry<B extends Block, I extends BlockItem>(BlockEntry<B> block, ItemEntry<I> item) {}

	static void init() {
	}
}
