package dev.lazurite.rayon.impl.bullet.collision.space.generator;

import java.util.HashSet;

import dev.lazurite.rayon.api.event.space.PhysicsSpaceEvent;
import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.TerrainRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import net.minecraft.core.BlockPos;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Used for loading blocks into the simulation so that rigid bodies can interact
 * with them.
 * 
 * @see MinecraftSpace
 */
public class TerrainGenerator
{
	@SubscribeEvent
	public static void onPhysicsStep(PhysicsSpaceEvent.Step event)
	{
		MinecraftSpace space = event.getSpace();
		
		final var chunkCache = space.getChunkCache();
		final var keep = new HashSet<TerrainRigidBody>();

		for (var rigidBody : space.getRigidBodiesByClass(ElementRigidBody.class))
		{
			if (!rigidBody.terrainLoadingEnabled() || !rigidBody.isActive())
			{
				continue;
			}

			final var aabb = rigidBody.getCurrentMinecraftBoundingBox().inflate(0.5f);

			BlockPos.betweenClosedStream(aabb).forEach(blockPos ->
			{
				chunkCache.getBlockData(blockPos).ifPresent(blockData ->
				{
					space.getTerrainObjectAt(blockPos).ifPresentOrElse(terrain ->
					{
						if (blockData.blockState() != terrain.getBlockState())
						{
							space.removeCollisionObject(terrain);

							final var terrain2 = TerrainRigidBody.from(blockData);
							space.addCollisionObject(terrain2);
							keep.add(terrain2);
						}
						else
						{
							keep.add(terrain);
						}
					}, () ->
					{
						final var terrain = TerrainRigidBody.from(blockData);
						space.addCollisionObject(terrain);
						keep.add(terrain);
					});
				});
			});
		}

		space.getTerrainMap().forEach((blockPos, terrain) ->
		{
			if (!keep.contains(terrain))
			{
				space.removeTerrainObjectAt(blockPos);
			}
		});
	}
}