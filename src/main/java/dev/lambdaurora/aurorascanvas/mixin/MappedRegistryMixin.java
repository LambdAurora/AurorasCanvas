/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.mixin;

import dev.lambdaurora.aurorascanvas.util.FabricRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Taken from Fabric API due to a lack of registry alias API in 1.20,
// please see https://github.com/FabricMC/fabric-api/blob/1.21.1/fabric-registry-sync-v0/src/main/java/net/fabricmc/fabric/mixin/registry/sync/SimpleRegistryMixin.java
// for the original source and license terms.
@Mixin(MappedRegistry.class)
public abstract class MappedRegistryMixin<T> implements FabricRegistry {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger(MappedRegistryMixin.class);

	@Shadow
	@Final
	ResourceKey<? extends Registry<T>> key;

	@Shadow
	@Final
	private Map<Identifier, Holder.Reference<T>> byLocation;

	@Shadow
	protected abstract void validateWrite();

	@Unique
	// invariant: the sets of keys and values are disjoint (every alias points to a 'deepest' non-alias ID)
	private Map<Identifier, Identifier> aurorascanvas$aliases = new HashMap<>();

	@Override
	public void aurorascanvas$addAlias(Identifier old, Identifier newId) {
		Objects.requireNonNull(old, "alias cannot be null");
		Objects.requireNonNull(newId, "aliased id cannot be null");

		if (this.aurorascanvas$aliases.containsKey(old)) {
			throw new IllegalArgumentException(
					"Tried adding %s as an alias for %s, but it is already an alias (for %s) in registry %s".formatted(
							old,
							newId,
							this.aurorascanvas$aliases.get(old),
							this.key
					)
			);
		}

		if (this.byLocation.containsKey(old)) {
			throw new IllegalArgumentException(
					"Tried adding %s as an alias, but it is already present in registry %s".formatted(
							old,
							this.key
					)
			);
		}

		if (old.equals(this.aurorascanvas$aliases.get(newId))) {
			// since an alias corresponds to at most one identifier, this is the only way to create a cycle
			// that doesn't already fall under the first condition
			throw new IllegalArgumentException(
					"Making %1$s an alias of %2$s would create a cycle, as %2$s is already an alias of %1$s (registry %3$s)".formatted(
							old,
							newId,
							this.key
					)
			);
		}

		if (!this.byLocation.containsKey(newId)) {
			LOGGER.warn(
					"Adding {} as an alias for {}, but the latter doesn't exist in registry {}",
					old,
					newId,
					this.key
			);
		}

		this.validateWrite();

		// recompute alias map to preserve invariant, i.e. make sure all keys point to a non-alias ID
		Identifier deepest = this.aurorascanvas$aliases.getOrDefault(newId, newId);

		for (Map.Entry<Identifier, Identifier> entry : this.aurorascanvas$aliases.entrySet()) {
			if (old.equals(entry.getValue())) {
				entry.setValue(deepest);
			}
		}

		this.aurorascanvas$aliases.put(old, deepest);
		LOGGER.debug("Adding alias {} for {} in registry {}", old, newId, this.key);
	}

	@ModifyVariable(
			method = {
					"get(Lnet/minecraft/resources/Identifier;)Ljava/lang/Object;",
					"containsKey(Lnet/minecraft/resources/Identifier;)Z"
			},
			at = @At("HEAD"),
			argsOnly = true
	)
	private Identifier aliasIdentifierParameter(Identifier original) {
		return this.aurorascanvas$aliases.getOrDefault(original, original);
	}

	@ModifyVariable(
			method = {
					"get(Lnet/minecraft/resources/ResourceKey;)Ljava/lang/Object;",
					"getHolder(Lnet/minecraft/resources/ResourceKey;)Ljava/util/Optional;",
					"getOrCreateHolderOrThrow",
					"containsKey(Lnet/minecraft/resources/ResourceKey;)Z",
			},
			at = @At("HEAD"),
			argsOnly = true
	)
	private ResourceKey<T> aurorascanvas$aliasRegistryKeyParameter(ResourceKey<T> original) {
		if (original == null) {
			return null;
		}

		Identifier aliased = this.aurorascanvas$aliases.get(original.identifier());
		return aliased == null ? original : ResourceKey.create(original.registry(), aliased);
	}
}
