package dev.lazurite.rayon.impl.bullet.collision.body.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix4f;

import com.jme3.bounding.BoundingBox;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import dev.lazurite.rayon.impl.bullet.math.Convert;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Triangle
{
	private final Vector3f[] vertices;
	private final Vector3f centroid;
	private final Vector3f area;

	public static List<Triangle> getMeshOf(BoundingBox box)
	{
		final var x = box.getXExtent() * 0.5f;
		final var y = box.getYExtent() * 0.5f;
		final var z = box.getZExtent() * 0.5f;
		final var triangles = new ArrayList<Triangle>(6 * 2);
		createBoxMesh(x, y, z, Vector3f.ZERO, triangles::add);
		return triangles;
	}

	public static List<Triangle> getMeshOf(VoxelShape voxelShape)
	{
		if (voxelShape.isEmpty())
			return List.of();

		var vec = new Vector3f();
		var aabbs = voxelShape.toAabbs();
		final var triangles = new ArrayList<Triangle>(6 * 2 * aabbs.size());

		for (var box : aabbs)
		{
			final var x = box.getXsize() * 0.5f;
			final var y = box.getYsize() * 0.5f;
			final var z = box.getZsize() * 0.5f;
			var center = box.getCenter();
			vec.set((float) center.x, (float) center.y, (float) center.z).subtractLocal(0.5f, 0.5f, 0.5f);
			createBoxMesh((float) x, (float) y, (float) z, vec, triangles::add);
		}
		return triangles;

	}

	public static void createBoxMesh(final float x, final float y, final float z, final Vector3f offset, Consumer<Triangle> consumer)
	{
		final var points = new Vector3f[]
		{
			// south
			new Vector3f(x, y, z), new Vector3f(-x, y, z), new Vector3f(0, 0, z), new Vector3f(-x, y, z), new Vector3f(-x, -y, z), new Vector3f(0, 0, z), new Vector3f(-x, -y, z), new Vector3f(x, -y, z), new Vector3f(0, 0, z), new Vector3f(x, -y, z), new Vector3f(x, y, z), new Vector3f(0, 0, z),
			// north
			new Vector3f(-x, y, -z), new Vector3f(x, y, -z), new Vector3f(0, 0, -z), new Vector3f(x, y, -z), new Vector3f(x, -y, -z), new Vector3f(0, 0, -z), new Vector3f(x, -y, -z), new Vector3f(-x, -y, -z), new Vector3f(0, 0, -z), new Vector3f(-x, -y, -z), new Vector3f(-x, y, -z), new Vector3f(0, 0, -z),
			// east
			new Vector3f(x, y, -z), new Vector3f(x, y, z), new Vector3f(x, 0, 0), new Vector3f(x, y, z), new Vector3f(x, -y, z), new Vector3f(x, 0, 0), new Vector3f(x, -y, z), new Vector3f(x, -y, -z), new Vector3f(x, 0, 0), new Vector3f(x, -y, -z), new Vector3f(x, y, -z), new Vector3f(x, 0, 0),
			// west
			new Vector3f(-x, y, z), new Vector3f(-x, y, -z), new Vector3f(-x, 0, 0), new Vector3f(-x, y, -z), new Vector3f(-x, -y, -z), new Vector3f(-x, 0, 0), new Vector3f(-x, -y, -z), new Vector3f(-x, -y, z), new Vector3f(-x, 0, 0), new Vector3f(-x, -y, z), new Vector3f(-x, y, z), new Vector3f(-x, 0, 0),
			// up
			new Vector3f(x, y, -z), new Vector3f(-x, y, -z), new Vector3f(0, y, 0), new Vector3f(-x, y, -z), new Vector3f(-x, y, z), new Vector3f(0, y, 0), new Vector3f(-x, y, z), new Vector3f(x, y, z), new Vector3f(0, y, 0), new Vector3f(x, y, z), new Vector3f(x, y, -z), new Vector3f(0, y, 0),
			// down
			new Vector3f(x, -y, z), new Vector3f(-x, -y, z), new Vector3f(0, -y, 0), new Vector3f(-x, -y, z), new Vector3f(-x, -y, -z), new Vector3f(0, -y, 0), new Vector3f(-x, -y, -z), new Vector3f(x, -y, -z), new Vector3f(0, -y, 0), new Vector3f(x, -y, -z), new Vector3f(x, -y, z), new Vector3f(0, -y, 0) 
		};

		for (int i = 0; i < points.length; i += 3)
			consumer.accept(new Triangle(points[i].add(offset), points[i + 1].add(offset), points[i + 2].add(offset)));
	}

	public Triangle(Vector3f v1, Vector3f v2, Vector3f v3)
	{
		this.vertices = new Vector3f[] { v1, v2, v3 };
		this.centroid = new Vector3f().add(v1).add(v2).add(v3).divideLocal(3.0f);

		final var e1 = v1.subtract(v2);
		final var e2 = v2.subtract(v3);

		this.area = e2.cross(e1).multLocal(0.5f);
		this.area.multLocal(Math.signum(centroid.dot(area))); // make sure it faces outward
	}

	public Vector3f[] getVertices()
	{
		return this.vertices;
	}

	public Vector3f getCentroid()
	{
		return this.centroid;
	}

	public Vector3f getArea()
	{
		return this.area;
	}

	public Triangle transform(Quaternion quaternion)
	{
		return new Triangle(transform(vertices[0].clone(), quaternion), transform(vertices[1].clone(), quaternion), transform(vertices[2].clone(), quaternion));
	}

	private static Vector3f transform(Vector3f vector, Quaternion quaternion)
	{
		return Convert.toBullet(Convert.toMinecraft(vector).mulTransposeDirection(Convert.toMinecraft(quaternion).get(new Matrix4f())));
	}
}