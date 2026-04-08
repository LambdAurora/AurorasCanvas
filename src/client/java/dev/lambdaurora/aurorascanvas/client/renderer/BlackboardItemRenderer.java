package dev.lambdaurora.aurorascanvas.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.client.AurorasCanvasClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Represents the dynamic item renderer of blackboards.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class BlackboardItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
	private final ModelResourceLocation modelId;

	public BlackboardItemRenderer(ModelResourceLocation modelId) {
		this.modelId = modelId;
	}

	@Override
	public void render(
			ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
			MultiBufferSource vertexConsumers, int light, int overlay
	) {
		var model = Minecraft.getInstance().getModelManager().getModel(this.modelId);

		matrices.pushPose();

		matrices.translate(0.5, 0.5, 0.5);
		boolean leftHanded = mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
		if (mode == ItemDisplayContext.HEAD) {
			var maskModel = Minecraft.getInstance().getModelManager().getModel(AurorasCanvasClient.BLACKBOARD_MASK);
			Minecraft.getInstance().getItemRenderer().render(
					stack, mode,
					false, matrices, vertexConsumers, light, overlay, maskModel
			);
		}

		matrices.pushPose();
		var nbt = BlockItem.getBlockEntityData(stack);
		if (nbt != null && nbt.contains("pixels", Tag.TAG_BYTE_ARRAY)) {
			float z = .933f;
			if (mode == ItemDisplayContext.HEAD) {
				matrices.translate(0.5, 0.5, z);
				matrices.scale(-1, -1, 1);
			} else if (mode == ItemDisplayContext.GUI) {
				matrices.translate(0.27, -0.08, 0);
				matrices.scale(-1, -1, 1);
			} else if (mode == ItemDisplayContext.GROUND) {
				matrices.translate(0.125, 0.5, 0.23333333);
				matrices.scale(-1, -1, 1);
			} else if (mode != ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
					&& mode != ItemDisplayContext.THIRD_PERSON_LEFT_HAND && !mode.firstPerson()) {
				matrices.scale(-1, -1, 1);
			}

			model.getTransforms().getTransform(mode).apply(leftHanded, matrices);
			matrices.translate(0, 0, -0.5);

			if (mode == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
					|| mode == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
					|| mode.firstPerson()) {
				matrices.translate(0.5, 0.5, z);
				matrices.scale(-1, -1, 1);
			}

			var canvas = Canvas.fromNbt(nbt);
			CanvasTexture.fromCanvas(canvas)
					.render(
							matrices.last().pose(), vertexConsumers,
							canvas.isLit() ? LightTexture.FULL_BLOCK : light,
							false
					);

			if (stack.getDescriptionId().contains("glass")) {
				CanvasTexture.fromCanvas(canvas)
						.render(
								matrices.last().pose(), vertexConsumers,
								canvas.isLit() ? LightTexture.FULL_BLOCK : light,
								true
						);
			}
		}
		matrices.popPose();

		Minecraft.getInstance().getItemRenderer().render(
				stack, mode,
				leftHanded,
				matrices, vertexConsumers, light, overlay, model
		);

		matrices.popPose();
	}
}
