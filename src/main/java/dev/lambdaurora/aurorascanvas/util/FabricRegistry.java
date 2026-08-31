/*
 * Copyright © 2026 LambdAurora <email@lambdaurora.dev>
 *
 * This file is part of Aurora's Canvas.
 *
 * Licensed under the Lambda License. For more information,
 * see the LICENSE file.
 */

package dev.lambdaurora.aurorascanvas.util;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * General-purpose Fabric-provided extensions for {@link net.minecraft.core.Registry} objects.
 *
 * <p>Note: This interface is automatically implemented on all registries via Mixin and interface injection.</p>
 */
@ApiStatus.NonExtendable
public interface FabricRegistry {
	/**
	 * Adds an alias for an entry in this registry. Once added, all queries to this registry that refer to the {@code old}
	 * {@link Identifier} will be redirected towards {@code newId}. This is useful if a mod wants to change an ID without
	 * breaking compatibility with existing worlds.
	 * @param old the {@link Identifier} that will become an alias for {@code newId}
	 * @param newId the {@link Identifier} for which {@code old} will become an alias
	 */
	default void aurorascanvas$addAlias(Identifier old, Identifier newId) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
