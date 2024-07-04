package dev.lazurite.rayon.impl.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.jme3.bullet.objects.PhysicsRigidBody;

import dev.lazurite.rayon.impl.util.debug.CollisionObjectDebugger;
import net.minecraft.client.KeyboardHandler;

/**
 * Adds an F3 key combination (F3 + R). It toggles renders for all relevant
 * {@link PhysicsRigidBody} objects.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardMixin
{
	@Shadow
	protected abstract void debugFeedbackTranslated(String string, Object... objects);

	@Inject(method = "handleDebugKeys", at = @At("HEAD"), cancellable = true)
	private void rayon$processExtraF3_handleDebugKeys(int key, CallbackInfoReturnable<Boolean> info)
	{
		if (key == 82) // 'r' key
		{
			boolean enabled = CollisionObjectDebugger.toggle();

			if (enabled)
				this.debugFeedbackTranslated("debug.rayon.on");
			else
				this.debugFeedbackTranslated("debug.rayon.off");

			info.setReturnValue(true);
		}
	}
}
