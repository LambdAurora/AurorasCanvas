/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.item;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.BlackboardColor;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.menu.NestedMenu;
import dev.lambdaurora.aurorascanvas.menu.PainterPaletteMenu;
import dev.lambdaurora.aurorascanvas.tooltip.PainterPaletteTooltipData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Represents a painter's palette item which can be used for easier painting on canvases.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class PainterPaletteItem extends Item {
	private static final int DEFAULT_BACKGROUND_COLOR = 0xff967441;

	public PainterPaletteItem(Properties settings) {
		super(settings);
	}

	public ItemStack getCurrentColorAsItem(ItemStack paletteStack) {
		var inventory = PainterPaletteInventory.fromNbt(paletteStack.getTagElement("inventory"));

		return inventory.getSelectedColor();
	}

	public ItemStack getCurrentToolAsItem(ItemStack paletteStack) {
		var inventory = PainterPaletteInventory.fromNbt(paletteStack.getTagElement("inventory"));
		if (inventory.selectedTool == -1) return ItemStack.EMPTY;

		return inventory.getSelectedTool();
	}

	public static MutableComponent getSelectedToolMessage(PainterPaletteInventory inventory, FeatureFlagSet enabledFeatures) {
		Component toolName = Canvas.DrawAction.ACTIONS.stream()
				.filter(drawAction -> {
					var offHandTool = drawAction.getOffHandTool(enabledFeatures);
					var selectedTool = inventory.getSelectedTool();

					return (offHandTool == null && selectedTool.isEmpty()) || selectedTool.is(offHandTool);
				}).findFirst()
				.map(Canvas.DrawAction::getName).orElseGet(() -> {
					if (inventory.getSelectedTool().is(Items.STICK)) return Component.translatable(AurorasCanvas.NAMESPACE + ".tool.line");
					else throw new IllegalStateException("Could not get tool name.");
				});

		return Component.translatable(AurorasCanvas.NAMESPACE + ".change_tool", toolName);
	}

	@Override
	public boolean overrideOtherStackedOnMe(ItemStack thisStack, ItemStack otherStack, Slot thisSlot, ClickAction clickType, Player player, SlotAccess cursor) {
		if (clickType == ClickAction.SECONDARY && otherStack.isEmpty() && !(player.containerMenu instanceof PainterPaletteMenu)) {
			NestedMenu.OriginType originType = null;
			if (thisSlot.container == player.getInventory()) {
				originType = NestedMenu.OriginType.PLAYER;
			} else if (thisSlot.container == player.getEnderChestInventory()) {
				originType = NestedMenu.OriginType.ENDER_CHEST;
			}

			if (originType != null && !player.level().isClientSide()) {
				player.inventoryMenu.resumeRemoteUpdates();
				player.openMenu(new PainterPaletteMenu.Factory(thisStack, originType, thisSlot.getContainerSlot()));
			}

			return true;
		}

		return super.overrideOtherStackedOnMe(thisStack, otherStack, thisSlot, clickType, player, cursor);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
		ItemStack stack = user.getItemInHand(hand);

		if (user.isShiftKeyDown()) {
			if (!user.level().isClientSide()) {
				int index = user.getInventory().findSlotMatchingItem(stack);

				user.containerMenu.resumeRemoteUpdates();
				user.openMenu(new PainterPaletteMenu.Factory(stack, NestedMenu.OriginType.PLAYER, index));
			}

			return InteractionResultHolder.consume(stack);
		}

		return super.use(world, user, hand);
	}

	public void onScroll(Player player, ItemStack paletteStack, double scrollDelta, boolean toolModifier) {
		var inventory = PainterPaletteInventory.fromNbt(paletteStack.getTagElement("inventory"));

		if (inventory.isEmpty()) {
			return;
		}

		if (!toolModifier) {
			if (scrollDelta < 0) {
				byte nextColor = inventory.findFirstNextColor();

				if (nextColor != -1) {
					inventory.selectedColor = nextColor;
				}
			} else {
				byte previousColor = inventory.findFirstPreviousColor();

				if (previousColor != -1) {
					inventory.selectedColor = previousColor;
				}
			}

			var nbt = inventory.toNbt();
			if (nbt != null) paletteStack.addTagElement("inventory", nbt);
			else paletteStack.removeTagKey("inventory");
			player.inventoryMenu.broadcastChanges();

			var modifier = DrawModifier.fromItem(inventory.getSelectedColor());

			if (!(modifier instanceof BlackboardColor) && modifier != null) {
				player.displayClientMessage(Component.translatable(AurorasCanvas.NAMESPACE + ".change_modifier", modifier.getName()), true);
			}
		} else {
			byte nextTool = inventory.scrollTool(scrollDelta < 0);

			if (inventory.selectedTool != nextTool) {
				inventory.selectedTool = nextTool;
				var nbt = inventory.toNbt();
				if (nbt != null) paletteStack.addTagElement("inventory", nbt);
				else paletteStack.removeTagKey("inventory");
				player.inventoryMenu.broadcastChanges();

				var message = getSelectedToolMessage(inventory, player.level().enabledFeatures());
				BlackboardColor primaryColor = BlackboardColor.fromItem(inventory.getSelectedColor().getItem());

				if (primaryColor != null && primaryColor != BlackboardColor.EMPTY) message.withStyle(style -> style.withColor(primaryColor.getColor()));

				player.displayClientMessage(message, true);
			}
		}
	}

	public int getColor(ItemStack paletteStack, int tintIndex) {
		CompoundTag nbt = paletteStack.getTagElement("inventory");

		DrawModifier primaryColor = null;
		DrawModifier previousColor = null;
		DrawModifier nextColor = null;

		if (nbt != null) {
			int selectedColor;

			if (nbt.contains(PainterPaletteInventory.SELECTED_COLOR_KEY, Tag.TAG_BYTE)) {
				selectedColor = nbt.getByte(PainterPaletteInventory.SELECTED_COLOR_KEY);
			} else {
				selectedColor = 0;
			}

			ListTag colors = nbt.getList("colors", Tag.TAG_COMPOUND);

			int previousSlot = -1, nextSlot = -1;
			CompoundTag previousNbt = null, nextNbt = null;

			for (var colorNbt : colors) {
				var slotNbt = (CompoundTag) colorNbt;
				int slot = slotNbt.getByte("slot");

				if (slot == selectedColor) {
					primaryColor = modifierFromNbt(slotNbt);
				} else if (slot > selectedColor) {
					if (nextSlot < selectedColor || slot < nextSlot || nextSlot == -1) {
						nextSlot = slot;
						nextNbt = slotNbt;
					}
					if (previousSlot == -1 || (slot > previousSlot && previousSlot > selectedColor)) {
						previousSlot = slot;
						previousNbt = slotNbt;
					}
				} else {
					if (nextSlot == -1 || (slot < nextSlot && nextSlot < selectedColor)) {
						nextSlot = slot;
						nextNbt = slotNbt;
					}
					if (previousSlot > selectedColor || slot > previousSlot || previousSlot == -1) {
						previousSlot = slot;
						previousNbt = slotNbt;
					}
				}
			}

			if (nextSlot == -1 || nextSlot == previousSlot) nextNbt = null;
			if (previousSlot == -1) previousNbt = null;

			previousColor = previousNbt == null ? BlackboardColor.EMPTY : modifierFromNbt(previousNbt);
			nextColor = nextNbt == null ? BlackboardColor.EMPTY : modifierFromNbt(nextNbt);

			if (primaryColor == BlackboardColor.EMPTY) primaryColor = null;
			if (previousColor == BlackboardColor.EMPTY) previousColor = null;
			if (nextColor == BlackboardColor.EMPTY) nextColor = null;
		}

		return switch (tintIndex) {
			case 1 -> primaryColor == null ? DEFAULT_BACKGROUND_COLOR : primaryColor.getColor();
			case 2 -> primaryColor == null ? 0xffffffff : primaryColor.getColor();
			case 3 -> previousColor == null ? DEFAULT_BACKGROUND_COLOR : previousColor.getColor();
			case 4 -> nextColor == null ? DEFAULT_BACKGROUND_COLOR : nextColor.getColor();
			default -> 0xffffffff;
		};
	}

	@Override
	public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
		var nbt = stack.getTagElement("inventory");
		if (nbt != null) {
			return Optional.of(new PainterPaletteTooltipData(PainterPaletteInventory.fromNbt(nbt)));
		}
		return super.getTooltipImage(stack);
	}

	public static class PainterPaletteInventory extends SimpleContainer {
		private static final int COLOR_SIZE = 27;
		private static final int TOOLS_SIZE = 4;
		private static final int SIZE = COLOR_SIZE + TOOLS_SIZE;
		private static final String SELECTED_COLOR_KEY = "selected_color";
		private static final String SELECTED_TOOL_KEY = "selected_tool";
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
					this.selectedTool = this.scrollTool(true);
				}
			});
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
			return this.getItem(this.getSelectedToolSlot());
		}

		public void setSelectedToolSlot(int slot) {
			this.selectedTool = (byte) (slot == -1 ? -1 : slot - COLOR_SIZE);
		}

		public ContainerData getProperties() {
			return this.properties;
		}

		private byte findFirstNextColor() {
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

		private byte findFirstPreviousColor() {
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

		public byte scrollTool(boolean next) {
			int localIndex = this.selectedTool + 1;

			do {
				if (next) localIndex++;
				else localIndex--;

				if (localIndex < 0) localIndex = (SIZE - COLOR_SIZE) + 1;
				else if (localIndex > SIZE - COLOR_SIZE + 1) localIndex = 0;
			} while (localIndex != 0 && this.getItem(COLOR_SIZE + localIndex - 1).isEmpty());

			return (byte) (localIndex - 1);
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

			var nbt = new CompoundTag();

			this.addInventoryPart(nbt, "colors", (byte) 0, (byte) COLOR_SIZE);
			this.addInventoryPart(nbt, "tools", (byte) COLOR_SIZE, (byte) (COLOR_SIZE + TOOLS_SIZE));

			if (!this.getItem(this.selectedColor).isEmpty()) {
				nbt.putByte(SELECTED_COLOR_KEY, this.selectedColor);
			}

			if (this.selectedTool != -1) {
				nbt.putByte(SELECTED_TOOL_KEY, this.selectedTool);
			}

			return nbt;
		}

		public void readNbt(CompoundTag nbt) {
			this.readInventoryPart(nbt.getList("colors", Tag.TAG_COMPOUND), 0);
			this.readInventoryPart(nbt.getList("tools", Tag.TAG_COMPOUND), COLOR_SIZE);

			if (nbt.contains(SELECTED_COLOR_KEY, Tag.TAG_BYTE)) {
				this.selectedColor = nbt.getByte(SELECTED_COLOR_KEY);
			} else {
				this.selectedColor = 0;
			}

			if (nbt.contains(SELECTED_TOOL_KEY, Tag.TAG_BYTE)) {
				this.selectedTool = nbt.getByte(SELECTED_TOOL_KEY);
			} else {
				this.selectedTool = -1;
			}
		}

		public static PainterPaletteInventory fromNbt(@Nullable CompoundTag nbt) {
			var inventory = new PainterPaletteInventory();

			if (nbt == null) {
				return inventory;
			}

			inventory.readNbt(nbt);
			return inventory;
		}

		private void addInventoryPart(CompoundTag nbt, String name, byte from, byte to) {
			var slots = new ListTag();

			for (byte slot = from; slot < to; slot++) {
				var stack = this.getItem(slot);

				if (!stack.isEmpty()) {
					var slotNbt = new CompoundTag();
					slotNbt.putByte("slot", (byte) (slot - from));
					slotNbt.put("item", stack.save(new CompoundTag()));
					slots.add(slotNbt);
				}
			}

			if (!slots.isEmpty()) {
				nbt.put(name, slots);
			}
		}

		private void readInventoryPart(@Nullable ListTag nbtList, int from) {
			if (nbtList == null) return;

			for (var nbt : nbtList) {
				var slotNbt = (CompoundTag) nbt;
				int slot = slotNbt.getByte("slot") + from;
				var item = ItemStack.of(slotNbt.getCompound("item"));

				this.setItem(slot, item);
			}
		}
	}

	private static DrawModifier modifierFromNbt(CompoundTag nbt) {
		var itemNbt = nbt.getCompound("item");
		Identifier id = Identifier.tryParse(itemNbt.getString("id"));

		if (id == null) return BlackboardColor.EMPTY;

		Item item = BuiltInRegistries.ITEM.get(id);
		var modifier = DrawModifier.fromItem(item);

		return modifier == null ? BlackboardColor.EMPTY : modifier;
	}
}
