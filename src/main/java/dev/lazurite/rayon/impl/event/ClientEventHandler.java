package dev.lazurite.rayon.impl.event;

import com.jme3.math.Vector3f;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.EntityCollisionGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity.ClientEntitySupplier;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.level.ClientLevelSupplier;
import dev.lazurite.rayon.impl.bullet.thread.PhysicsThreadStore;
import dev.lazurite.rayon.impl.packet.RayonPacketHandlers;
import dev.lazurite.rayon.impl.packet.impl.SendRigidBodyMovementPacket;
import dev.lazurite.rayon.impl.util.debug.CollisionObjectDebugger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;

@SuppressWarnings({"removal", "deprecation"})
public final class ClientEventHandler
{
	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event)
	{
		if (event.level.isClientSide && event.phase == TickEvent.Phase.END)
		{
			MinecraftSpace space = MinecraftSpace.get(event.level);
			space.step();
			EntityCollisionGenerator.step(space);

			for (var rigidBody : space.getRigidBodiesByClass(EntityRigidBody.class))
			{
				var player = Minecraft.getInstance().player;

				/* Movement */
				if (rigidBody.isActive() && rigidBody.isPositionDirty() && player != null && player.equals(rigidBody.getPriorityPlayer()))
					RayonPacketHandlers.MAIN.send(PacketDistributor.TRACKING_ENTITY.with(rigidBody.getElement()::cast), new SendRigidBodyMovementPacket(rigidBody));

				/* Set entity position */
				var location = rigidBody.getFrame().getLocation(new Vector3f(), 1.0f);
				rigidBody.getElement().cast().absMoveTo(location.x, location.y, location.z);
			}
		}
	}

	public static void onGameJoin(Minecraft minecraft)
	{
		PhysicsThreadStore.INSTANCE.createClientThread(minecraft, Thread.currentThread(), new ClientLevelSupplier(minecraft), new ClientEntitySupplier());
	}
	
	public static void onDisconnect(Minecraft minecraft, ClientLevel level)
	{
		PhysicsThreadStore.INSTANCE.destroyClientThread();
	}
	
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.END)
			PhysicsThreadStore.checkThrowable(PhysicsThreadStore.INSTANCE.getClientThread());
	}

	@SubscribeEvent
	public static void onDebugRender(RenderLevelLastEvent event)
	{
		if (CollisionObjectDebugger.isEnabled())
		{
			Minecraft mc = Minecraft.getInstance();
			CollisionObjectDebugger.renderSpace(MinecraftSpace.get(mc.level), event.getPoseStack(), event.getPartialTick());
		}
	}

	@SubscribeEvent
	public static void onEntityLoad(EntityJoinLevelEvent event)
	{
		if (event.getLevel().isClientSide())
		{
			Entity entity = event.getEntity();
			if (EntityPhysicsElement.is(entity))
			{
				Level level = entity.level;
				MinecraftSpace.getOptional(level).ifPresent(space -> {
					space.getWorkerThread().execute(() -> space.addCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
				});
			}
		}
	}

	@SubscribeEvent
	public static void onEntityUnload(EntityLeaveLevelEvent event)
	{
		if (event.getLevel().isClientSide())
		{
			Entity entity = event.getEntity();
			if (EntityPhysicsElement.is(entity))
			{
				Level level = entity.level;
				MinecraftSpace.getOptional(level).ifPresent(space -> {
					space.getWorkerThread().execute(() -> space.removeCollisionObject(EntityPhysicsElement.get(entity).getRigidBody()));
				});
			}
		}
	}
}