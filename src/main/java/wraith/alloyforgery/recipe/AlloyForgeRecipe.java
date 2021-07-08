package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import wraith.alloyforgery.AlloyForgery;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AlloyForgeRecipe implements Recipe<Inventory> {

    private final Identifier id;

    private final List<Ingredient> inputs;
    private final ItemStack output;

    private final int minForgeTier;
    private final int fuelPerTick;

    private final ImmutableMap<OverrideRange, ItemStack> tierOverrides;

    public AlloyForgeRecipe(Identifier id, List<Ingredient> inputs, ItemStack output, int minForgeTier, int fuelPerTick, ImmutableMap<OverrideRange, ItemStack> overrides) {
        this.id = id;
        this.inputs = inputs;
        this.output = output;
        this.minForgeTier = minForgeTier;
        this.fuelPerTick = fuelPerTick;

        this.tierOverrides = overrides;
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        RecipeMatcher recipeMatcher = new RecipeMatcher();
        int nonEmptyStacks = 0;

        for (int j = 0; j < inventory.size() - 2; ++j) {
            ItemStack itemStack = inventory.getStack(j);
            if (!itemStack.isEmpty()) {
                ++nonEmptyStacks;
                recipeMatcher.addInput(itemStack, 1);
            }
        }

        return nonEmptyStacks == this.inputs.size() && recipeMatcher.match(this, null);
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        return DefaultedList.copyOf(Ingredient.EMPTY, inputs.toArray(new Ingredient[0]));
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack getOutput() {
        return output.copy();
    }

    public ItemStack getOutput(int forgeTier) {
        return tierOverrides.getOrDefault(tierOverrides.keySet().stream().filter(overrideRange -> overrideRange.test(forgeTier)).findAny().orElse(null), output).copy();
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AlloyForgeRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public int getMinForgeTier() {
        return minForgeTier;
    }

    public int getFuelPerTick() {
        return fuelPerTick;
    }

    public ImmutableMap<OverrideRange, ItemStack> getTierOverrides() {
        return tierOverrides;
    }

    public static record OverrideRange(int lowerBound, int upperBound) {

        public OverrideRange(int lowerBound) {
            this(lowerBound, -1);
        }

        public boolean test(int value) {
            return value >= lowerBound && (upperBound == -1 || value <= upperBound);
        }

        @Override
        public String toString() {
            var outString = String.valueOf(lowerBound);
            var chars = outString.chars().mapToObj(value -> (char) value).collect(Collectors.toList());

            if (upperBound != lowerBound) {
                if (upperBound == -1) {
                    chars.add('+');
                } else {
                    var to = " to ".chars().mapToObj(value -> (char)value).collect(Collectors.toList());
                    to.forEach(character -> chars.add(character));

                    var bound = String.valueOf(upperBound).chars().mapToObj(value -> (char)value).collect(Collectors.toList());
                    bound.forEach(character -> chars.add(character));
                }
            }

            var output = new StringBuilder();
            chars.forEach(character -> output.append(character));

            return output.toString();
        }

    }

    public static class Type implements RecipeType<AlloyForgeRecipe> {
        private Type() {}

        public static final Identifier ID = AlloyForgery.id("forging");
        public static final Type INSTANCE = new Type();
    }
}
