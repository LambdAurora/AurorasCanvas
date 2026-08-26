/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client;

import dev.lambdaurora.aurorascanvas.block.entity.CanvasBlockEntity;
import dev.lambdaurora.aurorascanvas.client.renderer.CanvasMeshBaker;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public final class ClientCanvasBlockEntityData implements CanvasBlockEntity.SidedData {
	private static final Map<CanvasBlockEntity<?, ?>, ClientCanvasBlockEntityData> ACTIVE_BLACKBOARDS = new Reference2ObjectOpenHashMap<>();

	private final CanvasBlockEntity<?, ?> canvasBlockEntity;
	private List<Mesh> mesh = List.of();
	private boolean meshDirty = true;

	public ClientCanvasBlockEntityData(CanvasBlockEntity<?, ?> canvasBlockEntity) {
		this.canvasBlockEntity = canvasBlockEntity;
	}

	@Override
	public void markChanged() {
		this.refreshRendering();
	}

	@Override
	public void onRemoved() {
		ACTIVE_BLACKBOARDS.remove(this.canvasBlockEntity);
	}

	@Override
	public @Nullable Object getRenderAttachmentData() {
		if (this.meshDirty)
			this.rebuildMesh();
		return this.mesh.isEmpty() ? null : new RenderAttachmentData(this.mesh);
	}

	public void markMeshDirty() {
		this.meshDirty = true;
	}

	private void rebuildMesh() {
		this.meshDirty = false;

		this.mesh = this.canvasBlockEntity.canvases()
				.map(CanvasMeshBaker::buildMesh)
				.toList();
	}

	private void refreshRendering() {
		if (this.canvasBlockEntity.getLevel() instanceof ClientLevel clientWorld) {
			this.rebuildMesh();

			var pos = this.canvasBlockEntity.getBlockPos();
			clientWorld.setSectionDirtyWithNeighbors(
					SectionPos.blockToSectionCoord(pos.getX()),
					SectionPos.blockToSectionCoord(pos.getY()),
					SectionPos.blockToSectionCoord(pos.getZ())
			);
		}
	}

	public static void markAllMeshesDirty() {
		ACTIVE_BLACKBOARDS.values().forEach(ClientCanvasBlockEntityData::markMeshDirty);
	}

	public static void onLevelChange(@Nullable ClientLevel level) {
		ACTIVE_BLACKBOARDS.keySet().removeIf(canvasBlockEntity ->
				canvasBlockEntity.getLevel() == null || canvasBlockEntity.getLevel() != level
		);
	}

	public static void init() {
		CanvasBlockEntity.SIDED_LOGIC.register(blockEntity -> {
			var data = new ClientCanvasBlockEntityData(blockEntity);
			ACTIVE_BLACKBOARDS.put(blockEntity, data);
			return data;
		});
	}

	public record RenderAttachmentData(@Unmodifiable List<Mesh> meshes) {
	}
}
