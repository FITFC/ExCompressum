package net.blay09.mods.excompressum.compat.crafttweaker.builder;


import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import net.blay09.mods.excompressum.loot.LootTableUtils;
import net.blay09.mods.excompressum.registry.LootTableProvider;
import net.blay09.mods.excompressum.registry.hammer.HammerRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.excompressum.HammerRecipe")
public class ZenHammerRecipe extends ZenBaseRecipe<ZenHammerRecipe> {

    private final HammerRecipe recipe;

    private ZenHammerRecipe(ResourceLocation recipeId) {
        this.recipe = new HammerRecipe(recipeId, Ingredient.EMPTY, LootTableProvider.EMPTY);
    }

    @ZenCodeType.Method
    public static ZenHammerRecipe builder(ResourceLocation recipeId) {
        return new ZenHammerRecipe(recipeId);
    }

    @ZenCodeType.Method
    public ZenHammerRecipe addDrop(IItemStack drop) {
        addLootPoolBuilder().add(LootTableUtils.buildLootEntry(drop.getInternal(), 1f));
        recipe.setLootTable(getLootTableProvider());
        return this;
    }

    @ZenCodeType.Method
    public ZenHammerRecipe addDrop(IItemStack drop, float chance) {
        addLootPoolBuilder().add(LootTableUtils.buildLootEntry(drop.getInternal(), chance));
        recipe.setLootTable(getLootTableProvider());
        return this;
    }

    @ZenCodeType.Method
    public ZenHammerRecipe setInput(IIngredient input) {
        recipe.setInput(input.asVanillaIngredient());
        return this;
    }

    @Override
    public void updateLootTable(LootTableProvider provider) {
        recipe.setLootTable(provider);
    }

    public HammerRecipe build() {
        return recipe;
    }

}
