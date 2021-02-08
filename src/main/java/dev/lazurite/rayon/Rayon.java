package dev.lazurite.rayon;

import com.google.common.collect.Lists;
import dev.lazurite.rayon.api.event.DynamicsWorldEvents;
import dev.lazurite.rayon.impl.builder.RigidBodyEntry;
import dev.lazurite.rayon.impl.util.NativeLoader;
import dev.lazurite.rayon.impl.physics.body.EntityRigidBody;
import dev.lazurite.rayon.impl.util.net.RigidBodyC2S;
import dev.lazurite.rayon.impl.util.net.spawn.RayonSpawnHandler;
import dev.lazurite.rayon.impl.util.config.Config;
import dev.lazurite.rayon.impl.physics.world.MinecraftDynamicsWorld;
import dev.lazurite.rayon.impl.util.net.ConfigS2C;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Rayon implements ModInitializer, ClientModInitializer, EntityComponentInitializer, WorldComponentInitializer {
	public static final String MODID = "rayon";
	public static final Logger LOGGER = LogManager.getLogger("Rayon");

	public static final List<RigidBodyEntry<? extends Entity>> entries = Lists.newArrayList();

	public static final ComponentKey<EntityRigidBody> ENTITY = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "entity"), EntityRigidBody.class);
	public static final ComponentKey<MinecraftDynamicsWorld> WORLD = ComponentRegistryV3.INSTANCE.getOrCreate(new Identifier(MODID, "world"), MinecraftDynamicsWorld.class);

	@Override
	public void onInitialize() {
		NativeLoader.load();
		Config.getInstance().load();
		ServerPlayNetworking.registerGlobalReceiver(RigidBodyC2S.PACKET_ID, RigidBodyC2S::accept);
	}

	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(new Identifier(Rayon.MODID, "rayon_spawn_s2c_packet"), RayonSpawnHandler::accept);
		ClientPlayNetworking.registerGlobalReceiver(ConfigS2C.PACKET_ID, ConfigS2C::accept);
	}

	/**
	 * Registers every entity defined by other mods during initialization in CCA.
	 * @param registry the cardinal components entity registry
	 * @see EntityRigidBody
	 */
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		entries.forEach(entry -> registry.registerFor(entry.getEntity(), ENTITY,
			(entity) -> new EntityRigidBody(entity, entry.getShapeFactory(), entry.getMass(), entry.getDragCoefficient(), entry.getFriction(), entry.getRestitution())));
	}

	/**
	 * Registers the {@link MinecraftDynamicsWorld} component in CCA.
	 * @param registry the cardinal components world registry
	 * @see MinecraftDynamicsWorld
	 */
	@Override
	public void registerWorldComponentFactories(WorldComponentFactoryRegistry registry) {
		registry.register(WORLD, world -> {
			String name = world.isClient ? "Client" : "Server";
			AtomicReference<MinecraftDynamicsWorld> dynamicsWorld = new AtomicReference<>();

			Thread thread = new Thread(() -> {
				DynamicsWorldEvents.LOAD.invoker().onLoad(dynamicsWorld.get());

				while (!dynamicsWorld.get().isDestroyed()) {
					dynamicsWorld.get().step();
				}
			}, name + " Physics Thread");

			dynamicsWorld.set(new MinecraftDynamicsWorld(thread, world));
			thread.start();

			return dynamicsWorld.get();
		});
	}
}
