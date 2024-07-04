package dev.lazurite.rayon.impl.bullet.collision.body;

import java.security.InvalidParameterException;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import dev.lazurite.rayon.api.PhysicsElement;
import dev.lazurite.rayon.impl.bullet.collision.body.shape.MinecraftShape;
import dev.lazurite.rayon.impl.bullet.collision.space.MinecraftSpace;
import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.bullet.thread.util.Clock;
import dev.lazurite.rayon.impl.util.Frame;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.AABB;
import nonamecrackers2.crackerslib.common.util.primitives.PrimitiveHelper;

public abstract class ElementRigidBody extends MinecraftRigidBody
{
	private static final Logger LOGGER = LogManager.getLogger("rayon/ElementRigidBody");
	public static final float SLEEP_TIME_IN_SECONDS = 2.0f;
	protected final PhysicsElement<?> element;
	private final Frame frame;
	private final Clock sleepTimer;
	private boolean terrainLoading;
	private float dragCoefficient;
	private BuoyancyType buoyancyType;
	private DragType dragType;
	private BoundingBox currentBoundingBox = new BoundingBox();
	private AABB currentMinecraftBoundingBox = new AABB(0, 0, 0, 0, 0, 0);

	public ElementRigidBody(PhysicsElement<?> element, MinecraftSpace space, MinecraftShape shape, float mass, float dragCoefficient, float friction, float restitution)
	{
		super(space, shape, mass);

		if (shape instanceof MinecraftShape.Concave)
			throw new InvalidParameterException("Only massless rigid bodies can use concave shapes.");

		this.element = element;
		this.frame = new Frame();
		this.sleepTimer = new Clock();

		this.setTerrainLoadingEnabled(!this.isStatic());
		this.setDragCoefficient(dragCoefficient);
		this.setFriction(friction);
		this.setRestitution(restitution);
		this.setBuoyancyType(BuoyancyType.WATER);
		this.setDragType(DragType.SIMPLE);
	}

	public PhysicsElement<?> getElement()
	{
		return this.element;
	}
	
	public CompoundTag writeTagInfo()
	{
		CompoundTag tag = new CompoundTag();
		tag.put("orientation", PrimitiveHelper.quaternionToTag(Convert.toMinecraft(this.getPhysicsRotation(new Quaternion()))));
		tag.put("linearVelocity", PrimitiveHelper.vector3fToTag(Convert.toMinecraft(this.getLinearVelocity(new Vector3f()))));
		tag.put("angularVelocity", PrimitiveHelper.vector3fToTag(Convert.toMinecraft(this.getAngularVelocity(new Vector3f()))));
		tag.putFloat("mass", this.getMass());
		tag.putFloat("dragCoefficient", this.getDragCoefficient());
		tag.putFloat("friction", this.getFriction());
		tag.putFloat("restitution", this.getRestitution());
		tag.putBoolean("terrainLoadingEnabled", this.terrainLoadingEnabled());
		PrimitiveHelper.saveEnum(this.getBuoyancyType(), tag, "buoyancyType");
		PrimitiveHelper.saveEnum(this.getDragType(), tag, "dragType");
		return tag;
	}

	public void readTagInfo(CompoundTag tag)
	{
		try
		{
			if (tag.contains("orientation", 10))
				this.setPhysicsRotation(Convert.toBullet(PrimitiveHelper.quaternionFromTag(tag.getCompound("orientation"))));
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.warn("Failed to read orientation", e);
		}
		try
		{
			if (tag.contains("linearVelocity", 10))
				this.setLinearVelocity(Convert.toBullet(PrimitiveHelper.vector3fFromTag(tag.getCompound("linearVelocity"))));
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.warn("Failed to read linear velocity", e);
		}
		try
		{
			if (tag.contains("angularVelocity", 10))
				this.setAngularVelocity(Convert.toBullet(PrimitiveHelper.vector3fFromTag(tag.getCompound("angularVelocity"))));
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.warn("Failed to read angular velocity", e);
		}
		readOptionalAndCatchInvalid(tag, "mass", 5, CompoundTag::getFloat, this::setMass);
		readOptionalAndCatchInvalid(tag, "dragCoefficient", 5, CompoundTag::getFloat, this::setDragCoefficient);
		readOptionalAndCatchInvalid(tag, "friction", 5, CompoundTag::getFloat, this::setFriction);
		readOptionalAndCatchInvalid(tag, "restitution", 5, CompoundTag::getFloat, this::setRestitution);
		readOptionalAndCatchInvalid(tag, "terrainLoadingEnabled", 1, CompoundTag::getBoolean, this::setTerrainLoadingEnabled);
		PrimitiveHelper.readEnum(ElementRigidBody.BuoyancyType.class, tag, "buoyancyType");
		PrimitiveHelper.readEnum(ElementRigidBody.DragType.class, tag, "dragType");
		
	}
	
