package dev.lazurite.rayon.impl.packet;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.packet.impl.SendRigidBodyMovementPacket;
import dev.lazurite.rayon.impl.packet.impl.SendRigidBodyPropertiesPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

public class RayonClientPacketHandler
{
	public static void handleSendRigidBodyMovementPacket(SendRigidBodyMovementPacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null)
		{
			Entity entity = mc.level.getEntity(packet.getId());
			if (EntityPhysicsElement.is(entity))
			{
				EntityRigidBody rigidBody = EntityPhysicsElement.get(entity).getRigidBody();
				
				MinecraftSpace.get(mc.level).getWorkerThread().execute(() ->
				{
					rigidBody.setPhysicsRotation(Convert.toBullet(packet.getRotation()));
					rigidBody.setPhysicsLocation(Convert.toBullet(packet.getPos()));
					rigidBody.setLinearVelocity(Convert.toBullet(packet.getLinearVel()));
					rigidBody.setAngularVelocity(Convert.toBullet(packet.getAngularVel()));
					rigidBody.activate();
				});
			}
		}
	}
	
	public static void handleSendRigidBodyPropertiesPacket(SendRigidBodyPropertiesPacket packet)
	{
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null)
		{
			Entity entity = mc.level.getEntity(packet.getId());
			if (EntityPhysicsElement.is(entity))
			{
				EntityRigidBody rigidBody = EntityPhysicsElement.get(entity).getRigidBody();
				
				MinecraftSpace.get(mc.level).getWorkerThread().execute(() ->
				{
					rigidBody.setMass(packet.getMass());
					rigidBody.setDragCoefficient(packet.getDragCoefficient());
					rigidBody.setFriction(packet.getFriction());
					rigidBody.setRestitution(packet.getRestitution());
					rigidBody.setTerrainLoadingEnabled(packet.isTerrainLoadingEnabled());
					rigidBody.setBuoyancyType(packet.getBuoyancyType());
					rigidBody.setDragType(packet.getDragType());
					if (packet.getPriorityPlayer() != null)
						rigidBody.prioritize(mc.level.getPlayerByUUID(packet.getPriorityPlayer()));
					else
						rigidBody.prioritize(null);
					rigidBody.activate();
				});
			}
		}
	}
}
