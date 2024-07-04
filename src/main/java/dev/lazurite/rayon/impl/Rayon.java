package dev.lazurite.rayon.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.lazurite.rayon.impl.bullet.collision.space.generator.PressureGenerator;
import dev.lazurite.rayon.impl.bullet.collision.space.generator.TerrainGenerator;
import dev.lazurite.rayon.impl.bullet.natives.NativeLoader;
import dev.lazurite.rayon.impl.event.ClientEventHandler;
import dev.lazurite.rayon.impl.event.ServerEventHandler;
import dev.lazurite.rayon.impl.example.client.init.RayonExampleEntityRenderers;
import dev.lazurite.rayon.impl.example.init.RayonExampleEntities;
import dev.lazurite.rayon.impl.packet.RayonPacketHandlers;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Rayon.MODID)
public class Rayon
{
	public static final String MODID = "crayon";
	public static final Logger LOGGER = LogManager.getLogger("Crayon");
//	private static boolean serverHasRayon = false;
//
	public Rayon()
	{
		NativeLoader.load();
		
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener(this::clientInit);
		modBus.addListener(this::commonInit);
		modBus.addListener(RayonExampleEntityRenderers::registerEntityRenderers);
		
		RayonExampleEntities.register(modBus);
		
		// prevent annoying libbulletjme spam
		java.util.logging.LogManager.getLogManager().reset();


		// Rayon Server Detection
//		ServerEvents.Lifecycle.JOIN.register(player ->
//		{
//			ServerNetworking.send(player, new ResourceLocation(MODID, "i_have_rayon"), buf ->
//			{
//			});
//		});
	}
	
	private void clientInit(FMLClientSetupEvent event)
	{
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.register(ClientEventHandler.class);
		RayonPacketHandlers.registerPackets();
		
		// Rayon Server Detection
//		PacketRegistry.registerClientbound(new ResourceLocation(MODID, "i_have_rayon"), ctx -> serverHasRayon = true);
//		ClientEvents.Lifecycle.DISCONNECT.register((client, level) -> serverHasRayon = false);
	}
	
	private void commonInit(FMLCommonSetupEvent event)
	{
		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.register(ServerEventHandler.class);
		forgeBus.register(PressureGenerator.class);
		forgeBus.register(TerrainGenerator.class);
	}

//	public static boolean serverHasRayon()
//	{
//		return serverHasRayon;
//	}
	
	public static ResourceLocation id(String path)
	{
		return new ResourceLocation(MODID, path);
	}
}