package dev.lazurite.rayon.impl.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import dev.lazurite.rayon.impl.mixin.common.entity.TrackedEntityMixin;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.server.level.ChunkMap;

@Mixin(ChunkMap.class)
public interface ChunkMapMixin
{
	@Accessor("entityMap")
	Int2ObjectMap<TrackedEntityMixin> rayon$getEntityMap();
}
