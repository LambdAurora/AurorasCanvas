/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Represents a painter's palette inventory.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class PainterPaletteInventory extends SimpleContainer {
	private static final int COLOR_SIZE = 27;
	private static final int TOOLS_SIZE = 4;
	private static final int SIZE = COLOR_SIZE + TOOLS_SIZE;

	public static final Codec<PainterPaletteInventory> CODEC = Raw.CODEC.xmap(
			raw -> {
				var inventory = new PainterPaletteInventory();
				for (var slot : raw.colors) {
					inventory.setItem(slot.slot, slot.item);
				}
				for (var slot : raw.tools) {
					inventory.setItem(COLOR_SIZE + slot.slot, slot.item);
				}
				inventory.selectedColor = raw.selectedColor;
				inventory.selectedTool = raw.selectedTool;
				return inventory;
			},
			inventory -> {
				var colors = new ArrayList<RawSlot>();
				for (byte slot = 0; slot < COLOR_SIZE; slot++) {
					var stack = inventory.getItem(slot);

					if (!stack.isEmpty()) {
						colors.add(new RawSlot(slot, stack));
					}
				}

				var tools = new ArrayList<RawSlot>();
				for (byte slot = COLOR_SIZE; slot < SIZE; slot++) {
					var stack = inventory.getItem(slot);

					if (!stack.isEmpty()) {
						tools.add(new RawSlot((byte) (slot - COLOR_SIZE), stack));
					}
				}

				return new Raw(
						colors,
						tools,
						inventory.selectedColor,
						inventory.selectedTool
				);
			}
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, PainterPaletteInventory> STREAM_CODEC = StreamCodec.composite(
			ItemStack.LIST_STREAM_CODEC, inventory -> inventory.items,
			ByteBufCodecs.BYTE, inventory -> inventory.selectedColor,
			ByteBufCodecs.BYTE, inventory -> inventory.selectedTool,
			(items, selectedColor, selectedTool) -> {
				var inventory = new PainterPaletteInventory();
				for (int i = 0; i < items.size(); i++) {
					inventory.setItem(i, items.get(i));
				}
				inventory.selectedColor = selectedColor;
				inventory.selectedTool = selectedTool;
				return inventory;
			}
	);

	private byte selectedColor = 0;
	private byte selectedTool = -1;

	private final ContainerData properties = new ContainerData() {
		@Override
		public int get(int index) {
			return switch (index) {
				case 0 -> PainterPaletteInventory.this.selectedColor;
				case 1 -> PainterPaletteInventory.this.selectedTool;
				default -> 0;
			};
		}

		@Override
		public void set(int index, int value) {
			switch (index) {
				case 0 -> PainterPaletteInventory.this.selectedColor = (byte) value;
				case 1 -> PainterPaletteInventory.this.selectedTool = (byte) value;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public PainterPaletteInventory() {
		super(SIZE);

		this.addListener(sender -> {
			if (this.getSelectedColor().isEmpty()) {
				byte previousSlot = this.findFirstPreviousColor();
				byte nextSlot = this.findFirstNextColor();

				byte oldColor = this.selectedColor;

				if (nextSlot == -1) this.selectedColor = previousSlot;
				else this.selectedColor = nextSlot;

				if (this.selectedColor == -1) {
					this.selectedColor = oldColor;
				}
			}

			if (this.selectedTool != -1 && this.getSelectedTool().isEmpty()) {
				this.setSelectedToolSlot(this.scrollTool(true));
			}
		});
	}

	public List<ItemStack> getTools() {
		var list = new ArrayList<ItemStack>();

		for (int i = COLOR_SIZE; i < SIZE; i++) {
			var stack = this.getItem(i);
			if (!stack.isEmpty()) {
				list.add(stack);
			}
		}

		return list;
	}

	public @Unmodifiable List<DrawAction> getAvailableTools(FeatureFlagSet enabledFeatures) {
		var tools = new HashSet<>(
				this.getTools().stream().map(stack -> DrawAction.byItem(enabledFeatures, stack.getItem()))
						.filter(Objects::nonNull)
						.toList()
		);

		tools.add(DrawAction.DEFAULT);

		return tools.stream()
				.sorted(Comparator.comparingInt(DrawAction::ordinal))
				.toList();
	}

	public List<ItemStack> getColors() {
		var list = new ArrayList<ItemStack>();

		for (int i = 0; i < COLOR_SIZE; i++) {
			var stack = this.getItem(i);
			if (!stack.isEmpty()) {
				list.add(stack);
			}
		}

		return list;
	}

	public byte getSelectedColorSlot() {
		return this.selectedColor;
	}

	public ItemStack getSelectedColor() {
		return this.getItem(this.selectedColor);
	}

	public void setSelectedColor(int color) {
		this.selectedColor = (byte) color;
	}

	public int getSelectedToolSlot() {
		if (this.selectedTool == -1) return -1;
		else return this.selectedTool + COLOR_SIZE;
	}

	public ItemStack getSelectedTool() {
		if (this.selectedTool == -1) return ItemStack.EMPTY;
		return this.getItem(this.getSelectedToolSlot());
	}

	public void setSelectedToolSlot(int slot) {
		this.selectedTool = (byte) (slot == -1 ? -1 : slot - COLOR_SIZE);
	}

	public ContainerData getProperties() {
		return this.properties;
	}

	public byte findFirstNextColor() {
		byte i = this.selectedColor;

		do {
			i = (byte) ((i + 1) % COLOR_SIZE);
			var stack = this.getItem(i);

			if (!stack.isEmpty()) {
				return i;
			}
		} while (i != this.selectedColor);

		return -1;
	}

	public byte findFirstPreviousColor() {
		byte i = this.selectedColor;

		do {
			i--;
			if (i == -1) i = COLOR_SIZE - 1;

			var stack = this.getItem(i);

			if (!stack.isEmpty()) {
				return i;
			}
		} while (i != this.selectedColor);

		return -1;
	}

	public ItemStack getNextColorStack() {
		byte previousSlot = this.findFirstPreviousColor();
		byte nextSlot = this.findFirstNextColor();

		if (nextSlot == -1 || nextSlot == previousSlot) return ItemStack.EMPTY;
		else return this.getItem(nextSlot);
	}

	public @Nullable DrawModifier getNextColor() {
		return DrawModifier.fromItem(this.getNextColorStack());
	}

	public ItemStack getPreviousColorStack() {
		byte previousSlot = this.findFirstPreviousColor();

		if (previousSlot == -1) return ItemStack.EMPTY;
		else return this.getItem(previousSlot);
	}

	public @Nullable DrawModifier getPreviousColor() {
		return DrawModifier.fromItem(this.getPreviousColorStack());
	}

	public int scrollTool(boolean next) {
		int localIndex = this.selectedTool + 1;

		do {
			if (next) localIndex++;
			else localIndex--;

			if (localIndex < 0) localIndex = (SIZE - COLOR_SIZE) + 1;
			else if (localIndex > SIZE - COLOR_SIZE + 1) localIndex = 0;
		} while (localIndex != 0 && this.getItem(COLOR_SIZE + localIndex - 1).isEmpty());

		int result = localIndex - 1;

		if (result == -1) return -1;
		else return result + COLOR_SIZE;
	}

	public int getSlotOf(ItemStack stack) {
		for (int i = 0; i < this.getContainerSize(); i++) {
			if (this.getItem(i) == stack) {
				return i;
			}
		}

		return -1;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	public @Nullable CompoundTag toNbt() {
		if (this.isEmpty()) {
			return null;
		}

		return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this)
				.result()
				.orElse(null);
	}

	public static PainterPaletteInventory fromNbt(@Nullable CompoundTag nbt) {
		var inventory = new PainterPaletteInventory();

		if (nbt == null) {
			return new PainterPaletteInventory();
		}

		return CODEC.decode(NbtOps.INSTANCE, nbt).result().orElseThrow().getFirst();
	}

	public record Raw(
			@Unmodifiable List<RawSlot> colors,
			@Unmodifiable List<RawSlot> tools,
			byte selectedColor,
			byte selectedTool
	) {
		public static final Codec<Raw> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				RawSlot.CODEC.listOf().optionalFieldOf("colors", List.of()).forGetter(Raw::colors),
				RawSlot.CODEC.listOf().optionalFieldOf("tools", List.of()).forGetter(Raw::tools),
				Codec.BYTE.optionalFieldOf("selected_color", (byte) 0).forGetter(Raw::selectedColor),
				Codec.BYTE.optionalFieldOf("selected_tool", (byte) -1).forGetter(Raw::selectedTool)
		).apply(instance, Raw::new));
	}

	public record RawSlot(byte slot, ItemStack item) {
		public static final Codec<RawSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.BYTE.fieldOf("slot").forGetter(RawSlot::slot),
				ItemStack.CODEC.fieldOf("item").forGetter(RawSlot::item)
		).apply(instance, RawSlot::new));
	}
}