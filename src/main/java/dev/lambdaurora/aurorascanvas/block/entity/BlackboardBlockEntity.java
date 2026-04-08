/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.block.entity;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.BlackboardBlock;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasHandler;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.yumi.commons.event.Event;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Represents a canvas block entity, stores the pixels of a canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardBlockEntity extends BasicBlockEntity implements Nameable, RenderAttachmentBlockEntity, CanvasHandler {
	public static final Event<Identifier, SidedLogic> SIDED_LOGIC = AurorasCanvas.EVENT_MANAGER.create(
			SidedLogic.class,
			sidedLogics -> blockEntity -> {
				for (var sidedLogic : sidedLogics) {
					return sidedLogic.onAdded(blockEntity);
				}

				return NoOpSidedData.INSTANCE;
			}
	);

	private final Canvas canvas = new AssignedCanvas();
	private @Nullable Component customName;

	public @Nullable Player lastUser;
	public int lastX;
	public int lastY;

	private SidedData sidedData = NoOpSidedData.INSTANCE;

	public BlackboardBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, pos, state);
	}

	public Canvas canvas() {
		return this.canvas;
	}

	@Override
	public short getPixel(int x, int y) {
		return this.canvas.getPixel(x, y);
	}

	@Override
	public boolean setPixel(int x, int y, int color) {
		if (this.canvas.setPixel(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.setChanged();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean brush(int x, int y, int color) {
		if (this.canvas.brush(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.setChanged();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean replace(int x, int y, int color) {
		if (this.canvas.replace(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.setChanged();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean fill(int x, int y, int color) {
		if (this.canvas.fill(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.setChanged();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean line(int x1, int y1, int x2, int y2, DrawModifier modifier) {
		if (this.canvas.line(x1, y1, x2, y2, modifier)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.setChanged();
			}
			return true;
		}
		return false;
	}

	public void copy(Canvas source) {
		this.canvas.copy(source);
		if (this.getLevel() instanceof ServerLevel) {
			this.sync();
			this.setChanged();
		}
	}

	/**
	 * Clears the canvas.
	 */
	public void clear() {
		this.canvas.clear();
		this.lastUser = null;
		if (this.getLevel() instanceof ServerLevel) {
			this.sync();
			this.setChanged();
		}
	}

	/**
	 * Returns whether this canvas is empty or not.
	 *
	 * @return {@code true} if empty, or {@code false} otherwise
	 */
	public boolean isEmpty() {
		return this.canvas.isEmpty();
	}

	@Override
	public @Nullable Component getCustomName() {
		return this.customName;
	}

	/**
	 * Sets the canvas custom name.
	 *
	 * @param customName the custom name
	 */
	public void setCustomName(@Nullable Component customName) {
		this.customName = customName;
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null;
	}

	@Override
	public Component getName() {
		return this.customName != null ? this.customName
				: Component.translatable(this.getBlockState().getBlock().getDescriptionId());
	}

	public boolean isLocked() {
		return ((BlackboardBlock) this.getBlockState().getBlock()).isLocked();
	}

	@Override
	public void setRemoved() {
		super.setRemoved();

		this.sidedData.onRemoved();
		this.sidedData = NoOpSidedData.INSTANCE;
	}

	@Override
	public void clearRemoved() {
		super.clearRemoved();

		this.sidedData = SIDED_LOGIC.invoker().onAdded(this);
	}

	/* Client */

	@Override
	public @Nullable Object getRenderAttachmentData() {
		return this.sidedData.getRenderAttachmentData();
	}

	/* Serialization */

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.readBlackBoardNbt(nbt);
		this.lastUser = null;

		if (this.level != null && this.level.isClientSide()) {
			this.sidedData.markChanged();
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		this.writeBlackBoardNbt(nbt);
	}

	public void readBlackBoardNbt(CompoundTag nbt) {
		this.canvas.readNbt(nbt);

		if (nbt.contains("custom_name", Tag.TAG_STRING)) {
			this.customName = Component.Serializer.fromJson(nbt.getString("custom_name"));
		}
	}

	public CompoundTag writeBlackBoardNbt(CompoundTag nbt) {
		this.canvas.writeNbt(nbt);
		if (this.customName != null) {
			nbt.putString("custom_name", Component.Serializer.toJson(this.customName));
		}
		return nbt;
	}

	public interface SidedData extends RenderAttachmentBlockEntity {
		void markChanged();

		void onRemoved();
	}

	static final class NoOpSidedData implements SidedData {
		static final SidedData INSTANCE = new NoOpSidedData();

		private NoOpSidedData() {}

		@Override
		public void markChanged() {}

		@Override
		public void onRemoved() {}

		@Override
		public @Nullable Object getRenderAttachmentData() {
			return null;
		}
	}

	@FunctionalInterface
	public interface SidedLogic {
		SidedData onAdded(BlackboardBlockEntity blockEntity);
	}

	private class AssignedCanvas extends Canvas {
		@Override
		public boolean isLit() {
			return BlackboardBlockEntity.this.getBlockState().getValue(BlackboardBlock.LIT);
		}

		@Override
		public void setLit(boolean lit) {}
	}
}
