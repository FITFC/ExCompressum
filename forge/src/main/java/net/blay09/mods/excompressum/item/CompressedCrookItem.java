package net.blay09.mods.excompressum.item;

import net.blay09.mods.excompressum.ExCompressum;
import net.blay09.mods.excompressum.config.ExCompressumConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.HashSet;

public class CompressedCrookItem extends DiggerItem implements ICompressedCrook {

    public static final String name = "compressed_crook";
    public static final ResourceLocation registryName = new ResourceLocation(ExCompressum.MOD_ID, name);

    public CompressedCrookItem(Item.Properties properties) {
        super(0f, 0f, Tiers.WOOD, ModTags.MINEABLE_WITH_CROOK, properties
                .durability((int) (Tiers.WOOD.getUses() * 2 * ExCompressumConfig.getActive().tools.compressedCrookDurabilityMultiplier)));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity) {
        pushEntity(itemStack, player, entity);
        return true;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity entity, InteractionHand hand) {
        pushEntity(itemStack, player, entity);
        return InteractionResult.SUCCESS;
    }

    private void pushEntity(ItemStack itemStack, Player player, Entity entity) {
        if (!player.level.isClientSide) {
            double distance = Math.sqrt(Math.pow(player.getX() - entity.getX(), 2) + Math.pow(player.getZ() - entity.getZ(), 2));
            double scalarX = (player.getX() - entity.getX()) / distance;
            double scalarZ = (player.getZ() - entity.getZ()) / distance;
            double strength = 2.0;
            double velX = 0.0 - scalarX * strength;
            double velY = player.getY() < entity.getY() ? 0.5 : 0.0;
            double velZ = 0.0 - scalarZ * strength;
            entity.push(velX, velY, velZ);
        }
        itemStack.hurtAndBreak(1, player, it -> {
        });
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState block) {
        return block.getMaterial() == Material.LEAVES;
    }

    @Override
    public float getDestroySpeed(ItemStack item, BlockState block) {
        return block.getMaterial() == Material.LEAVES ? (float) (getTier().getSpeed() * ExCompressumConfig.getActive().tools.compressedCrookSpeedMultiplier) : 0f;
    }

    @Override
    public boolean canCrook(ItemStack itemStack, Level level, BlockState state, Player player) {
        return true;
    }

}
