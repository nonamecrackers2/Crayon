package dev.lazurite.rayon.impl.bullet.thread;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.entity.EntitySupplier;
import dev.lazurite.rayon.impl.bullet.collision.space.supplier.level.LevelSupplier;
import net.minecraft.world.level.Level;

@SuppressWarnings("deprecation")
public class PhysicsThreadStore
{
	private static final Logger LOGGER = LogManager.getLogger("rayon/PhysicsThreadStore");
	public static final PhysicsThreadStore INSTANCE = new PhysicsThreadStore();
	private PhysicsThread client;
	private PhysicsThread server;
	
	private PhysicsThreadStore() {}
	
	private static void ensureThreadValid(@Nullable PhysicsThread thread)
	{
		if (thread == null)
			throw new NullPointerException("Physics thread does not exist");
		if (!thread.isAlive())
			throw new IllegalStateException("Physics thread is no longer valid");
	}
	
	public static void checkThrowable(@Nullable PhysicsThread thread)
	{
		if (thread != null && thread.throwable != null)
			throw new RuntimeException(thread.throwable);
	}
	
	public MinecraftSpace createPhysicsSpace(Level level)
	{
		PhysicsThread thread = this.getPhysicsThread(level.isClientSide());
		ensureThreadValid(thread);
		return new MinecraftSpace(thread, level);
	}
	
	/**
	 * @see MinecraftSpace#getWorkerThread() 
	 */
	public @Nullable PhysicsThread getPhysicsThread(boolean client)
	{
		return client ? this.client : this.server;
	}
	
	/**
	 * @see MinecraftSpace#getWorkerThread() 
	 */
	public @Nullable PhysicsThread getClientThread()
	{
		return this.client;
	}
	
	/**
	 * @see MinecraftSpace#getWorkerThread() 
	 */
	public @Nullable PhysicsThread getServerThread()
	{
		return this.server;
	}
	
	public void createServerThread(Executor parentExecutor, Thread parentThread, LevelSupplier levelSupplier, EntitySupplier entitySupplier)
	{
		this.server = createThread(this.server, parentExecutor, parentThread, levelSupplier, entitySupplier, "Server Physics Thread");
	}
	
	public void destroyServerThread()
	{
		destroyThread(this.server);
		this.server = null;
	}
	
	public void createClientThread(Executor parentExecutor, Thread parentThread, LevelSupplier levelSupplier, EntitySupplier entitySupplier)
	{
		this.client = createThread(this.client, parentExecutor, parentThread, levelSupplier, entitySupplier, "Client Physics Thread");
	}
	
	public void destroyClientThread()
	{
		destroyThread(this.client);
		this.client = null;
	}
	
	private static PhysicsThread createThread(@Nullable PhysicsThread thread, Executor parentExecutor, Thread parentThread, LevelSupplier levelSupplier, EntitySupplier entitySupplier, String name)
	{
		if (thread != null && !thread.isAlive())
		{
			LOGGER.warn("The previous {} thread was not destroyed", thread.getName());
			thread.destroy();
		}
		return new PhysicsThread(parentExecutor, parentThread, levelSupplier, entitySupplier, name);
	}
	
	private static void destroyThread(@Nullable PhysicsThread thread)
	{
		ensureThreadValid(thread);
		thread.destroy();
	}
}
