package dev.lambdaurora.aurorascanvas.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.lambdaurora.aurorascanvas.canvas.BlackboardColor;
import dev.lambdaurora.aurorascanvas.item.PainterPaletteItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

/**
 * Represents the painter's palette tooltip component.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class PainterPaletteTooltipComponent implements ClientTooltipComponent {
	private final PainterPaletteItem.PainterPaletteInventory inventory;
	private final Component selectedToolText;

	public PainterPaletteTooltipComponent(PainterPaletteItem.PainterPaletteInventory inventory) {
		this.inventory = inventory;
		var enabledFeatures = Minecraft.getInstance().level.enabledFeatures();
		this.selectedToolText = PainterPaletteItem.getSelectedToolMessage(inventory, enabledFeatures).withStyle(ChatFormatting.GRAY);
	}

	@Override
	public int getHeight() {
		int height = 12;
		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return height;

		return height + 24;
	}

	@Override
	public int getWidth(Font font) {
		int width = font.width(this.selectedToolText);

		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return width;

		return Math.max(width, 18 * 5 + 2);
	}

	@Override
	public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource immediate) {
		font.drawInBatch(
				this.selectedToolText, x, y, 0xffffffff, true, matrix4f, immediate, Font.DisplayMode.NORMAL,
				0, LightTexture.FULL_BRIGHT
		);
	}

	@Override
	public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
		ItemStack primaryColorStack = this.inventory.getSelectedColor();

		if (primaryColorStack.isEmpty()) return;

		var matrices = graphics.pose();
		ItemStack previousColorStack = this.inventory.getPreviousColorStack();
		ItemStack nextColorStack = this.inventory.getNextColorStack();

		matrices.pushPose();
		y += 12;

		matrices.translate(x, y, 0);
		this.drawSlot(graphics, previousColorStack, inventory.getSlotOf(previousColorStack), true, false);

		matrices.translate(18, 0, 0);
		this.drawSlot(graphics, primaryColorStack, inventory.getSelectedColorSlot(), false, false);
		AbstractContainerScreen.renderSlotHighlight(graphics, 2, 2, 0);

		matrices.translate(18, 0, 0);
		this.drawSlot(graphics, nextColorStack, inventory.getSlotOf(nextColorStack), false, true);

		matrices.popPose();
	}

	private void drawSlot(
			GuiGraphics graphics, ItemStack stack,
			int index, boolean start, boolean end
	) {
		this.drawSlotPart(graphics, 1, 1, 0, 0, 0, 18, 20);

		if (start) this.drawSlotPart(graphics, 0, 0, 0, 0, 20, 1, 1);
		if (end) this.drawSlotPart(graphics, 0, 0, 0, 0, 20, 1, 1);

		this.drawSlotPart(graphics, 1, 0, 0, 0, 20, 18, 1);
		this.drawSlotPart(graphics, 1, 20, 0, 0, 60, 18, 1);

		if (start) this.drawSlotPart(graphics, 0, 0, 0, 0, 18, 1, 20);
		if (end) this.drawSlotPart(graphics, 18 + 1, 0, 0, 0, 18, 1, 20);

		if (start) this.drawSlotPart(graphics, 0, 20, 0, 0, 60, 1, 1);
		if (end) this.drawSlotPart(graphics, 18 + 1, 20, 0, 0, 60, 1, 1);

		if (!stack.isEmpty()) {
			graphics.renderItem(stack, 2, 2, index);
			this.drawColorOverlay(graphics, stack);
		}
	}

	private void drawColorOverlay(GuiGraphics graphics, ItemStack stack) {
		var color = BlackboardColor.fromItem(stack.getItem());
		if (color != null) {
			var matrices = graphics.pose();
			matrices.pushPose();
			matrices.translate(14, 14, 210);
			graphics.fill(0, 0, 4, 4, color.getColor());
			matrices.popPose();
		}
	}

	private void drawSlotPart(GuiGraphics graphics, int x, int y, int z, float u, float v, int width, int height) {
		graphics.setColor(1.f, 1.f, 1.f, 1.f);
		RenderSystem.setShaderTexture(0, ClientBundleTooltip.TEXTURE_LOCATION);
		graphics.blit(ClientBundleTooltip.TEXTURE_LOCATION, x, y, 0, u, v, width, height, 128, 128);
	}
}
