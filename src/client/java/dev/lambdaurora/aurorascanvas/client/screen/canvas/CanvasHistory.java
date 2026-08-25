/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.screen.canvas;

import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import dev.lambdaurora.aurorascanvas.canvas.CanvasHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.*;
import java.util.function.Consumer;

/**
 * Represents the history of actions done to a canvas.
 *
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class CanvasHistory {
	private final Deque<Entry> history = new ArrayDeque<>();
	private final Deque<Entry> future = new ArrayDeque<>();
	private final CanvasHandler root;
	private Canvas effectiveCanvas;

	final List<Consumer<CanvasHistory>> listeners = new ArrayList<>();

	public CanvasHistory(CanvasHandler root) {
		this.root = root;

		if (root instanceof Canvas canvas) {
			this.effectiveCanvas = canvas;
		} else {
			this.effectiveCanvas = new Canvas();
			this.effectiveCanvas.copy(root);
		}
	}

	/**
	 * {@return the effective canvas}
	 */
	Canvas effectiveCanvas() {
		return this.effectiveCanvas;
	}

	/**
	 * {@return {@code true} if an action can be undone, or {@code false} otherwise}
	 */
	public boolean canUndo() {
		return !this.history.isEmpty();
	}

	/**
	 * {@return {@code true} if an action can be redone, or {@code false} otherwise}
	 */
	public boolean canRedo() {
		return !this.future.isEmpty();
	}

	/**
	 * Pushes an entry to the history.
	 *
	 * @param entry the entry to push
	 * @return {@code true} if the entry has resulted in an edit and has been recorded, or {@code false} otherwise
	 */
	boolean push(Entry entry) {
		this.history.push(entry);

		var result = this.computeResult();

		if (!Arrays.equals(result.getPixels(), this.effectiveCanvas.getPixels())) {
			this.future.clear();
			this.effectiveCanvas = result;
			this.invokeListeners();
			return true;
		} else {
			this.history.pop();
			return false;
		}
	}

	/**
	 * Undoes the last action, if any.
	 */
	void undo() {
		if (!this.canUndo()) return;

		var entry = this.history.pop();
		this.future.push(entry);

		this.computeEffective();
	}

	/**
	 * Redoes an undone action, if any.
	 */
	void redo() {
		if (!this.canRedo()) return;

		var entry = this.future.pop();
		this.history.push(entry);
		this.computeEffective();
	}

	private Canvas computeResult() {
		var canvas = new Canvas();
		canvas.copy(this.root);

		var it = this.history.descendingIterator();
		while (it.hasNext()) {
			it.next().apply(canvas);
		}

		return canvas;
	}

	private void computeEffective() {
		this.effectiveCanvas = this.computeResult();
		this.invokeListeners();
	}

	void clearListeners() {
		this.listeners.clear();
	}

	void addListener(Consumer<CanvasHistory> listener) {
		this.listeners.add(listener);
	}

	void invokeListeners() {
		this.listeners.forEach(listener -> listener.accept(this));
	}

	/**
	 * Folds this history into a single composite history entry.
	 *
	 * @return the composite entry
	 */
	public Entry fold() {
		if (this.history.size() == 1) {
			return this.history.getFirst();
		}

		var list = new ArrayList<Entry>();
		var it = this.history.descendingIterator();
		while (it.hasNext()) {
			list.add(it.next());
		}

		return new Entry.Composite(list);
	}

	@FunctionalInterface
	public interface Entry {
		void apply(CanvasHandler handler);

		record Composite(Collection<Entry> entries) implements Entry {
			@Override
			public void apply(CanvasHandler handler) {
				this.entries.forEach(entry -> entry.apply(handler));
			}
		}
	}
}
