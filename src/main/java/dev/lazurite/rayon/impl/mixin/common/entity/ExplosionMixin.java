package dev.lazurite.rayon.impl.mixin.common.entity;

import java.util.List;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import dev.lazurite.rayon.api.PhysicsElement;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;

/**
 * Allows {@link PhysicsElement} objects to be affected by explosions.
 */
@SuppressWarnings("rawtypes")
@Mixin(Explosion.class)
public class ExplosionMixin
{
	@Unique
	private Entity entity;

	@Inject(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;ignoreExplosion()Z"), locals = LocalCapture.CAPTURE_FAILHARD)
	public void rayon$setCurrentEntity_explode(CallbackInfo info, Set set, int q, float r, int s, int t, int u, int v, int w, int x, List list, Vec3 vec3, int y, Entity entity)
	{
		this.entity = entity;
	}

	@ModifyArg(method = "explode", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
	public Vec3 rayon$setVelocityOfRigidBody_explode(Vec3 velocity)
	{
		if (EntityPhysicsElement.is(this.entity))
		{
			var element = EntityPhysicsElement.get(this.entity);
			element.getRigidBody().applyCentralImpulse(Convert.toBullet(velocity).multLocal(element.getRigidBody().getMass() * 100f));
		}

		return velocity;
	}
}