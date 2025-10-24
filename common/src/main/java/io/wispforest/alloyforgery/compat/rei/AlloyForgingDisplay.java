package io.wispforest.alloyforgery.compat.rei;

import io.wispforest.endec.Endec;
import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import io.wispforest.alloyforgery.compat.CountedIngredientDisplay;
import io.wispforest.alloyforgery.recipe.AlloyForgeRecipe;
import java.util.*;

public class AlloyForgingDisplay implements Display {

    private final List<EntryIngredient> inputs;
    private final EntryIngredient output;

    public final int minForgeTier;
    public final int fuelPerTick;

    public final Map<AlloyForgeRecipe.OverrideRange, ItemStack> overrides;

    public final Optional<Identifier> recipeID;

    private AlloyForgingDisplay(List<EntryIngredient> inputs, EntryIngredient output, int minForgeTier, int fuelPerTick, Map<AlloyForgeRecipe.OverrideRange, ItemStack> overrides, Optional<Identifier> recipeID) {
        this.inputs = inputs;
        this.output = output;

        this.minForgeTier = minForgeTier;
        this.fuelPerTick = fuelPerTick;

        this.overrides = overrides;

        this.recipeID = recipeID;
    }

    public static AlloyForgingDisplay of(RecipeEntry<AlloyForgeRecipe> recipeEntry) {
        List<EntryIngredient> convertedInputs = new ArrayList<>();

        var recipe = recipeEntry.value();

        for (Map.Entry<Ingredient, Integer> entry : recipe.getIngredientsMap().entrySet()) {
            for (int i = entry.getValue(); i > 0; ) {
                int stackCount = Math.min(i, 64);

                convertedInputs.add(
                    EntryIngredients.ofSlotDisplay(new CountedIngredientDisplay(entry.getKey().toDisplay(), stackCount))
                );

                i -= stackCount;
            }
        }

        return new AlloyForgingDisplay(
            convertedInputs,
            EntryIngredients.of(recipe.getBaseResult()),
            recipe.getMinForgeTier(),
            recipe.getFuelPerTick(),
            recipe.getTierOverrides(),
            recipe.secondaryID().or(() -> Optional.of(recipeEntry.id().getValue())));
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return Collections.singletonList(output);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return AlloyForgeryCommonPlugin.ID;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return recipeID;
    }

    @Override
    public @Nullable DisplaySerializer<AlloyForgingDisplay> getSerializer() {
        return SERIALIZER;
    }

    public static final Endec<EntryIngredient> ENTRY_INGREDIENT_ENDEC = CodecUtils.toEndecWithRegistries(EntryIngredient.codec(), EntryIngredient.streamCodec());

    public static final StructEndec<AlloyForgingDisplay> ENDEC = StructEndecBuilder.of(
            ENTRY_INGREDIENT_ENDEC.listOf().fieldOf("inputs", (display) -> display.inputs),
            ENTRY_INGREDIENT_ENDEC.fieldOf("output", (display) -> display.output),
            Endec.INT.fieldOf("min_forge_tier", (display) -> display.minForgeTier),
            Endec.INT.fieldOf("fuel_per_tick", (display) -> display.fuelPerTick),
            Endec.map(AlloyForgeRecipe.OverrideRange.OVERRIDE_RANGE, MinecraftEndecs.ITEM_STACK).fieldOf("overrides", (display) -> display.overrides),
            MinecraftEndecs.IDENTIFIER.optionalOf().fieldOf("recipe_id", (display) -> display.recipeID),
            AlloyForgingDisplay::new
    );

    public static final DisplaySerializer<AlloyForgingDisplay> SERIALIZER = DisplaySerializer.of(CodecUtils.toMapCodec(ENDEC), CodecUtils.toPacketCodec(ENDEC));
}
