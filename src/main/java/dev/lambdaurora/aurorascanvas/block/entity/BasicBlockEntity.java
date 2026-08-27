/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * Represents a basic block entity with common serialization and update packet code.
 *
 * @author LambdAurora
 * @version 1.1.0
 * @since 1.0.0
 */
public class BasicBlockEntity extends BlockEntity {
	public BasicBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	/**
	 * Attempts to synchronize the block entity data to the client.
	 *
	 * @throws IllegalStateException if called on the logical client
	 * @throws NullPointerException  if there's no level associated with the block entity
	 */
	protected void sync() {
		var level = this.getLevel();

		Objects.requireNonNull(level); // Maintain distinct failure case from below.
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.getChunkSource().blockChanged(this.getBlockPos());
		} else {
			throw new UnsupportedOperationException("Cannot call sync() on the logical client!");
		}
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		return this.saveWithoutMetadata(registries);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}
