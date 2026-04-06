/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.block.entity;

import dev.lambdaurora.aurorascanvas.AurorasCanvasRegistry;
import dev.lambdaurora.aurorascanvas.block.BlackboardBlock;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasHandler;
import dev.lambdaurora.aurorascanvas.canvas.DrawModifier;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * Represents a blackboard block entity, stores the pixels of a blackboard.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
public class BlackboardBlockEntity extends BasicBlockEntity implements Nameable, RenderAttachmentBlockEntity, CanvasHandler {
	@ClientOnly
	private static final Set<BlackboardBlockEntity> ACTIVE_BLACKBOARDS = new ObjectOpenHashSet<>();
	private final Canvas blackboard = new AssignedCanvas();
	private @Nullable Component customName;

	public PlayerEntity lastUser;
	public int lastX;
	public int lastY;

	@ClientOnly
	private Mesh mesh = null;
	@ClientOnly
	private boolean meshDirty = true;

	public BlackboardBlockEntity(BlockPos pos, BlockState state) {
		super(AurorasCanvasRegistry.BLACKBOARD_BLOCK_ENTITY_TYPE, pos, state);
	}

	@Override
	public short getPixel(int x, int y) {
		return this.blackboard.getPixel(x, y);
	}

	@Override
	public boolean setPixel(int x, int y, int color) {
		if (this.blackboard.setPixel(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean brush(int x, int y, int color) {
		if (this.blackboard.brush(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean replace(int x, int y, int color) {
		if (this.blackboard.replace(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean fill(int x, int y, int color) {
		if (this.blackboard.fill(x, y, color)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean line(int x1, int y1, int x2, int y2, DrawModifier modifier) {
		if (this.blackboard.line(x1, y1, x2, y2, modifier)) {
			if (this.getLevel() instanceof ServerLevel) {
				this.sync();
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	public void copy(Canvas source) {
		this.blackboard.copy(source);
		if (this.getLevel() instanceof ServerLevel) {
			this.sync();
			this.markDirty();
		}
	}

	/**
	 * Clears the blackboard.
	 */
	public void clear() {
		this.blackboard.clear();
		this.lastUser = null;
		if (this.getWorld() instanceof ServerWorld) {
			this.sync();
			this.markDirty();
		}
	}

	/**
	 * Returns whether this blackboard is empty or not.
	 *
	 * @return {@code true} if empty, or {@code false} otherwise
	 */
	public boolean isEmpty() {
		return this.blackboard.isEmpty();
	}

	@Override
	public @Nullable Text getCustomName() {
		return this.customName;
	}

	/**
	 * Sets the blackboard custom name.
	 *
	 * @param customName the custom name
	 */
	public void setCustomName(@Nullable Text customName) {
		this.customName = customName;
	}

	@Override
	public boolean hasCustomName() {
		return this.customName != null;
	}

	@Override
	public Component getName() {
		return this.customName != null ? this.customName
				: Text.translatable(this.getCachedState().getBlock().getTranslationKey());
	}

	public boolean isLocked() {
		return ((BlackboardBlock) this.getCachedState().getBlock()).isLocked();
	}

	@Override
	public void markRemoved() {
		super.markRemoved();

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			this.markBlackboardRemoved();
		}
	}

	@Override
	public void cancelRemoval() {
		super.cancelRemoval();

		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			ACTIVE_BLACKBOARDS.add(this);
		}
	}

	/* Client */

	@Override
	public @Nullable Object getRenderAttachmentData() {
		if (this.meshDirty)
			this.rebuildMesh();
		return this.mesh;
	}

	@ClientOnly
	public void markMeshDirty() {
		this.meshDirty = true;
	}

	@ClientOnly
	private void rebuildMesh() {
		this.meshDirty = false;
		int light = this.blackboard.isLit() ? 0xf000f0 : 0;
		this.mesh = this.blackboard.buildMesh(this.getCachedState().get(BlackboardBlock.FACING), light);
	}

	@ClientOnly
	public void markBlackboardRemoved() {
		ACTIVE_BLACKBOARDS.remove(this);
	}

	@ClientOnly
	public static void markAllMeshesDirty() {
		ACTIVE_BLACKBOARDS.forEach(BlackboardBlockEntity::markMeshDirty);
	}

	@ClientOnly
	public static void onWorldChange(@Nullable ClientWorld world) {
		ACTIVE_BLACKBOARDS.removeIf(blackboardBlockEntity -> blackboardBlockEntity.world == null
				|| blackboardBlockEntity.world != world);
	}

	/* Serialization */

	@Override
	public void readNbt(CompoundTag nbt) {
		super.readNbt(nbt);
		this.readBlackBoardNbt(nbt);
		this.lastUser = null;
		if (this.world != null && this.world.isClient()) {
			this.refreshRendering();
		}
	}

	public void refreshRendering() {
		if (this.world instanceof ClientWorld clientWorld) {
			this.rebuildMesh();
			clientWorld.scheduleBlockRenders(
					ChunkSectionPos.getSectionCoord(this.getPos().getX()),
					ChunkSectionPos.getSectionCoord(this.getPos().getY()),
					ChunkSectionPos.getSectionCoord(this.getPos().getZ())
			);
		}
	}

	@Override
	public void writeNbt(CompoundTag nbt) {
		super.writeNbt(nbt);
		this.writeBlackBoardNbt(nbt);
	}

	public void readBlackBoardNbt(CompoundTag nbt) {
		this.blackboard.readNbt(nbt);

		if (nbt.contains("custom_name", NbtElement.STRING_TYPE)) {
			this.customName = Text.Serializer.fromJson(nbt.getString("custom_name"));
		}
	}

	public CompoundTag writeBlackBoardNbt(CompoundTag nbt) {
		this.blackboard.writeNbt(nbt);
		if (this.customName != null) {
			nbt.putString("custom_name", Text.Serializer.toJson(this.customName));
		}
		return nbt;
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
