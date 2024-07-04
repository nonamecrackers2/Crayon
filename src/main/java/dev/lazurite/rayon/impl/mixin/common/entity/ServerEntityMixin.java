package dev.lazurite.rayon.impl.mixin.common.entity;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.lazurite.rayon.api.EntityPhysicsElement;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

/**
 * Prevents certain packets from being sent for {@link EntityPhysicsElement}s.
 */
//TODO: Somehow override the broadcast consumer so we don't have to have a bunch of these methods?
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(ServerEntity.class)
public class ServerEntityMixin
{
	@Shadow
	@Final
	private Entity entity;

	@Redirect(method = "sendChanges", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 1))
	public void rayon$overrideRotation_sendChanges(Consumer consumer, Object object)
	{
		if (!EntityPhysicsElement.is(this.entity))
			consumer.accept(object);
	}

	@Redirect(method = "sendChanges", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 2))
	public void rayon$overrideVelocity_sendChanges(Consumer consumer, Object object)
	{
		if (!EntityPhysicsElement.is(this.entity))
			consumer.accept(object);
	}

	@Redirect(method = "sendChanges", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", ordinal = 3))
	public void rayon$overrideMultiple_sendChanges(Consumer consumer, Object object)
	{
		if (!EntityPhysicsElement.is(this.entity))
			consumer.accept(object);
	}
}
