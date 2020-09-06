package net.blay09.mods.excompressum.item;

import net.blay09.mods.excompressum.ExCompressum;
import net.blay09.mods.excompressum.compat.Compat;
import net.blay09.mods.excompressum.config.ModConfig;
import net.minecraft.block.material.Material;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.HashSet;

public class ItemCompressedCrook extends ToolItem implements ICompressedCrook {

    public static final String name = "compressed_crook";
    public static final ResourceLocation registryName = new ResourceLocation(ExCompressum.MOD_ID, name);

    public ItemCompressedCrook() {
        super(0f, 0f, ToolMaterial.WOOD, new HashSet<>());
        setCreativeTab(ExCompressum.creativeTab);
        setMaxDamage((int) (ToolMaterial.WOOD.getMaxUses() * 2 * ModConfig.tools.compressedCrookDurabilityMultiplier));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, EntityPlayer player, Entity entity) {
        pushEntity(itemStack, player, entity);
        return true;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack itemStack, EntityPlayer player, EntityLivingBase entity, EnumHand hand) {
        pushEntity(itemStack, player, entity);
        return true;
    }

    private void pushEntity(ItemStack itemStack, EntityPlayer player, Entity entity) {
        if(!player.world.isRemote) {
            double distance = Math.sqrt(Math.pow(player.posX - entity.posX, 2) + Math.pow(player.posZ - entity.posZ, 2));
            double scalarX = (player.posX - entity.posX) / distance;
            double scalarZ = (player.posZ - entity.posZ) / distance;
            double strength = 2.0;
            double velX = 0.0 - scalarX * strength;
            double velY = player.posY < entity.posY ? 0.5 : 0.0;
            double velZ = 0.0 - scalarZ * strength;
            entity.addVelocity(velX, velY, velZ);
        }
        itemStack.damageItem(1, player);
    }

    @Override
    public boolean canHarvestBlock(BlockState block) {
        return block.getMaterial() == Material.LEAVES;
    }

    @Override
    public float getDestroySpeed(ItemStack item, BlockState block) {
        return block.getMaterial() == Material.LEAVES ? toolMaterial.getEfficiency() * ModConfig.tools.compressedCrookSpeedMultiplier : 0f;
    }

    @Override
    public boolean canCrook(ItemStack itemStack, World world, BlockState state, EntityPlayer entityPlayer) {
        return true;
    }

    @Override
    public boolean isCrook(ItemStack itemStack) {
        return true;
    }
}
