package net.blay09.mods.excompressum.registry;

import net.blay09.mods.excompressum.StupidUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public abstract class ExRegistro {

	public static ExNihiloProvider instance;

	public static boolean isNihiloItem(ItemStack itemStack, ExNihiloProvider.NihiloItems type) {
		ItemStack nihiloStack = instance.getNihiloItem(type);
		return nihiloStack != null && itemStack.getItem() == nihiloStack.getItem() && (itemStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || nihiloStack.getItemDamage() == OreDictionary.WILDCARD_VALUE || itemStack.getItemDamage() == nihiloStack.getItemDamage());
	}

	@Nullable
	public static ItemStack getNihiloItem(ExNihiloProvider.NihiloItems type) {
		return instance.getNihiloItem(type);
	}

	public static boolean isHammerable(IBlockState state) {
		return instance.isHammerable(state);
	}

	public static boolean isHammerable(ItemStack itemStack) {
		IBlockState state = StupidUtils.getStateFromItemStack(itemStack);
		return state != null && instance.isHammerable(state);
	}

	public static Collection<ItemStack> rollHammerRewards(IBlockState state, float luck, Random rand) {
		return instance.rollHammerRewards(state, luck, rand);
	}

	public static Collection<ItemStack> rollHammerRewards(ItemStack itemStack, float luck, Random rand) {
		IBlockState state = StupidUtils.getStateFromItemStack(itemStack);
		if(state != null) {
			return instance.rollHammerRewards(state, luck, rand);
		}
		return Collections.emptyList();
	}

	public static boolean isSiftable(ItemStack itemStack) {
		IBlockState state = StupidUtils.getStateFromItemStack(itemStack);
		return state != null && instance.isSiftable(state);
	}

	public static Collection<ItemStack> rollSieveRewards(IBlockState state, float luck, Random rand) {
		return instance.rollSieveRewards(state, luck, rand);
	}

	public static Collection<ItemStack> rollSieveRewards(ItemStack itemStack, float luck, Random rand) {
		IBlockState state = StupidUtils.getStateFromItemStack(itemStack);
		if(state != null) {
			return instance.rollSieveRewards(state, luck, rand);
		}
		return Collections.emptyList();
	}

	@Nullable
	public static ItemStack rollSilkWorm(EntityLivingBase player, IBlockState state, int fortune) {
		return instance.rollSilkWorm(player, state, fortune);
	}
}
