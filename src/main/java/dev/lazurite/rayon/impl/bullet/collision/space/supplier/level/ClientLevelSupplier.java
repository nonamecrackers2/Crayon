package dev.lazurite.rayon.impl.bullet.collision.space.supplier.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * This is a {@link LevelSupplier} which returns a single {@link ClientLevel}
 * object in a {@link List} object.
 */
@Deprecated
public record ClientLevelSupplier(Minecraft minecraft) implements LevelSupplier
{
	@Override
	public List<Level> getAll()
	{
		final var out = new ArrayList<Level>();

		if (this.minecraft.level != null)
			out.add(this.minecraft.level);

		return out;
	}

	@Override
	public Level get(ResourceKey<Level> key)
	{
		if (this.minecraft.level != null && this.minecraft.level.dimension().equals(key))
			return this.minecraft.level;

		return null;
	}

	@Override
	public Optional<Level> getOptional(ResourceKey<Level> key)
	{
		return Optional.ofNullable(this.get(key));
	}
}
