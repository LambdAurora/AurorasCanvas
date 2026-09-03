/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import dev.lambdaurora.aurorascanvas.AurorasCanvas;
import dev.lambdaurora.aurorascanvas.canvas.DrawAction;
import dev.lambdaurora.aurorascanvas.network.CanvasEditSubmitPayload;
import dev.lambdaurora.spruceui.render.SpruceGuiGraphics;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

@Environment(EnvType.CLIENT)
public class CanvasScreen extends SpruceScreen {
	private static final int PIXEL_SIZE = 8;
	final CanvasController controller;
	private int canvasStartX;
	private int canvasStartY;

	private @Nullable TrackedCanvasHandler currentHistory;

	public CanvasScreen(Component title, CanvasController controller) {
		super(title);
		this.controller = controller;
	}

	int canvasEndX() {
		return this.canvasStartX + 16 * PIXEL_SIZE;
	}

	int canvasEndY() {
		return this.canvasStartY + 16 * PIXEL_SIZE;
	}

	@Override
	public void onClose() {
		if (!this.controller.canvas.getDefault().history().effectiveCanvas().areContentsEqual(this.controller.root().getDefault())) {
			// Changed.
			var buffer = FriendlyByteBufs.create();
			var payload = new CanvasEditSubmitPayload(this.controller.id(), this.controller.canvas.mapToCanvas(canvas -> canvas.history().effectiveCanvas()));
			ClientPlayNetworking.getSender().sendPacket(payload);
		}

		super.onClose();
	}

	@Override
	protected void init() {
		super.init();

		this.canvasStartX = this.width / 2 - 8 * PIXEL_SIZE;
		this.canvasStartY = this.height / 2 - 8 * PIXEL_SIZE;

		this.controller.canvas.stream().map(TrackedCanvasHandler::history).forEach(CanvasHistory::clearListeners);
		int actionY = this.canvasStartY - 13;
		var buttons = new ArrayList<ToolButton>();

		for (var action : DrawAction.ACTIONS) {
			if (action == DrawAction.LINE) continue;

			buttons.add(this.addRenderableWidget(new ToolButton(
					controller,
					buttons,
					this.canvasStartX - 40,
					actionY,
					20, 20,
					action
			)));

			actionY += 22;
		}

		this.addRenderableWidget(new ActionButton(this.controller, Component.translatable(AurorasCanvas.NAMESPACE + ".tool.undo"),
				this.canvasStartX - 40, actionY,
				ActionButton.UNDO_SPRITE, ActionButton.DISABLED_UNDO_SPRITE,
				canvas -> canvas.history().undo(),
				canvas -> canvas.history().canUndo()
		));

		this.addRenderableWidget(new ActionButton(this.controller, Component.translatable(AurorasCanvas.NAMESPACE + ".tool.redo"),
				this.canvasStartX - 40, actionY += 22,
				ActionButton.REDO_SPRITE, ActionButton.DISABLED_REDO_SPRITE,
				canvas -> canvas.history().redo(),
				canvas -> canvas.history().canRedo()
		));

		this.addRenderableWidget(new ActionButton(this.controller, Component.translatable(AurorasCanvas.NAMESPACE + ".tool.clear"),
				this.canvasStartX - 40, actionY += 22,
				ActionButton.CLEAR_SPRITE, ActionButton.DISABLED_CLEAR_SPRITE,
				TrackedCanvasHandler::clear,
				canvas -> !canvas.isEmpty()
		));

		var selectorWidget = this.addRenderableWidget(new ColorSelectorWidget(
				this,
				this.canvasStartY,
				this.canvasEndX() + 20
		));

		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
				.bounds(this.width / 2 - 56 / 2 + 2, this.height / 2 + 8 * PIXEL_SIZE + 20, 52, 20)
				.build()
		);

		this.controller.canvas.stream().map(TrackedCanvasHandler::history).forEach(CanvasHistory::invokeListeners);
	}

	public void extractTitle(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.centeredShadowedText(this.font, this.title, this.width / 2, 20, 0xffffffff);
	}

	@Override
	public void extractRenderState(SpruceGuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);

		graphics.vanilla().blit(
				RenderPipelines.GUI_TEXTURED,
				this.controller.backgroundTexture,
				this.canvasStartX, this.canvasStartY, 0, 0,
				PIXEL_SIZE * 16, PIXEL_SIZE * 16, 16, 16,
				16, 16
		);

		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				int screenX = this.canvasStartX + x * PIXEL_SIZE;
				int screenY = this.canvasStartY + y * PIXEL_SIZE;

				var effectiveCanvas = this.currentHistory != null ? this.currentHistory : this.controller.canvas.getDefault();
				int color = effectiveCanvas.getColor(x, y);

				graphics.fill(screenX, screenY, screenX + PIXEL_SIZE, screenY + PIXEL_SIZE, color);
			}
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			if (event.x() >= this.canvasStartX && event.x() < this.canvasEndX() && event.y() >= this.canvasStartY && event.y() < this.canvasEndY()) {
				this.currentHistory = new TrackedCanvasHandler(this.controller.canvas.getDefault());
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseDragged(final MouseButtonEvent event, double dragX, double dragY) {
		if (event.button() == 0) {
			if (event.x() >= this.canvasStartX && event.x() < this.canvasEndX() && event.y() >= this.canvasStartY && event.y() < this.canvasEndY()) {
				int x = Math.min((int) ((event.x() - this.canvasStartX) / PIXEL_SIZE), 15);
				int y = Math.min((int) ((event.y() - this.canvasStartY) / PIXEL_SIZE), 15);

				if (this.currentHistory != null) {
					this.controller.currentAction.execute(this.currentHistory, x, y, this.controller.currentModifier);
					return true;
				}
			}

			if (this.currentHistory != null) {
				return true;
			}
		}

		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (event.button() == 0) {
			if (event.x() >= this.canvasStartX && event.x() < this.canvasEndX() && event.y() >= this.canvasStartY && event.y() < this.canvasEndY()) {
				int x = Math.min((int) ((event.x() - this.canvasStartX) / PIXEL_SIZE), 15);
				int y = Math.min((int) ((event.y() - this.canvasStartY) / PIXEL_SIZE), 15);

				if (this.currentHistory != null) {
					this.controller.currentAction.execute(this.currentHistory, x, y, this.controller.currentModifier);
					this.controller.canvas.getDefault().history().push(this.currentHistory.history().fold());
					this.currentHistory = null;
					return true;
				}
			}

			if (this.currentHistory != null) {
				this.controller.canvas.getDefault().history().push(this.currentHistory.history().fold());
				this.currentHistory = null;
				return true;
			}
		}

		return super.mouseReleased(event);
	}
}
