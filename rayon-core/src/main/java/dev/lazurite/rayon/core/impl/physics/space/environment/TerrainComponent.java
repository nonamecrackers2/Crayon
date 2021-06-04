package dev.lazurite.rayon.core.impl.physics.space.environment;

import com.jme3.math.Vector3f;
import dev.lazurite.rayon.core.impl.physics.space.body.BlockRigidBody;
import dev.lazurite.rayon.core.impl.physics.space.body.MinecraftRigidBody;
import dev.lazurite.rayon.core.impl.physics.space.body.shape.MinecraftShape;
import dev.lazurite.rayon.core.impl.physics.space.MinecraftSpace;
import dev.lazurite.rayon.core.impl.util.BlockProps;
import dev.lazurite.rayon.core.impl.util.model.Clump;
import dev.lazurite.transporter.Transporter;
import dev.lazurite.transporter.api.Disassembler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;

import java.util.*;

/**
 * Used for loading blocks into the simulation so that rigid bodies can interact with them.
 * @see MinecraftSpace
 * @see WorldComponent
 */
public class TerrainComponent implements WorldComponent {
    @Override
    public void apply(MinecraftSpace space) {
        final var toKeep = new ArrayList<BlockRigidBody>();

        for (var rigidBody : space.getRigidBodiesByClass(MinecraftRigidBody.class)) {
            if (rigidBody.shouldDoTerrainLoading()) {
                var pos = rigidBody.getPhysicsLocation(new Vector3f());
                var box = new Box(new BlockPos(pos.x, pos.y, pos.z)).expand(rigidBody.getEnvironmentLoadDistance());
                toKeep.addAll(getOrCreateAround(rigidBody, box));
            }
        }

        for (var rigidBody : space.getRigidBodiesByClass(BlockRigidBody.class)) {
            if (!toKeep.contains(rigidBody)) {
                space.removeCollisionObject(rigidBody);
            }
        }
    }

    /**
     * Loads an individual element's block area into the physics simulation. This
     * is also where each block's {@link BlockRigidBody} object is instantiated
     * and properties such as position, shape, friction, etc. are applied here.
     * @param rigidBody the rigid body to be loaded
     * @param bubble the {@link Box} area around the element to search for blocks within
     * @return a list of {@link BlockRigidBody}s that were loaded or otherwise should be kept
     */
    public static List<BlockRigidBody> getOrCreateAround(MinecraftRigidBody rigidBody, Box bubble) {
        final var space = rigidBody.getSpace();
        final var world = space.getWorld();
        final var clump = new Clump(world, bubble);
        final var toKeep = new ArrayList<BlockRigidBody>();

        if (rigidBody.isActive()) {
            clump.getData().forEach(blockInfo -> {
                BlockPos blockPos = blockInfo.getBlockPos();
                BlockState blockState = blockInfo.getBlockState();

                float friction = 0.5f; // 1.0f
                float restitution = 0.25f;
                boolean collidable = !blockState.getBlock().canMobSpawnInside();

                if (blockState.getBlock() instanceof IceBlock) {
                    friction = 0.05F;
                } else if (blockState.getBlock() instanceof SlimeBlock) {
                    friction = 3.0F;
                    restitution = 3.0F;
                } else if (blockState.getBlock() instanceof HoneyBlock || blockState.getBlock() instanceof SoulSandBlock) {
                    friction = 3.0F;
                }

                /* Apply custom block properties */
                var blockId = Registry.BLOCK.getId(blockState.getBlock());
                if (!blockId.getNamespace().equals("minecraft")) {
                    var props = BlockProps.get().get(blockId);

                    if (props != null) {
                        collidable = props.collidable();

                        if (props.friction() >= 0) {
                            friction = props.friction();
                        }

                        if (props.restitution() >= 0) {
                            restitution = props.restitution();
                        }
                    }
                }

                /* Check if the block is solid or not */
                if (collidable) {
                    var blockRigidBody = findBlockAtPos(space, blockPos);

                    /* Make a new rigid body if there isn't already one */
                    if (blockRigidBody == null) {
                        var voxel = blockState.getCollisionShape(world, blockPos);
                        MinecraftShape shape = voxel.isEmpty() ? MinecraftShape.of(new Box(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f)) : MinecraftShape.of(voxel.getBoundingBox());
                        blockRigidBody = new BlockRigidBody(blockState, blockPos, space, shape, friction, restitution);
                    }

                    /* Make a pattern shape if applicable */
                    if (!blockState.isFullCube(world, blockPos)) {
                        var pattern = Transporter.getPatternBuffer().get(Registry.BLOCK.getId(blockState.getBlock()));

                        if (pattern == null && world.isClient()) {
                            tryGenerateShape(world, blockPos, blockState);
                        }

                        if (pattern != null && blockRigidBody.getCollisionShape() == null) {
                            blockRigidBody.setCollisionShape(MinecraftShape.of(pattern));
                        }
                    }

                    if (!space.getRigidBodyList().contains(blockRigidBody)) {
                        space.addCollisionObject(blockRigidBody);
                    }

                    toKeep.add(blockRigidBody);
                }
            });
        }

        if (!clump.equals(rigidBody.getClump())) {
            rigidBody.activate();
        }

        rigidBody.setClump(clump);
        return toKeep;
    }

    public static BlockRigidBody findBlockAtPos(MinecraftSpace space, BlockPos blockPos) {
        for (BlockRigidBody body : space.getRigidBodiesByClass(BlockRigidBody.class)) {
            if (body.getBlockPos().equals(blockPos)) {
                return body;
            }
        }

        return null;
    }

    @Environment(EnvType.CLIENT)
    public static void tryGenerateShape(BlockView blockView, BlockPos blockPos, BlockState blockState) {
        var transformation = new MatrixStack();
        transformation.scale(0.95f, 0.95f, 0.95f);
        transformation.translate(-0.5f, -0.5f, -0.5f);

        var blockEntity = blockView.getBlockEntity(blockPos);

        try {
            if (blockEntity != null) {
                Disassembler.getBlockEntity(blockEntity, transformation);
            } else {
                Disassembler.getBlock(blockState, transformation);
            }
        } catch (Exception ignored) {

        }
    }
}
