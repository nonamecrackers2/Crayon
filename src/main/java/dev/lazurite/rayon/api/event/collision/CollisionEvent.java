package dev.lazurite.rayon.api.event.collision;

import dev.lazurite.rayon.impl.bullet.collision.body.MinecraftRigidBody;
import net.minecraftforge.eventbus.api.Event;

public class CollisionEvent extends Event
{
	private final CollisionEvent.Type type;
	private final MinecraftRigidBody main;
	private final MinecraftRigidBody other;
	private final float impulse;
	
	public CollisionEvent(CollisionEvent.Type type, MinecraftRigidBody main, MinecraftRigidBody other, float impulse)
	{
		this.type = type;
		this.main = main;
		this.other = other;
		this.impulse = impulse;
	}
	
	public CollisionEvent.Type getType()
	{
		return this.type;
	}

	public MinecraftRigidBody getMain()
	{
		return this.main;
	}

	public MinecraftRigidBody getOther()
	{
		return this.other;
	}

	public float getImpulse()
	{
		return this.impulse;
	}
	
	public static enum Type
	{
		BLOCK,
		FLUID,
		ELEMENT;
	}
}
