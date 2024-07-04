package dev.lazurite.rayon.api;

import org.jetbrains.annotations.Nullable;

import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import net.minecraft.world.entity.Entity;

/**
 * Use this interface to create a physics entity.
 * 
 * @see PhysicsElement
 */
public interface EntityPhysicsElement extends PhysicsElement<Entity>
{
	static boolean is(Entity entity)
	{
		return entity instanceof EntityPhysicsElement element && element.getRigidBody() != null;
	}

	static EntityPhysicsElement get(Entity entity)
	{
		return (EntityPhysicsElement) entity;
	}

	@Override
	@Nullable
	EntityRigidBody getRigidBody();

	@Override
	default MinecraftShape.Convex createShape()
	{
		return MinecraftShape.convex(this.cast().getBoundingBox());
	}

	default boolean skipVanillaEntityCollisions()
	{
		return false;
	}
}
