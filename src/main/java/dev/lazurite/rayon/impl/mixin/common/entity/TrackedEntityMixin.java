package dev.lazurite.rayon.impl.mixin.common.entity;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.network.ServerPlayerConnection;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityMixin
{
	@Accessor("seenBy")
	Set<ServerPlayerConnection> rayon$getSeenBy();
}
