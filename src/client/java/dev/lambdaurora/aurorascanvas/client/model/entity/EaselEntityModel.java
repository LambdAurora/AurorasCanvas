/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.lambdaurora.aurorascanvas.entity.EaselEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/**
 * Represents the easel entity model.
 *
 * @param <T> the easel entity type
 */
public class EaselEntityModel<T extends EaselEntity> extends EntityModel<T> {
	public static final PartPose FRONT_PART_POSE = PartPose.offsetAndRotation(0.f, 24.f, -9.f, -0.2618f, 0.f, 0.f);
	private final ModelPart root;

	public EaselEntityModel(ModelPart root) {
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		var mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition front = root.addOrReplaceChild("front",
				CubeListBuilder.create()
						.texOffs(0, 34)
						.addBox(-8.f, -16.f, -2.f, 16.f, 2.f, 2.f, CubeDeformation.NONE),
				FRONT_PART_POSE
		);

		{
			PartDefinition frontLegs = front.addOrReplaceChild("front_legs", CubeListBuilder.create(), PartPose.offset(0.f, 0.f, 1.f));
			frontLegs.addOrReplaceChild("right_leg",
					CubeListBuilder.create()
							.texOffs(0, 0)
							.addBox(-1.f, -32.f, -1.f, 2.f, 32.f, 2.f, CubeDeformation.NONE),
					PartPose.offsetAndRotation(7.f, 0.f, 0.f, 0.f, 0.f, -0.1745f)
			);
			frontLegs.addOrReplaceChild("left_leg",
					CubeListBuilder.create()
							.texOffs(0, 0)
							.addBox(-1.f, -32.f, -1.f, 2.f, 32.f, 2.f, CubeDeformation.NONE),
					PartPose.offsetAndRotation(-7.f, 0.f, 0.f, 0.f, 0.f, 0.1745f)
			);
		}

		root.addOrReplaceChild("hind_leg",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-1.f, -32.f, -1.f, 2.f, 32.f, 2.f, CubeDeformation.NONE),
				PartPose.offsetAndRotation(0.f, 24.f, 8.f, 0.2618f, 0.f, 0.f)
		);

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(EaselEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(
			PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha
	) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
