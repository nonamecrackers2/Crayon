package dev.lazurite.rayon.impl.example.client.entity.renderer;

import org.joml.Quaternionf;

import com.jme3.math.Quaternion;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.lazurite.rayon.impl.bullet.math.Convert;
import dev.lazurite.rayon.impl.example.entity.PhysicsFallingBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

public class PhysicsFallingBlockRenderer extends EntityRenderer<PhysicsFallingBlock>
{
	private final BlockRenderDispatcher blockRenderer;
	
	public PhysicsFallingBlockRenderer(EntityRendererProvider.Context context)
	{
		super(context);
		this.blockRenderer = context.getBlockRenderDispatcher();
	}
	
	@Override
	public void render(PhysicsFallingBlock block, float yRot, float partialTick, PoseStack stack, MultiBufferSource bufferSource, int packedLight)
	{
		BlockState state = block.getBlockState();
		if (state.getRenderShape() == RenderShape.MODEL)
		{
			stack.pushPose();
			Quaternionf rot = Convert.toMinecraft(block.getPhysicsRotation(new Quaternion(), partialTick));
			stack.mulPose(rot);
			stack.translate(-0.5D, -0.5D, -0.5D);
			this.blockRenderer.renderSingleBlock(state, stack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);
			stack.popPose();
			super.render(block, yRot, partialTick, stack, bufferSource, packedLight);
		}
	}
	
	@Override
	public ResourceLocation getTextureLocation(PhysicsFallingBlock block)
	{
		return null;
	}
}
