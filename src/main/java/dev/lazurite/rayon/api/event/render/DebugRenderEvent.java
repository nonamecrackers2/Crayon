package dev.lazurite.rayon.api.event.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.Event;

public class DebugRenderEvent extends Event
{
	private final MinecraftSpace space;
	private final VertexConsumer vertexConsumer;
	private final PoseStack stack;
	private final Vec3 cameraPos;
	private final float partialTick;
	
	public DebugRenderEvent(MinecraftSpace space, VertexConsumer vertexConsumer, PoseStack stack, Vec3 cameraPos, float partialTick)
	{
		this.space = space;
		this.vertexConsumer = vertexConsumer;
		this.stack = stack;
		this.cameraPos = cameraPos;
		this.partialTick = partialTick;
	}

	public MinecraftSpace getSpace()
	{
		return this.space;
	}

	public VertexConsumer getVertexConsumer()
	{
		return this.vertexConsumer;
	}

	public PoseStack getStack()
	{
		return this.stack;
	}

	public Vec3 getCameraPos()
	{
		return this.cameraPos;
	}

	public float getPartialTick()
	{
		return this.partialTick;
	}
}
