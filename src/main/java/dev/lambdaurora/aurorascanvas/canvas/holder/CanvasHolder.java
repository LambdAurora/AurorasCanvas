package dev.lambdaurora.aurorascanvas.canvas.holder;

import com.mojang.serialization.Codec;
import dev.lambdaurora.aurorascanvas.canvas.Canvas;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Contract;

import java.util.Map;

public interface CanvasHolder<H extends CanvasHolder<H>> extends CanvasLikeHolder<Canvas> {
	Type<H> type();

	void writeBuffer(FriendlyByteBuf buffer);

	@SuppressWarnings("unchecked")
	default CompoundTag toNbt() {
		return this.type().toNbt((H) this);
	}

	static CanvasHolder<?> fromBuffer(FriendlyByteBuf buffer) {
		var typeName = buffer.readUtf();
		var type = Registry.REGISTRY.get(typeName);

		if (type == null) {
			throw new IllegalArgumentException("Invalid type " + typeName + ".");
		}

		return type.fromBuffer(buffer);
	}

	interface Type<H extends CanvasHolder<H>> {
		String name();

		Codec<H> codec();

		@Contract("-> new")
		H createDefault();

		default H fromNbt(CompoundTag nbt) {
			return this.codec().parse(NbtOps.INSTANCE, nbt).result().orElseGet(this::createDefault);
		}

		default CompoundTag toNbt(H holder) {
			var encoded = this.codec().encodeStart(NbtOps.INSTANCE, holder)
					.getOrThrow(false, message -> {});

			if (!(encoded instanceof CompoundTag encodedNbt))
				throw new IllegalStateException("Canvases codec did not encode into a NBT compound.");

			return encodedNbt;
		}

		H fromBuffer(FriendlyByteBuf buffer);
	}

	final class Registry {
		public static final Map<String, Type<? extends CanvasHolder<?>>> REGISTRY = Map.of(
				"simple", SimpleCanvasHolder.TYPE,
				"glass", GlassCanvasHolder.TYPE
		);

		private Registry() {
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " only contains static definitions.");
		}
	}
}
