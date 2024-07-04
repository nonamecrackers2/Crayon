package dev.lazurite.rayon.api.event.space;

import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import net.minecraftforge.eventbus.api.Event;

public abstract class PhysicsSpaceEvent extends Event
{
	private final MinecraftSpace space;
	
	public PhysicsSpaceEvent(MinecraftSpace space)
	
	{
		this.space = space;
	}
	
	public MinecraftSpace getSpace()
	{
		return this.space;
	}
	
	public static class Init extends PhysicsSpaceEvent
	{
		public Init(MinecraftSpace space)
		{
			super(space);
		}
	}
	
	public static class Step extends PhysicsSpaceEvent
	{
		public Step(MinecraftSpace space)
		{
			super(space);
		}
	}
	
	public static class ElementAdded extends PhysicsSpaceEvent
	{
		private final ElementRigidBody rigidBody;
		
		public ElementAdded(MinecraftSpace space, ElementRigidBody rigidBody)
		{
			super(space);
			this.rigidBody = rigidBody;
		}
		
		public ElementRigidBody getRigidBody()
		{
			return this.rigidBody;
		}
	}
	
	public static class ElementRemoved extends PhysicsSpaceEvent
	{
		private final ElementRigidBody rigidBody;
		
		public ElementRemoved(MinecraftSpace space, ElementRigidBody rigidBody)
		{
			super(space);
			this.rigidBody = rigidBody;
		}
		
		public ElementRigidBody getRigidBody()
		{
			return this.rigidBody;
		}
	}
}
