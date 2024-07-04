package dev.lazurite.rayon.impl.mixin.common.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Basic changes for {@link EntityPhysicsElement}s.
 * ({@link CallbackInfo#cancel()} go brrr)
 */
@Mixin(Entity.class)
public abstract class EntityMixin
{
	@Shadow
	private EntityDimensions dimensions;
	@Shadow
	private Vec3 position;
	
	@Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	public void rayon$pushAwayFrom_push(Entity entity, CallbackInfo info)
	{
		if (EntityPhysicsElement.is((Entity) (Object) this) && EntityPhysicsElement.is(entity))
			info.cancel();
	}

	@Inject(method = "move", at = @At("HEAD"), cancellable = true)
	public void rayon$overrideMovement_move(CallbackInfo info)
	{
		if (EntityPhysicsElement.is((Entity) (Object) this))
			info.cancel();
	}

	@Inject(method = "saveWithoutId", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;addAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	public void rayon$saveRigidBody_saveWithoutId(CompoundTag tag, CallbackInfoReturnable<CompoundTag> info)
	{
		if (EntityPhysicsElement.is((Entity) (Object) this))
			tag.put("RigidBody", EntityPhysicsElement.get((Entity) (Object) this).getRigidBody().writeTagInfo());
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V"))
	public void rayon$readRigidBody_load(CompoundTag tag, CallbackInfo info)
	{
		if (EntityPhysicsElement.is((Entity) (Object) this))
			EntityPhysicsElement.get((Entity) (Object) this).getRigidBody().readTagInfo(tag.getCompound("RigidBody"));
	}
	
	@Inject(method = "makeBoundingBox", at = @At("HEAD"), cancellable = true)
	public void rayon$centerBoundingBox_makeBoundingBox(CallbackInfoReturnable<AABB> ci)
	{
		if (EntityPhysicsElement.is((Entity)(Object)this))
			ci.setReturnValue(this.dimensions.makeBoundingBox(this.position.subtract(0.0D, (double)(this.dimensions.height / 2.0F), 0.0D)));
	}
}
