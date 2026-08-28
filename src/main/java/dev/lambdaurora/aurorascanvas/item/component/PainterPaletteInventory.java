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
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a painter's palette inventory.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public final class PainterPaletteInventory implements PainterPaletteInventoryView, TooltipComponent {
	private static final int COLOR_SIZE = 27;
	private static final int TOOLS_SIZE = 4;
	private static final int SIZE = COLOR_SIZE + TOOLS_SIZE;

	public static final Codec<PainterPaletteInventory> CODEC = Raw.CODEC.xmap(
			raw -> {
				var colors = NonNullList.withSize(COLOR_SIZE, ItemStack.EMPTY);
				var tools = NonNullList.withSize(TOOLS_SIZE, ItemStack.EMPTY);

				for (var slot : raw.colors) {
					colors.set(slot.slot, slot.item);
				}
				for (var slot : raw.tools) {
					tools.set(slot.slot, slot.item);
				}

				return new PainterPaletteInventory(colors, tools, raw.selectedColor, raw.selectedTool);
			},
			inventory -> {
				var colors = new ArrayList<RawSlot>();
				for (byte slot = 0; slot < COLOR_SIZE; slot++) {
					var stack = inventory.colors.get(slot);

					if (!stack.isEmpty()) {
						colors.add(new RawSlot(slot, stack));
					}
				}

				var tools = new ArrayList<RawSlot>();
				for (byte slot = 0; slot < TOOLS_SIZE; slot++) {
					var stack = inventory.tools.get(slot);

					if (!stack.isEmpty()) {
						tools.add(new RawSlot(slot, stack));
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
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, inventory -> inventory.colors,
			ItemStack.OPTIONAL_LIST_STREAM_CODEC, inventory -> inventory.tools,
			ByteBufCodecs.BYTE, inventory -> inventory.selectedColor,
			ByteBufCodecs.BYTE, inventory -> inventory.selectedTool,
			PainterPaletteInventory::new
	);

	public static PainterPaletteInventory EMPTY = new PainterPaletteInventory(List.of(), List.of(), (byte) 0, (byte) -1);

	private final List<ItemStack> colors;
	private final List<ItemStack> tools;
	private final byte selectedColor;
	private final byte selectedTool;

	public PainterPaletteInventory(
			List<ItemStack> colors,
			List<ItemStack> tools,
			byte selectedColor,
			byte selectedTool
	) {
		var checkedColors = NonNullList.withSize(COLOR_SIZE, ItemStack.EMPTY);
		var checkedTools = NonNullList.withSize(TOOLS_SIZE, ItemStack.EMPTY);

		for (int i = 0; i < colors.size() && i < COLOR_SIZE; i++) {
			checkedColors.set(i, colors.get(i));
		}
		for (int i = 0; i < tools.size() && i < COLOR_SIZE; i++) {
			checkedTools.set(i, tools.get(i));
		}

		this.colors = List.copyOf(checkedColors);
		this.tools = List.copyOf(checkedTools);

		this.selectedColor = selectedColor;
		this.selectedTool = selectedTool;
	}

	@Override
	public @Unmodifiable List<ItemStack> getTools() {
		return this.tools.stream().filter(Predicate.not(ItemStack::isEmpty)).toList();
	}

	@Override
	public @Unmodifiable List<ItemStack> getColors() {
		return this.colors.stream().filter(Predicate.not(ItemStack::isEmpty)).toList();
	}

	@Override
	public ItemStack getColorStack(int index) {
		return this.colors.get(index);
	}

	@Override
	public byte getSelectedColorSlot() {
		return this.selectedColor;
	}

	@Override
	public int getSelectedToolSlot() {
		if (this.selectedTool == -1) return -1;
		else return this.selectedTool + COLOR_SIZE;
	}

	@Override
	public ItemStack getSelectedTool() {
		if (this.selectedTool == -1) return ItemStack.EMPTY;
		return this.tools.get(this.selectedTool);
	}

	@Override
	public byte findFirstNextColor() {
		byte i = this.selectedColor;

		do {
			i = (byte) ((i + 1) % COLOR_SIZE);
			var stack = this.colors.get(i);

			if (!stack.isEmpty()) {
				return i;
			}
		} while (i != this.selectedColor);

		return -1;
	}

	@Override
	public byte findFirstPreviousColor() {
		byte i = this.selectedColor;

		do {
			i--;
			if (i == -1) i = COLOR_SIZE - 1;

			var stack = this.colors.get(i);

			if (!stack.isEmpty()) {
				return i;
			}
		} while (i != this.selectedColor);

		return -1;
	}

	@Override
	public boolean isPaletteEmpty() {
		return this.colors.stream().allMatch(ItemStack::isEmpty) && this.tools.stream().allMatch(ItemStack::isEmpty);
	}

	public Mutable toMutable() {
		var mutable = new Mutable();
		for (int i = 0; i < this.colors.size(); i++) {
			mutable.setItem(i, this.colors.get(i));
		}
		for (int i = 0; i < this.tools.size(); i++) {
			mutable.setItem(COLOR_SIZE + i, this.tools.get(i));
		}
		mutable.selectedColor = this.selectedColor;
		mutable.selectedTool = this.selectedTool;
		return mutable;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PainterPaletteInventory that)) return false;
		return this.selectedColor == that.selectedColor && this.selectedTool == that.selectedTool && Objects.equals(this.colors, that.colors) && Objects.equals(this.tools, that.tools);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.colors, this.tools, this.selectedColor, this.selectedTool);
	}

	public static final class Mutable extends SimpleContainer implements PainterPaletteInventoryView {
		private byte selectedColor = EMPTY.selectedColor;
		private byte selectedTool = EMPTY.selectedTool;

		private final ContainerData properties = new ContainerData() {
			@Override
			public int get(int index) {
				return switch (index) {
					case 0 -> Mutable.this.selectedColor;
					case 1 -> Mutable.this.selectedTool;
					default -> 0;
				};
			}

			@Override
			public void set(int index, int value) {
				switch (index) {
					case 0 -> Mutable.this.selectedColor = (byte) value;
					case 1 -> Mutable.this.selectedTool = (byte) value;
				}
			}

			@Override
			public int getCount() {
				return 2;
			}
		};

		public Mutable() {
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

		@Override
		public @Unmodifiable List<ItemStack> getTools() {
			var list = new ArrayList<ItemStack>();

			for (int i = COLOR_SIZE; i < SIZE; i++) {
				var stack = this.getItem(i);
				if (!stack.isEmpty()) {
					list.add(stack);
				}
			}

			return list;
		}

		@Override
		public @Unmodifiable List<ItemStack> getColors() {
			var list = new ArrayList<ItemStack>();

			for (int i = 0; i < COLOR_SIZE; i++) {
				var stack = this.getItem(i);
				if (!stack.isEmpty()) {
					list.add(stack);
				}
			}

			return list;
		}

		@Override
		public ItemStack getColorStack(int index) {
			return this.getItem(index);
		}

		@Override
		public byte getSelectedColorSlot() {
			return this.selectedColor;
		}

		public void setSelectedColor(int color) {
			this.selectedColor = (byte) color;
		}

		@Override
		public int getSelectedToolSlot() {
			if (this.selectedTool == -1) return -1;
			else return this.selectedTool + COLOR_SIZE;
		}

		@Override
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

		@Override
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

		@Override
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

		@Override
		public boolean isPaletteEmpty() {
			return this.isEmpty();
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

		public PainterPaletteInventory toImmutable() {
			return new PainterPaletteInventory(
					this.items.subList(0, COLOR_SIZE),
					this.items.subList(COLOR_SIZE, SIZE),
					this.selectedColor,
					this.selectedTool
			);
		}
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
