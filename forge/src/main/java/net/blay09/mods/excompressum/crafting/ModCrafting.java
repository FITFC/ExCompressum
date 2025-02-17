package net.blay09.mods.excompressum.crafting;

import net.blay09.mods.excompressum.ExCompressum;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExCompressum.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCrafting {
    @SubscribeEvent
    public static void registerRecipeSerializers(RegistryEvent.Register<RecipeSerializer<?>> event) {
        CraftingHelper.register(HasNihiloItemCondition.Serializer.INSTANCE);
        CraftingHelper.register(NihiloModLoadedCondition.Serializer.INSTANCE);
        CraftingHelper.register(EvolvedOrechidEnabledCondition.Serializer.INSTANCE);

        CraftingHelper.register(new ResourceLocation(ExCompressum.MOD_ID, "nihilo_item"), NihiloItemIngredient.Serializer.INSTANCE);
    }
}
