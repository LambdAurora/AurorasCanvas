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
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasHandler;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import dev.lambdaurora.aurorascanvas.canvas.PlacedCanvas;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasHolder;
import dev.lambdaurora.aurorascanvas.canvas.holder.CanvasLikeHolder;
import dev.yumi.commons.event.Event;
import net.fabricmc.fabric.api.blockview.v2.RenderDataBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.stream.Stream;

public abstract class CanvasBlockEntity<P, T extends CanvasHolder<P, T>, S extends CanvasLikeHolder<CanvasBlockEntity.SyncedCanvas>>
		extends BasicBlockEntity implements Nameable, RenderDataBlockEntity {
	public static final Event<Identifier, SidedLogic> SIDED_LOGIC = AurorasCanvas.EVENT_MANAGER.create(
			SidedLogic.class,
			sidedLogics -> blockEntity -> {
				for (var sidedLogic : sidedLogics) {
					return sidedLogic.onAdded(blockEntity);
				}

				return NoOpSidedData.INSTANCE;
			}
	);

	private static final String CANVAS_KEY = "canvas";
	private static final String CUSTOM_NAME_KEY = "custom_name";

	protected final S canvases;
	private @Nullable Component customName;

	private SidedData sidedData = NoOpSidedData.INSTANCE;

	@SuppressWarnings("unchecked")
	protected CanvasBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
		this.canvases = (S) this.canvasType().createDefault().map(canvas -> new SyncedCanvas(this, canvas));
	}

	/**
	 * {@return the canvases type to use for this canvas block entity}
	 */
	protected abstract CanvasHolder.Type<T> canvasType();

	protected abstract P getPlacementData();

	/**
	 * {@return the canvases associated with this block entity}
	 */
	public @Unmodifiable Stream<PlacedCanvas> canvases() {
		return this.getCanvasHolder().streamPlaced(this.getPlacementData());
	}

	public abstract SyncedCanvas getSyncedCanvas(Direction facing);

	@SuppressWarnings("unchecked")
	private T getCanvasHolder() {
		return (T) this.canvases.mapToCanvas(SyncedCanvas::getCanvas);
	}

	/**
	 * Clears the source.
	 */
	public void clear() {
		this.canvases().forEach(CanvasHandler::clear);
		if (this.getLevel() instanceof ServerLevel) {
			this.sync();
			this.setChanged();
		}
	}

	/**
	 * Returns whether this source is empty or not.
	 *
	 * @return {@code true} if empty, or {@code false} otherwise
	 */
	public boolean isEmpty() {
		return this.canvases.isEmpty();
	}

	@Override
	public @Nullable Component getCustomName() {
		return this.customName;
	}

	/**
	 * Sets the source custom name.
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
	public @Nullable Object getRenderData() {
		return this.sidedData.getRenderData();
	}

	/* Serialization */

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		this.customName = Component.Serializer.fromJson(nbt.getString(CUSTOM_NAME_KEY));
		this.canvasType().fromNbt(nbt.getCompound(CANVAS_KEY)).into(this.canvases, SyncedCanvas::setCanvas);

		if (this.level != null && this.level.isClientSide()) {
			this.sidedData.markChanged();
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		this.writeCanvasNbt(nbt);
	}

	public CompoundTag writeCanvasNbt(CompoundTag nbt) {
		if (this.customName != null) {
			nbt.putString(CUSTOM_NAME_KEY, Component.Serializer.toJson(this.customName));
		}

		nbt.put(CANVAS_KEY, this.canvasType().toNbt(this.getCanvasHolder()));

		return nbt;
	}

	public interface SidedData extends RenderDataBlockEntity {
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
		public @Nullable Object getRenderData() {
			return null;
		}
	}

	@FunctionalInterface
	public interface SidedLogic {
		SidedData onAdded(CanvasBlockEntity<?, ?, ?> blockEntity);
	}

	public static final class SyncedCanvas implements CanvasHandler {
		private final CanvasBlockEntity<?, ?, ?> parent;
		private Canvas canvas;

		private @Nullable WeakReference<Player> lastUser = null;
		private int lastX;
		private int lastY;

		public SyncedCanvas(CanvasBlockEntity<?, ?, ?> parent, Canvas canvas) {
			this.parent = parent;
			this.canvas = canvas;
		}

		Canvas getCanvas() {
			return this.canvas;
		}

		void setCanvas(Canvas canvas) {
			this.lastUser = null;
			this.canvas = canvas;
		}

		@Override
		public short getRawPixel(int x, int y) {
			return this.canvas.getRawPixel(x, y);
		}

		@Override
		public boolean setPixel(int x, int y, int color) {
			if (this.canvas.setPixel(x, y, color)) {
				this.doSync();
				return true;
			}
			return false;
		}

		@Override
		public boolean drawBrush(int x, int y, DrawModifier modifier) {
			if (this.canvas.drawBrush(x, y, modifier)) {
				this.doSync();
				return true;
			}
			return false;
		}

		@Override
		public boolean replaceColor(int x, int y, int color) {
			if (this.canvas.replaceColor(x, y, color)) {
				this.doSync();
				return true;
			}
			return false;
		}

		@Override
		public boolean drawLine(int x1, int y1, int x2, int y2, DrawModifier modifier) {
			if (this.canvas.drawLine(x1, y1, x2, y2, modifier)) {
				this.doSync();
				return true;
			}
			return false;
		}

		public boolean tryDrawLine(Player player, int x, int y, DrawModifier modifier) {
			if (this.lastUser == null || this.lastUser.get() != player) {
				this.lastUser = new WeakReference<>(player);
				this.lastX = x;
				this.lastY = y;
				return false;
			} else {
				boolean result = this.drawLine(this.lastX, this.lastY, x, y, modifier);
				this.lastUser = null;
				return result;
			}
		}

		@Override
		public boolean fillColor(int x, int y, int color) {
			if (this.canvas.fillColor(x, y, color)) {
				this.doSync();
				return true;
			}
			return false;
		}

		@Override
		public boolean isGlowing() {
			return this.canvas.isGlowing();
		}

		@Override
		public void setGlowing(boolean glowing) {
			this.canvas.setGlowing(glowing);
			this.doSync();
		}

		@Override
		public void copy(CanvasHandler source) {
			this.canvas.copy(source);
			this.doSync();
		}

		@Override
		public boolean isEmpty() {
			return this.canvas.isEmpty();
		}

		@Override
		public void clear() {
			this.lastUser = null;
			this.canvas.clear();
			this.doSync();
		}

		SyncedCanvas access() {
			if (this.lastUser != null) {
				var lastUser = this.lastUser.get();

				if (lastUser != null && lastUser.isRemoved()) {
					this.lastUser = null;
				}
			}

			return this;
		}

		private void doSync() {
			if (this.parent.getLevel() instanceof ServerLevel) {
				this.parent.sync();
				this.parent.setChanged();
			}
		}
	}
}
