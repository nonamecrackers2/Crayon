package dev.lazurite.rayon.impl.packet.impl;

import java.util.UUID;

import javax.annotation.Nullable;

import dev.lazurite.rayon.impl.bullet.collision.body.ElementRigidBody;
import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.packet.RayonClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent.Context;
import nonamecrackers2.crackerslib.common.packet.Packet;

public class SendRigidBodyPropertiesPacket extends Packet
{
	private int id;
	private float mass;
	private float dragCoefficient;
	private float friction;
	private float restitution;
	private boolean terrainLoadingEnabled;
	private ElementRigidBody.BuoyancyType buoyancyType;
	private ElementRigidBody.DragType dragType;
	private @Nullable UUID priorityPlayer;
	
	public SendRigidBodyPropertiesPacket(EntityRigidBody body)
	{
		super(true);
		this.id = body.getElement().cast().getId();
		this.mass = body.getMass();
		this.dragCoefficient = body.getDragCoefficient();
		this.friction = body.getFriction();
		this.restitution = body.getRestitution();
		this.terrainLoadingEnabled = body.terrainLoadingEnabled();
		this.buoyancyType = body.getBuoyancyType();
		this.dragType = body.getDragType();
		this.priorityPlayer = body.getPriorityPlayer() != null ? body.getPriorityPlayer().getUUID() : null;
	}
	
	public SendRigidBodyPropertiesPacket()
	{
		super(false);
	}

	public int getId()
	{
		return this.id;
	}

	public float getMass()
	{
		return this.mass;
	}

	public float getDragCoefficient()
	{
		return this.dragCoefficient;
	}

	public float getFriction()
	{
		return this.friction;
	}

	public float getRestitution()
	{
		return this.restitution;
	}

	public boolean isTerrainLoadingEnabled()
	{
		return this.terrainLoadingEnabled;
	}

	public ElementRigidBody.BuoyancyType getBuoyancyType()
	{
		return this.buoyancyType;
	}

	public ElementRigidBody.DragType getDragType()
	{
		return this.dragType;
	}
	
	public @Nullable UUID getPriorityPlayer()
	{
		return this.priorityPlayer;
	}
	
	@Override
	protected void decode(FriendlyByteBuf buffer)
	{
		this.id = buffer.readVarInt();
		this.mass = buffer.readFloat();
		this.dragCoefficient = buffer.readFloat();
		this.friction = buffer.readFloat();
		this.restitution = buffer.readFloat();
		this.terrainLoadingEnabled = buffer.readBoolean();
		this.buoyancyType = buffer.readEnum(ElementRigidBody.BuoyancyType.class);
		this.dragType = buffer.readEnum(ElementRigidBody.DragType.class);
		this.priorityPlayer = buffer.readNullable(FriendlyByteBuf::readUUID);
	}
	
	@Override
	protected void encode(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(this.id);
		buffer.writeFloat(this.mass);
		buffer.writeFloat(this.dragCoefficient);
		buffer.writeFloat(this.friction);
		buffer.writeFloat(this.restitution);
		buffer.writeBoolean(this.terrainLoadingEnabled);
		buffer.writeEnum(this.buoyancyType);
		buffer.writeEnum(this.dragType);
		buffer.writeNullable(this.priorityPlayer, FriendlyByteBuf::writeUUID);
	}
	
	@Override
	public Runnable getProcessor(Context context)
	{
		return client(() -> RayonClientPacketHandler.handleSendRigidBodyPropertiesPacket(this));
	}
}
