package dev.lazurite.rayon.impl.bullet.collision.space.cache;

import java.util.List;
import java.util.Optional;

import com.jme3.math.Vector3f;

import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.collision.space.block.BlockProperty;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;

/**
 * Used for storing blocks that can be queried during physics execution. An
 * implementation of this should be updated/reloaded every tick on the main game
 * thread.
 * 
 * @see MinecraftSpace#step
 */
public interface ChunkCache
{
	static ChunkCache create(MinecraftSpace space)
	{
		return new SimpleChunkCache(space);
	}

	static boolean isValidBlock(BlockState blockState)
	{
		if (blockState == null)
			return false;

		final var block = blockState.getBlock();
		final var properties = BlockProperty.getBlockProperty(block);

		return properties != null ? properties.collidable() : !blockState.isAir() && !block.isPossibleToRespawnInThis() && (blockState.getFluidState().isEmpty() || (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)));
	}

	void refreshAll();

	void loadBlockData(BlockPos blockPos);

	void loadFluidData(BlockPos blockPos);

	MinecraftSpace getSpace();

	List<BlockData> getBlockData();

	List<FluidColumn> getFluidColumns();

	Optional<BlockData> getBlockData(BlockPos blockPos);

	Optional<FluidColumn> getFluidColumn(BlockPos blockPos);

	boolean isActive(BlockPos blockPos);

	record BlockData(Level level, BlockPos blockPos, BlockState blockState, MinecraftShape shape) {}

	record FluidData(Level level, BlockPos blockPos, FluidState fluidState) {}

	class FluidColumn
	{
		private final FluidData top;
		private final FluidData bottom;
		private final Vector3f flow;
		private final float height;
		private long index;

		public FluidColumn(BlockPos start, Level level)
		{
			this.index = Integer.toUnsignedLong(start.getX()) << 32l | Integer.toUnsignedLong(start.getZ());
			final var cursor = new BlockPos(start).mutable();
			var fluidState = level.getFluidState(cursor);

			// find bottom block
			while (!fluidState.isEmpty())
			{
				cursor.set(cursor.below());
				fluidState = level.getFluidState(cursor);
			}

			cursor.set(cursor.above()); // the above loop ends at one below the bottom
			fluidState = level.getFluidState(cursor);
			this.bottom = new FluidData(level, new BlockPos(cursor), level.getFluidState(cursor));

			// find top block
			while (!fluidState.isEmpty())
			{
				cursor.set(cursor.above());
				fluidState = level.getFluidState(cursor);
			}

			cursor.set(cursor.below());
			fluidState = level.getFluidState(cursor);

			this.top = new FluidData(level, new BlockPos(cursor), fluidState);
			this.height = fluidState.getHeight(level, cursor);

			// Water flow direction
			this.flow = Convert.toBullet(fluidState.getFlow(level, cursor));
		}

		public boolean contains(BlockPos blockPos)
		{
			return this.top.blockPos.getX() == blockPos.getX() && this.top.blockPos.getZ() == blockPos.getZ() && this.top.blockPos.getY() >= blockPos.getY() && this.bottom.blockPos.getY() <= blockPos.getY();
		}

		public FluidData getTop()
		{
			return this.top;
		}

		public FluidData getBottom()
		{
			return this.bottom;
		}

		public float getTopHeight(Vector3f position)
		{
			return this.height;
		}

		public int getHeight()
		{
			return this.top.blockPos.getY() - this.bottom.blockPos.getY() + 1;
		}

		public Vector3f getFlow()
		{
			return this.flow;
		}

		public long getIndex()
		{
			return this.index;
		}
	}
}