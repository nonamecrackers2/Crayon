package dev.lazurite.rayon.impl.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import dev.lazurite.rayon.impl.event.ServerEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(BlockBehaviour.BlockStateBase.class)
public class BlockStateBaseMixin
{
	@Inject(method = "updateShape", at = @At("HEAD"))
	public void rayon$onShapeUpdated_updateShape(Direction direction, BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2, CallbackInfoReturnable<BlockState> info)
	{
		if (levelAccessor instanceof Level level)
			ServerEventHandler.onBlockUpdate(level, blockState, blockPos);
	}
}
