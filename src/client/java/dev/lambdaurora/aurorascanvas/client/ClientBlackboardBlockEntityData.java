/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client;

import dev.lambdaurora.aurorascanvas.block.BlackboardBlock;
import dev.lambdaurora.aurorascanvas.block.entity.BlackboardBlockEntity;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasMeshBaker;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Environment(EnvType.CLIENT)
public final class ClientBlackboardBlockEntityData implements BlackboardBlockEntity.SidedData {
	private static final Map<BlackboardBlockEntity, ClientBlackboardBlockEntityData> ACTIVE_BLACKBOARDS = new Reference2ObjectOpenHashMap<>();

	private final BlackboardBlockEntity blackboardBlockEntity;
	private @Nullable Mesh mesh = null;
	private boolean meshDirty = true;

	public ClientBlackboardBlockEntityData(BlackboardBlockEntity blackboardBlockEntity) {
		this.blackboardBlockEntity = blackboardBlockEntity;
	}

	@Override
	public void markChanged() {
		this.refreshRendering();
	}

	@Override
	public void onRemoved() {
		ACTIVE_BLACKBOARDS.remove(this.blackboardBlockEntity);
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		if (this.meshDirty)
			this.rebuildMesh();
		return this.mesh;
	}

	public void markMeshDirty() {
		this.meshDirty = true;
	}

	private void rebuildMesh() {
		this.meshDirty = false;
		var canvas = this.blackboardBlockEntity.canvas();

		int light = canvas.isLit() ? 0xf000f0 : 0;
		this.mesh = CanvasMeshBaker.buildMesh(canvas, this.blackboardBlockEntity.getBlockState().getValue(BlackboardBlock.FACING), light);
	}

	private void refreshRendering() {
		if (this.blackboardBlockEntity.getLevel() instanceof ClientLevel clientWorld) {
			this.rebuildMesh();

			var pos = this.blackboardBlockEntity.getBlockPos();
			clientWorld.setSectionDirtyWithNeighbors(
					SectionPos.blockToSectionCoord(pos.getX()),
					SectionPos.blockToSectionCoord(pos.getY()),
					SectionPos.blockToSectionCoord(pos.getZ())
			);
		}
	}

	public static void markAllMeshesDirty() {
		ACTIVE_BLACKBOARDS.values().forEach(ClientBlackboardBlockEntityData::markMeshDirty);
	}

	public static void onLevelChange(@Nullable ClientLevel level) {
		ACTIVE_BLACKBOARDS.keySet().removeIf(blackboardBlockEntity ->
				blackboardBlockEntity.getLevel() == null || blackboardBlockEntity.getLevel() != level
		);
	}

	public static void init() {
		BlackboardBlockEntity.SIDED_LOGIC.register(blockEntity -> {
			var data = new ClientBlackboardBlockEntityData(blockEntity);
			ACTIVE_BLACKBOARDS.put(blockEntity, data);
			return data;
		});
	}
}
