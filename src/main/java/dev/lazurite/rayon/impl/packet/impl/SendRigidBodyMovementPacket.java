package dev.lazurite.rayon.impl.packet.impl;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.jme3.math.Quaternion;

import dev.lazurite.rayon.impl.bullet.collision.body.EntityRigidBody;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.packet.RayonClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import nonamecrackers2.crackerslib.common.packet.Packet;

public class SendRigidBodyMovementPacket extends Packet
{
	private int id;
	private Quaternionf rotation;
	private Vector3f pos;
	private Vector3f linearVel;
	private Vector3f angularVel;
	
	public SendRigidBodyMovementPacket(EntityRigidBody body)
	{
		super(true);
		this.id = body.getElement().cast().getId();
		this.rotation = Convert.toMinecraft(body.getPhysicsRotation(new Quaternion()));
		this.pos = Convert.toMinecraft(body.getPhysicsLocation(new com.jme3.math.Vector3f()));
		this.linearVel = Convert.toMinecraft(body.getLinearVelocity(new com.jme3.math.Vector3f()));
		this.angularVel = Convert.toMinecraft(body.getAngularVelocity(new com.jme3.math.Vector3f()));
	}
	
	public SendRigidBodyMovementPacket()
	{
		super(false);
	}

	public int getId()
	{
		return this.id;
	}

	public Quaternionf getRotation()
	{
		return this.rotation;
	}

	public Vector3f getPos()
	{
		return this.pos;
	}

	public Vector3f getLinearVel()
	{
		return this.linearVel;
	}

	public Vector3f getAngularVel()
	{
		return this.angularVel;
	}
	
	@Override
	protected void decode(FriendlyByteBuf buffer)
	{
		this.id = buffer.readVarInt();
		this.rotation = buffer.readQuaternion();
		this.pos = buffer.readVector3f();
		this.linearVel = buffer.readVector3f();
		this.angularVel = buffer.readVector3f();
	}
	
	@Override
	protected void encode(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(this.id);
		buffer.writeQuaternion(this.rotation);
		buffer.writeVector3f(this.pos);
		buffer.writeVector3f(this.linearVel);
		buffer.writeVector3f(this.angularVel);
	}
	
	@Override
	public Runnable getProcessor(NetworkEvent.Context context)
	{
		return client(() -> RayonClientPacketHandler.handleSendRigidBodyMovementPacket(this));
	}
}
