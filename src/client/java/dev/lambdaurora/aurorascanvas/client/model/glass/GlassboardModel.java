package dev.lambdaurora.aurorascanvas.client.model.glass;

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

	static int getCornerDataIndex(Corner corner, Type type) {
		return corner.ordinal() | (type.ordinal() << 2);
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
