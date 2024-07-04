package dev.lazurite.rayon.impl.event;

import com.jme3.math.Vector3f;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.api.event.space.PhysicsSpaceEvent;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.EntityCollisionGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.storage.SpaceStorage;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity.ServerEntitySupplier;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.level.ServerLevelSupplier;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThreadStore;
import dev.lazurite.rayon.impl.packet.RayonPacketHandlers;
import dev.lazurite.rayon.impl.packet.impl.SendRigidBodyMovementPacket;
import dev.lazurite.rayon.impl.packet.impl.SendRigidBodyPropertiesPacket;
import dev.lazurite.rayon.impl.util.Utilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

@SuppressWarnings("deprecation")
public final class ServerEventHandler
{
	public static void onBlockUpdate(Level level, BlockState blockState, BlockPos blockPos)
	{
		MinecraftSpace.getOptional(level).ifPresent(space -> space.doBlockUpdate(blockPos));
	}

	@SubscribeEvent
	public static void onServerStart(ServerAboutToStartEvent event)
	{
		PhysicsThreadStore.INSTANCE.createServerThread(event.getServer(), Thread.currentThread(), new ServerLevelSupplier(event.getServer()), new ServerEntitySupplier());
	}
	
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event)
	{
		PhysicsThreadStore.INSTANCE.destroyServerThread();
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
			PhysicsThreadStore.checkThrowable(PhysicsThreadStore.INSTANCE.getServerThread());
	}

	@SubscribeEvent
	public static void onStartLevelTick(TickEvent.LevelTickEvent event)
	{
		if (!event.level.isClientSide && event.phase == TickEvent.Phase.START)
		{
			MinecraftSpace space = MinecraftSpace.get(event.level);
			space.step();
			
			EntityCollisionGenerator.step(space);

			for (var rigidBody : space.getRigidBodiesByClass(EntityRigidBody.class))
			{
				if (rigidBody.isActive())
				{
					/* Movement */
					if (rigidBody.isPositionDirty())
						RayonPacketHandlers.MAIN.send(PacketDistributor.TRACKING_ENTITY.with(rigidBody.getElement()::cast), new SendRigidBodyMovementPacket(rigidBody));

					/* Properties */
					if (rigidBody.arePropertiesDirty())
						RayonPacketHandlers.MAIN.send(PacketDistributor.TRACKING_ENTITY.with(rigidBody.getElement()::cast), new SendRigidBodyPropertiesPacket(rigidBody));
				}

				/* Set entity position */
				var location = rigidBody.getFrame().getLocation(new Vector3f(), 1.0f);
				rigidBody.getElement().cast().absMoveTo(location.x, location.y, location.z);
			}
		}
	}

	@SubscribeEvent
	public static void onLevelLoad(LevelEvent.Load event)
	{
		if (event.getLevel() instanceof Level level)
		{
			MinecraftSpace space = PhysicsThreadStore.INSTANCE.createPhysicsSpace(level);
			((SpaceStorage)level).setSpace(space);
			MinecraftForge.EVENT_BUS.post(new PhysicsSpaceEvent.Init(space));
		}
	}

	@SubscribeEvent
	public static void onElementAddedToSpace(PhysicsSpaceEvent.ElementAdded event)
	{
		if (event.getRigidBody() instanceof EntityRigidBody entityBody)
		{
			final var pos = entityBody.getElement().cast().position();
			entityBody.setPhysicsLocation(Convert.toBullet(pos));
		}
	}

	//Might not need this, since the below method should fire
	//TODO: Test
//	@SubscribeEvent
//	public static void onEntityLoad(EntityJoinLevelEvent event)
//	{
//		if (!event.getLevel().isClientSide())
//		{
//			Entity entity = event.getEntity();
//			if (EntityPhysicsElement.is(entity) && !PlayerUtil.tracking(entity).isEmpty())
//			{
//				var space = MinecraftSpace.get(entity.level);
//				space.getWorkerThread().execute(() -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
//			}
//		}
//	}

	@SubscribeEvent
	public static void onStartTrackingEntity(PlayerEvent.StartTracking event)
	{
		Entity entity = event.getTarget();
		if (EntityPhysicsElement.is(entity))
		{
			var space = MinecraftSpace.get(entity.level);
			space.getWorkerThread().execute(() -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
		}
	}

	@SubscribeEvent
	public static void onStopTrackingEntity(PlayerEvent.StopTracking event)
	{
		Entity entity = event.getTarget();
		if (EntityPhysicsElement.is(entity) && Utilities.getTracking(entity).isEmpty())
		{
			var space = MinecraftSpace.get(entity.level);
			space.getWorkerThread().execute(() -> space.removeCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
		}
	}
}