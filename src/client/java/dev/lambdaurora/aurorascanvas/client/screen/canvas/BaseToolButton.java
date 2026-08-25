package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
class BaseToolButton extends Button {
	protected BaseToolButton(int x, int y, int width, int height, Component message, OnPress onPress, CreateNarration createNarration) {
		super(x, y, width, height, message, onPress, createNarration);
	}

	@Override
	protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(graphics);
	}

	protected void renderBackground(GuiGraphics graphics) {
		var client = Minecraft.getInstance();
		graphics.setColor(1.f, 1.f, 1.f, this.alpha);
		RenderSystem.enableBlend();
		RenderSystem.enableDepthTest();
		graphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, this.getTextureY());
		graphics.setColor(1.f, 1.f, 1.f, 1.f);
	}

	private int getTextureY() {
		int i = 1;
		if (!this.active) {
			i = 0;
		} else if (this.isHoveredOrFocused()) {
			i = 2;
		}

		return 46 + i * 20;
	}
}