	private static <T> void readOptionalAndCatchInvalid(CompoundTag tag, String name, int tagId, BiFunction<CompoundTag, String, T> valueGetter, Consumer<T> consumer)
	{
		try
		{
			if (tag.contains(name, tagId))
				consumer.accept(valueGetter.apply(tag, name));
		}
		catch (IllegalArgumentException e)
		{
			LOGGER.warn("Failed to read '" + name + "'", e);
		}
	}
	
	public boolean terrainLoadingEnabled()
	{
		return this.terrainLoading && !this.isStatic();
	}

	public void setTerrainLoadingEnabled(boolean terrainLoading)
	{
		this.terrainLoading = terrainLoading;
	}

	public float getDragCoefficient()
	{
		return dragCoefficient;
	}

	public void setDragCoefficient(float dragCoefficient)
	{
		this.dragCoefficient = dragCoefficient;
	}

	public BuoyancyType getBuoyancyType()
	{
		return this.buoyancyType;
	}

	public void setBuoyancyType(BuoyancyType buoyancyType)
	{
		this.buoyancyType = buoyancyType;
	}

	public DragType getDragType()
	{
		return this.dragType;
	}

	public void setDragType(DragType dragType)
	{
		this.dragType = dragType;
	}

	public Frame getFrame()
	{
		return this.frame;
	}

	public Clock getSleepTimer()
	{
		return this.sleepTimer;
	}

	@Override
	public Vector3f getOutlineColor()
	{
		return this.isActive() ? new Vector3f(1.0f, 1.0f, 1.0f) : new Vector3f(1.0f, 0.0f, 0.0f);
	}

	public void updateFrame()
	{
		getFrame().from(getFrame(), getPhysicsLocation(new Vector3f()), getPhysicsRotation(new Quaternion()));
		this.updateBoundingBox();
	}

	public boolean isNear(BlockPos blockPos)
	{
		return this.currentMinecraftBoundingBox.intersects(new AABB(blockPos).inflate(0.5f));
	}

	public boolean isNear(SectionPos blockPos)
	{
		return this.currentMinecraftBoundingBox.intersects(new AABB(blockPos.center()).inflate(8.5f));
	}

	public boolean isWaterBuoyancyEnabled()
	{
		return this.buoyancyType.isWaterBuoyancy();
	}

	public boolean isAirBuoyancyEnabled()
	{
		return this.buoyancyType.isAirBuoyancy();
	}

	public boolean isWaterDragEnabled()
	{
		return this.dragType.isWaterDrag();
	}

	public boolean isAirDragEnabled()
	{
		return this.dragType.isAirDrag();
	}

	public void updateBoundingBox()
	{
		this.currentBoundingBox = this.boundingBox(this.currentBoundingBox);
		this.currentMinecraftBoundingBox = Convert.toMinecraft(this.currentBoundingBox);
	}

	public AABB getCurrentMinecraftBoundingBox()
	{
		return this.currentMinecraftBoundingBox;
	}

	public BoundingBox getCurrentBoundingBox()
	{
		return this.currentBoundingBox;
	}

	public enum BuoyancyType
	{
		NONE(false, false), 
		AIR(false, true), 
		WATER(true, false), 
		ALL(true, true);
		
		private boolean waterBuoyancy;
		private boolean airBuoyancy;
		
		private BuoyancyType(boolean waterBuoyancy, boolean airBuoyancy)
		{
			this.waterBuoyancy = waterBuoyancy;
			this.airBuoyancy = airBuoyancy;
		}
		
		public boolean isWaterBuoyancy()
		{
			return this.waterBuoyancy;
		}
		
		public boolean isAirBuoyancy()
		{
			return this.airBuoyancy;
		}
	}

	public enum DragType
	{
		NONE(false, false), 
		AIR(false, true), 
		WATER(true, false), 
		SIMPLE(true, false), 
		ALL(true, true);
		
		private boolean waterDrag;
		private boolean airDrag;
		
		private DragType(boolean waterDrag, boolean airDrag)
		{
			this.waterDrag = waterDrag;
			this.airDrag = airDrag;
		}
		
		public boolean isWaterDrag()
		{
			return this.waterDrag;
		}
		
		public boolean isAirDrag()
		{
			return this.airDrag;
		}
	}
}