package net.blay09.mods.excompressum.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import net.blay09.mods.excompressum.client.ModModels;
import net.blay09.mods.excompressum.block.entity.WoodenCrucibleBlockEntity;
import net.blay09.mods.excompressum.utils.StupidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

public class WoodenCrucibleRenderer implements BlockEntityRenderer<WoodenCrucibleBlockEntity> {

    private final Random random = new Random();

    public WoodenCrucibleRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WoodenCrucibleBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int combinedLight, int combinedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        ItemStack outputStack = blockEntity.getItemHandler().getStackInSlot(0);
        if (!outputStack.isEmpty()) {
            BlockState outputState = StupidUtils.getStateFromItemStack(outputStack);
            if (outputState != null) {
                poseStack.pushPose();
                poseStack.translate(0.0625f, 0.2f, 0.0625f);
                poseStack.scale(0.875f, 0.75f, 0.875f);
                dispatcher.renderSingleBlock(outputState, poseStack, buffers, combinedLight, combinedOverlay);
                poseStack.popPose();
            }
        }

        FluidStack fluidStack = blockEntity.getFluidTank().getFluid();
        if (!fluidStack.isEmpty()) {
            poseStack.pushPose();
            float fillLevel = (float) fluidStack.getAmount() / (float) blockEntity.getFluidTank().getCapacity();
            poseStack.translate(0f, fillLevel * 11 / 16f, 0f);
            int color = fluidStack.getFluid().getAttributes().getColor(level, blockEntity.getBlockPos());
            float red = (float)(color >> 16 & 255) / 255.0F;
            float green = (float)(color >> 8 & 255) / 255.0F;
            float blue = (float)(color & 255) / 255.0F;
            dispatcher.getModelRenderer().renderModel(poseStack.last(), buffers.getBuffer(RenderType.translucent()), null, ModModels.woodenCrucibleLiquid.get(), red, green, blue, combinedLight, combinedOverlay);
            poseStack.popPose();
        }

        int solidVolume = blockEntity.getSolidVolume();
        if (solidVolume > 0) {
            poseStack.pushPose();
            poseStack.translate(0.0625f, 0.251f, 0.0625f);
            poseStack.scale(0.875f, (float) (0.71 * (float) solidVolume / (float) blockEntity.getSolidCapacity()), 0.875f);
            BlockState solidState = Blocks.DARK_OAK_LEAVES.defaultBlockState();
            dispatcher.renderBatched(solidState, blockEntity.getBlockPos(), blockEntity.getLevel(), poseStack, buffers.getBuffer(RenderType.translucent()), false, random);
            poseStack.popPose();
        }
    }
}
