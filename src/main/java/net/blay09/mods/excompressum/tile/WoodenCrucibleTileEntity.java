package net.blay09.mods.excompressum.tile;

import net.blay09.mods.excompressum.config.ModConfig;
import net.blay09.mods.excompressum.handler.VanillaPacketHandler;
import net.blay09.mods.excompressum.api.ExNihiloProvider;
import net.blay09.mods.excompressum.registry.ExRegistro;
import net.blay09.mods.excompressum.registry.woodencrucible.WoodenCrucibleRegistry;
import net.blay09.mods.excompressum.api.woodencrucible.WoodenCrucibleRegistryEntry;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

public class WoodenCrucibleTileEntity extends TileEntity implements ITickable {

	private static final int RAIN_FILL_INTERVAL = 20;
	private static final int MELT_INTERVAL = 20;
	private static final int RAIN_FILL_SPEED = 8;
	private static final int SYNC_INTERVAL = 10;

	private ItemStackHandler itemHandler = new ItemStackHandler(1) {
		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			ItemStack copy = stack.copy();
			if (addItem(copy, true, simulate)) {
				return copy.isEmpty() ? ItemStack.EMPTY : copy;
			}
			return stack;
		}
	};

	private FluidTank fluidTank = new FluidTank(1999) {
		@Override
		public int fill(FluidStack resource, boolean doFill) {
			int result = super.fill(resource, doFill);
			if (fluid != null && fluid.amount > 1000) {
				fluid.amount = 1000;
			}
			return result;
		}

		@Override
		public int getCapacity() {
			return 1000;
		}

		@Override
		public boolean canFill() {
			return itemHandler.getStackInSlot(0).isEmpty();
		}

		@Override
		public boolean canFillFluidType(FluidStack fluid) {
			return super.canFillFluidType(fluid) && fluid.getFluid().getTemperature() <= 300;
		}

		@Override
		protected void onContentsChanged() {
			markDirty();
			isDirty = true;
		}
	};

	private int ticksSinceSync;
	private boolean isDirty;
	private int ticksSinceRain;
	private int ticksSinceMelt;
	private WoodenCrucibleRegistryEntry currentMeltable;
	private int solidVolume;

	public boolean addItem(ItemStack itemStack, boolean isAutomated, boolean simulate) {
		// When inserting dust, turn it into clay if we have enough liquid
		if (fluidTank.getFluidAmount() >= 1000 && ExRegistro.isNihiloItem(itemStack, ExNihiloProvider.NihiloItems.DUST)) {
			itemStack.shrink(1);
			if(!simulate) {
				itemHandler.setStackInSlot(0, new ItemStack(Blocks.CLAY));
				fluidTank.setFluid(null);
				VanillaPacketHandler.sendTileEntityUpdate(this);
			}
			return true;
		}

		// Otherwise, try to add it as a meltable
		WoodenCrucibleRegistryEntry meltable = WoodenCrucibleRegistry.getEntry(itemStack);
		if (meltable != null) {
			if(fluidTank.getFluid() == null || fluidTank.getFluidAmount() == 0 || fluidTank.getFluid().getFluid() == meltable.getFluid()) {
				int capacityLeft = fluidTank.getCapacity() - fluidTank.getFluidAmount() - solidVolume;
				if ((isAutomated && capacityLeft >= meltable.getAmount()) || (!isAutomated && capacityLeft > 0)) {
					itemStack.shrink(1);
					if(!simulate) {
						currentMeltable = meltable;
						solidVolume += Math.min(capacityLeft, meltable.getAmount());
						VanillaPacketHandler.sendTileEntityUpdate(this);
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void tick() {
		if (!world.isRemote) {
			// Fill the crucible from rain
			if (world.getWorldInfo().isRaining() && world.canBlockSeeSky(pos) && world.getBiomeForCoordsBody(pos).getRainfall() > 0f) {
				ticksSinceRain++;
				if (ticksSinceRain >= RAIN_FILL_INTERVAL) {
					fluidTank.fill(new FluidStack(Fluids.WATER, RAIN_FILL_SPEED), true);
					ticksSinceRain = 0;
				}
			}

			// Melt down content
			if (currentMeltable != null) {
				ticksSinceMelt++;
				if (ticksSinceMelt >= MELT_INTERVAL && fluidTank.getFluidAmount() < fluidTank.getCapacity()) {
					int amount = Math.min(ModConfig.automation.woodenCrucibleSpeed, solidVolume);
					fluidTank.fill(new FluidStack(currentMeltable.getFluid(), amount), true);
					solidVolume = Math.max(0, solidVolume - amount);
					ticksSinceMelt = 0;
					isDirty = true;
				}
			}

			// Sync to clients
			ticksSinceSync++;
			if (ticksSinceSync >= SYNC_INTERVAL) {
				ticksSinceSync = 0;
				if (isDirty) {
					VanillaPacketHandler.sendTileEntityUpdate(this);
					isDirty = false;
				}
			}
		}
	}

	@Override
	public void readFromNBT(CompoundNBT tagCompound) {
		super.readFromNBT(tagCompound);
		solidVolume = tagCompound.getInt("SolidVolume");
		fluidTank.readFromNBT(tagCompound.getCompound("FluidTank"));
		itemHandler.deserializeNBT(tagCompound.getCompound("ItemHandler"));
		if (tagCompound.contains("Content")) {
			currentMeltable = WoodenCrucibleRegistry.getEntry(new ItemStack(tagCompound.getCompound("Content")));
		}
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
		super.writeToNBT(tagCompound);
		if (currentMeltable != null) {
			tagCompound.put("Content", currentMeltable.getItemStack().writeToNBT(new CompoundNBT()));
		}
		tagCompound.putInt("SolidVolume", solidVolume);
		tagCompound.put("FluidTank", fluidTank.writeToNBT(new CompoundNBT()));
		tagCompound.put("ItemHandler", itemHandler.serializeNBT());
		return tagCompound;
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return writeToNBT(new CompoundNBT());
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(pos, getBlockMetadata(), getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		readFromNBT(pkt.getNbtCompound());
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
				|| capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
				|| super.hasCapability(capability, facing);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return (T) fluidTank;
		}
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return (T) itemHandler;
		}
		return super.getCapability(capability, facing);
	}

	public FluidTank getFluidTank() {
		return fluidTank;
	}

	public int getSolidVolume() {
		return solidVolume;
	}

	public int getSolidCapacity() {
		return fluidTank.getCapacity();
	}

	public ItemStackHandler getItemHandler() {
		return itemHandler;
	}
}
