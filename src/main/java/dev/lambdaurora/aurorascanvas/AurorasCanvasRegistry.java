/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas;

import dev.lambdaurora.aurorascanvas.block.BlackboardBlock;
import dev.lambdaurora.aurorascanvas.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorascanvas.item.BlackboardItem;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class AurorasCanvasRegistry {
	private AurorasCanvasRegistry() {
		throw new UnsupportedOperationException("AurorasCanvasRegistry only contains static definitions.");
	}

	//region Advancement Triggers
	//endregion

	public static final BlackboardBlock BLACKBOARD_BLOCK = registerBlockWithItem(
			AurorasCanvas.id("blackboard"),
			properties -> new BlackboardBlock(properties, false),
			FabricBlockSettings.create()
					.strength(.2f)
					.nonOpaque()
					.pistonBehavior(PushReaction.DESTROY)
					.sounds(SoundType.WOOD),
			BlackboardItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);
	public static final BlackboardBlock WAXED_BLACKBOARD_BLOCK = registerBlockWithItem(
			AurorasCanvas.id("waxed_blackboard"),
			properties -> new BlackboardBlock(properties, true),
			FabricBlockSettings.copyOf(BLACKBOARD_BLOCK),
			BlackboardItem::new,
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD)
	);

	public static final BlackboardBlock CHALKBOARD_BLOCK = registerWithItem("chalkboard",
			new BlackboardBlock(FabricBlockSettings.copyOf(BLACKBOARD_BLOCK), false),
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);
	public static final BlackboardBlock WAXED_CHALKBOARD_BLOCK = registerWithItem("waxed_chalkboard",
			new BlackboardBlock(FabricBlockSettings.copyOf(CHALKBOARD_BLOCK), true),
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);

	public static final BlackboardBlock GLASSBOARD_BLOCK = registerWithItem("glassboard",
			new BlackboardBlock(FabricBlockSettings.copyOf(BLACKBOARD_BLOCK).nonOpaque().sounds(SoundType.GLASS), false),
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);
	public static final BlackboardBlock WAXED_GLASSBOARD_BLOCK = registerWithItem("waxed_glassboard",
			new BlackboardBlock(FabricBlockSettings.copyOf(GLASSBOARD_BLOCK), true),
			new FabricItemSettings().equipmentSlot(stack -> EquipmentSlot.HEAD),
			BlackboardItem::new);

	public static final BlackboardPressBlock BLACKBOARD_PRESS_BLOCK = registerWithItem("blackboard_press",
			new BlackboardPressBlock(FabricBlockSettings.create().mapColor(MapColor.METAL)),
			new FabricItemSettings()
	);

	public static final Item PAINTER_PALETTE_ITEM = Items.registerItem(
			AurorasCanvas.id("painter_palette"),
			new PainterPaletteItem(new Item.Properties().stacksTo(1))
	);

	public static final BlockEntityType<BlackboardBlockEntity> BLACKBOARD_BLOCK_ENTITY_TYPE = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			AurorasCanvas.id("canvas"),
			FabricBlockEntityTypeBuilder.create(
					BlackboardBlockEntity::new,
					BLACKBOARD_BLOCK, CHALKBOARD_BLOCK, GLASSBOARD_BLOCK,
					WAXED_BLACKBOARD_BLOCK, WAXED_CHALKBOARD_BLOCK, WAXED_GLASSBOARD_BLOCK
			).build()
	);
	public static final BlockEntityType<BlackboardPressBlockEntity> BLACKBOARD_PRESS_BLOCK_ENTITY = Registry.register(
			BuiltInRegistries.BLOCK_ENTITY_TYPE,
			AurorasCanvas.id("blackboard_press"),
			FabricBlockEntityTypeBuilder.create(
					BlackboardPressBlockEntity::new,
					BLACKBOARD_PRESS_BLOCK
			).build()
	);

	public static final MenuType<PainterPaletteMenu> PAINTER_PALETTE_MENU_TYPE = Registry.register(
			BuiltInRegistries.MENU,
			AurorasCanvas.id("painter_palette"),
			new ExtendedScreenHandlerType<>(PainterPaletteMenu::new)
	);

	static <T extends Block> T registerBlock(
			Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties
	) {
		var key = ResourceKey.create(Registries.BLOCK, id);
		var block = factory.apply(properties);
		return Registry.register(BuiltInRegistries.BLOCK, id, block);
	}

	static <T extends Block> T registerBlockWithItem(
			Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties,
			BiFunction<T, Item.Properties, BlockItem> itemFactory, Item.Properties itemProperties
	) {
		var block = registerBlock(id, factory, properties);

		var key = ResourceKey.create(Registries.ITEM, id);
		Registry.register(BuiltInRegistries.ITEM, id, itemFactory.apply(block, itemProperties));

		return block;
	}

	static void init() {
	}
}
