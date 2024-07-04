package dev.lazurite.rayon.impl.util;

import java.util.List;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.google.common.collect.ImmutableList;

import dev.lazurite.rayon.impl.mixin.common.ChunkMapMixin;
import dev.lazurite.rayon.impl.mixin.common.entity.TrackedEntityMixin;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * From Lazurite-Toolbox {@link https://github.com/LazuriteMC/Lazurite-Toolbox}
 */
public class Utilities
{
	/**
	 * Lerp, but for spherical stuff (hence Slerp).
	 * 
	 * @param q1 the first {@link Quaternionf} to slerp
	 * @param q2 the second {@link Quaternionf} to slerp
	 * @param t  the delta time
	 * @return the slerped {@link Quaternionf}
	 */
	public static Quaternionf slerp(Quaternionf q1, Quaternionf q2, float t)
	{
		q1.normalize();
		q2.normalize();

		if (q1.x() == q2.x() && q1.y() == q2.y() && q1.z() == q2.z() && q1.w() == q2.w())
			return new Quaternionf(q1.x(), q1.y(), q1.z(), q1.w());

		var result = (q1.x() * q2.x()) + (q1.y() * q2.y()) + (q1.z() * q2.z()) + (q1.w() * q2.w());

		if (result < 0.0f)
		{
			q2.set(-q2.x(), -q2.y(), -q2.z(), -q2.w());
			result = -result;
		}

		var scale0 = 1 - t;
		var scale1 = t;

		if ((1 - result) > 0.1f)
		{
			final var theta = (float) Math.acos(result);
			final var invSinTheta = 1f / (float) Math.sin(theta);

			scale0 = (float) Math.sin((1 - t) * theta) * invSinTheta;
			scale1 = (float) Math.sin((t * theta)) * invSinTheta;
		}

		final var out = new Quaternionf((scale0 * q1.x()) + (scale1 * q2.x()), (scale0 * q1.y()) + (scale1 * q2.y()), (scale0 * q1.z()) + (scale1 * q2.z()), (scale0 * q1.w()) + (scale1 * q2.w()));

		out.normalize();
		return out;
	}

	/**
	 * Lerps two {@link Vector3f} objects using tick delta.
	 * 
	 * @param vec1  the first float vector
	 * @param vec2  the second float vector
	 * @param delta minecraft tick delta
	 * @return the newly lerped {@link Vector3f}
	 */
	public static Vector3f lerp(Vector3f vec1, Vector3f vec2, float delta)
	{
		return new Vector3f(Mth.lerp(delta, vec1.x(), vec2.x()), Mth.lerp(delta, vec1.y(), vec2.y()), Mth.lerp(delta, vec1.z(), vec2.z()));
	}

	/**
	 * Lerps two {@link Vec3} objects using tick delta.
	 * 
	 * @param vec1  the first double vector
	 * @param vec2  the second double vector
	 * @param delta minecraft tick delta
	 * @return the newly lerped {@link Vec3}
	 */
	public static Vec3 lerp(Vec3 vec1, Vec3 vec2, float delta)
	{
		return new Vec3(Mth.lerp(delta, vec1.x, vec2.x), Mth.lerp(delta, vec1.y, vec2.y), Mth.lerp(delta, vec1.z, vec2.z));
	}

	/**
	 * Converts the given {@link Quaternionf} to a vector containing three axes of
	 * rotation in degrees. The order is (roll, pitch, yaw).
	 * 
	 * @param quat the {@link Quaternionf} to extract the euler angles from
	 * @return a new vector containing three rotations in degrees
	 */
	public static Vector3f toEulerAngles(Quaternionf quat)
	{
		final var q = new Quaternionf(0, 0, 0, 1);
		q.set(quat.x(), quat.y(), quat.z(), quat.w());

		var i = 0.0f;
		var j = 0.0f;
		var k = 0.0f;

		// roll (x-axis rotation)
		final var sinr_cosp = 2 * (q.w() * q.x() + q.y() * q.z());
		final var cosr_cosp = 1 - 2 * (q.x() * q.x() + q.y() * q.y());
		i = (float) Math.atan2(sinr_cosp, cosr_cosp);

		// pitch (y-axis rotation)
		final var sinp = 2 * (q.w() * q.y() - q.z() * q.x());
		if (Math.abs(sinp) >= 1)
			j = (float) Math.copySign(Math.PI / 2, sinp); // use 90 degrees if out of range
		else
			j = (float) Math.asin(sinp);

		// yaw (z-axis rotation)
		final var siny_cosp = 2 * (q.w() * q.z() + q.x() * q.y());
		final var cosy_cosp = 1 - 2 * (q.y() * q.y() + q.z() * q.z());
		k = (float) Math.atan2(siny_cosp, cosy_cosp);

		return new Vector3f(i, j, k);
	}
	
    public static List<ServerPlayer> getTracking(Entity entity) 
    {
    	if (entity.getLevel() instanceof ServerLevel level)
    	{
    		ChunkMap chunkMap = level.getChunkSource().chunkMap;
    		TrackedEntityMixin trackedEntity = ((ChunkMapMixin)chunkMap).rayon$getEntityMap().get(entity.getId());
    		if (trackedEntity != null)
    			return trackedEntity.rayon$getSeenBy().stream().map(ServerPlayerConnection::getPlayer).collect(ImmutableList.toImmutableList());
    		return ImmutableList.of();
    	}
    	else
    	{
    		throw new IllegalArgumentException("Can only fetch players tracking entity on the server");
    	}
    }
}
