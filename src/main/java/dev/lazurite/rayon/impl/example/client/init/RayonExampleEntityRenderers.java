package dev.lazurite.rayon.impl.example.client.init;

import dev.lazurite.rayon.impl.example.client.entity.renderer.PhysicsFallingBlockRenderer;
import dev.lazurite.rayon.impl.example.init.RayonExampleEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;

public class RayonExampleEntityRenderers
{
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
		event.registerEntityRenderer(RayonExampleEntities.PHYSICS_FALLING_BLOCK.get(), PhysicsFallingBlockRenderer::new);
	}
}
