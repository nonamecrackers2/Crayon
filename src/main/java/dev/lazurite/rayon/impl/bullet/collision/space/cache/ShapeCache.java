package dev.lazurite.rayon.impl.bullet.collision.space.cache;

import java.util.IdentityHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class ShapeCache
{
	private static final MinecraftShape FALLBACK_SHAPE = MinecraftShape.convex(new AABB(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f));

	private static final IdentityHashMap<BlockState, MinecraftShape> SHAPES_SERVER = new IdentityHashMap<>();
	private static final IdentityHashMap<BlockState, MinecraftShape> SHAPES_CLIENT = new IdentityHashMap<>();

	public static MinecraftShape getShapeFor(BlockState blockState, Level level, BlockPos blockPos)
	{
		if (blockState.getBlock().hasDynamicShape())
			return createShapeFor(blockState, level, blockPos);

		final var shapes = getShapes(level.isClientSide);
		var shape = shapes.get(blockState);

		if (shape == null)
		{
			shape = createShapeFor(blockState, level, BlockPos.ZERO);
			shapes.put(blockState, shape);
		}

		return shape;
	}

	private static Map<BlockState, MinecraftShape> getShapes(boolean isClientSide)
	{
		return isClientSide ? SHAPES_CLIENT : SHAPES_SERVER;
	}

	@Nullable
	private static MinecraftShape createShapeFor(BlockState blockState, Level level, BlockPos blockPos)
	{
//		final var properties = BlockProperty.getBlockProperty(blockState.getBlock());
		MinecraftShape shape = null;

//		if (!blockState.isCollisionShapeFullBlock(level, blockPos) || (properties != null && !properties.isFullBlock()))
//		{
//			Pattern pattern;
//
//			if (level.isClientSide)
//			{
//				pattern = ChunkCache.genShapeForBlock(level, blockPos, blockState);
//			}
//			else
//			{
//				pattern = Transporter.getPatternBuffer().getBlock(Block.getId(blockState));
//			}
//
//			if (pattern != null && !pattern.getQuads().isEmpty())
//			{
//				shape = MinecraftShape.concave(pattern);
//			}
//		}

		if (shape == null)
		{
			final var voxelShape = blockState.getCollisionShape(level, blockPos);
			if (!voxelShape.isEmpty())
				shape = MinecraftShape.convex(voxelShape);
			else
				shape = FALLBACK_SHAPE;
		}
		return shape;
	}
}
