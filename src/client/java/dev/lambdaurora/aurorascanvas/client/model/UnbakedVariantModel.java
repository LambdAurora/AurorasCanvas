package dev.lambdaurora.aurorascanvas.client.model;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents an unbaked model which is using a variant map to adapt to depending on block states.
 *
 * @param <T> the type of the unbaked models of the variants
 * @author LambdAurora
 * @version 1.0.0
 * @since 1.0.0
 */
@Environment(EnvType.CLIENT)
public class UnbakedVariantModel<T extends UnbakedModel> implements UnbakedModel {
	private final Block block;
	private final Map<String, T> unbakedVariantMap;
	private final List<Property<?>> ignoreProperties;

	public UnbakedVariantModel(Block block, Map<String, T> variantMap, List<Property<?>> ignoreProperties) {
		this.block = block;
		this.unbakedVariantMap = variantMap;
		this.ignoreProperties = ignoreProperties;
	}

	@Override
	public Collection<Identifier> getDependencies() {
		return this.unbakedVariantMap.values().stream().flatMap(model -> model.getDependencies().stream()).collect(Collectors.toSet());
	}

	@Override
	public void resolveParents(Function<Identifier, UnbakedModel> models) {
		this.unbakedVariantMap.values().forEach(model -> model.resolveParents(models));
	}

	@Override
	public BakedModel bake(
			ModelBaker modelBaker, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, Identifier modelId
	) {
		var map = new Object2ReferenceOpenHashMap<String, BlockState>();
		var models = new Reference2ObjectOpenHashMap<BlockState, BakedModel>();

		this.block.getStateDefinition().getPossibleStates().forEach(state -> {
			map.put(this.propertyMapToString(state.getValues()), state);
		});

		this.unbakedVariantMap.forEach((variant, model) -> {
			models.put(map.get(variant), model.bake(modelBaker, textureGetter, modelState, modelId));
		});

		return new BakedVariantModel(models);
	}

	private String propertyMapToString(Map<Property<?>, Comparable<?>> map) {
		var builder = new StringBuilder();

		for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
			if (this.ignoreProperties.contains(entry.getKey())) {
				continue;
			}

			if (builder.length() != 0) {
				builder.append(',');
			}

			Property<?> property = entry.getKey();
			builder.append(property.getName());
			builder.append('=');
			builder.append(propertyValueToString(property, entry.getValue()));
		}

		return builder.toString();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> String propertyValueToString(Property<T> property, Comparable<?> value) {
		return property.getName((T) value);
	}
}
