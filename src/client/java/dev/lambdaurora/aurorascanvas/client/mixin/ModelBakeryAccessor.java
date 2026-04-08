package dev.lambdaurora.aurorascanvas.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(ModelBakery.class)
public interface ModelBakeryAccessor {
	@Accessor
	Map<Identifier, UnbakedModel> getTopLevelModels();

	@Invoker
	void invokeCacheAndQueueDependencies(Identifier id, UnbakedModel unbakedModel);
}
