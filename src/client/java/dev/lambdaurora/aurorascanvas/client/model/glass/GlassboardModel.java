/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.client.model.glass;

import com.mojang.math.Quadrant;
import net.minecraft.core.Direction;

import java.util.List;

public final class GlassboardModel {
	static final int LEFT_UP_MASK = 0b00000001;
	static final int UP_MASK = 0b00000010;
	static final int RIGHT_UP_MASK = 0b00000100;
	static final int LEFT_MASK = 0b00001000;
	static final int RIGHT_MASK = 0b00010000;
	static final int LEFT_DOWN_MASK = 0b00100000;
	static final int DOWN_MASK = 0b01000000;
	static final int RIGHT_DOWN_MASK = 0b10000000;
	static final int ALL_MASK = LEFT_UP_MASK | UP_MASK | RIGHT_UP_MASK | LEFT_MASK | RIGHT_MASK | LEFT_DOWN_MASK | DOWN_MASK | RIGHT_DOWN_MASK;

	public static String getModelPath(String prefix, Corner corner, Type type) {
		return "glassboard/" + prefix + "glassboard_" + corner.getShortName() + type.suffix();
	}

	public static int getCornerDataIndex(Corner corner, Type type) {
		return corner.ordinal() | (type.ordinal() << 2);
	}

	public static Quadrant yRot(Direction direction) {
		return switch (direction) {
			case NORTH -> Quadrant.R180;
			case EAST -> Quadrant.R270;
			case WEST -> Quadrant.R90;
			default -> Quadrant.R0;
		};
	}

	public static Quadrant partYRot(Direction direction) {
		return switch (direction) {
			case SOUTH -> Quadrant.R180;
			case WEST -> Quadrant.R270;
			case EAST -> Quadrant.R90;
			default -> Quadrant.R0;
		};
	}

	public enum Corner {
		LEFT_UP("lu"),
		RIGHT_UP("ru"),
		RIGHT_DOWN("rd"),
		LEFT_DOWN("ld");

		public static final List<Corner> CORNERS = List.of(values());
		private final String shortName;

		Corner(String shortName) {
			this.shortName = shortName;
		}

		public String getShortName() {
			return this.shortName;
		}
	}

	public enum Type {
		NONE(""),
		INNER("_inner"),
		HORIZONTAL("_horizontal"),
		VERTICAL("_vertical"),
		CENTER("_center");

		public static final List<Type> TYPES = List.of(values());
		private final String suffix;

		Type(String suffix) {
			this.suffix = suffix;
		}

		public String suffix() {
			return this.suffix;
		}
	}

	private GlassboardModel() {
		throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions");
	}
}
