package dev.lazurite.rayon.impl.example.init;

import dev.lazurite.rayon.impl.Rayon;
import dev.lazurite.rayon.impl.example.entity.PhysicsFallingBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class RayonExampleEntities
{
	private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Rayon.MODID);
	
	public static final RegistryObject<EntityType<PhysicsFallingBlock>> PHYSICS_FALLING_BLOCK = register("physics_falling_block", EntityType.Builder.of(PhysicsFallingBlock::new, MobCategory.MISC).clientTrackingRange(10).sized(1.0F, 1.0F).updateInterval(4));
	
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String id, EntityType.Builder<T> builder)
	{		
		return ENTITY_TYPES.register(id, () -> builder.build(Rayon.id(id).toString()));
	}
	
	public static void register(IEventBus modBus)
	{
		ENTITY_TYPES.register(modBus);
	}
}
