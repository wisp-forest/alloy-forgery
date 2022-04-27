package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.forges.UnifiedInventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AlloyForgeRecipe implements Recipe<Inventory> {

    private final Identifier id;

    private final Map<Ingredient, Integer> inputs;

    private final ItemStack output;

    private final int minForgeTier;
    private final int fuelPerTick;

    private final ImmutableMap<OverrideRange, ItemStack> tierOverrides;

    public AlloyForgeRecipe(Identifier id, Map<Ingredient, Integer> inputs, ItemStack output, int minForgeTier, int fuelPerTick, ImmutableMap<OverrideRange, ItemStack> overrides) {
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
        int matchedIngredients = 0;
        UnifiedInventory unifiedInventory = (UnifiedInventory)inventory;

        //Confirm that the there is enough items for this recipe to even work
        if(unifiedInventory.getUnifiedInventory().size() != inputs.size())
            return false;

        List<Map.Entry<Ingredient, Integer>> localInputs = new ArrayList<>(inputs.entrySet());

        for(Map.Entry<Item, Integer> invEntry : unifiedInventory.getUnifiedInventory().entrySet()){
            boolean isValidIngredient = false;

            for(int i = 0; i < localInputs.size(); i++){
                Map.Entry<Ingredient, Integer> inputEntry = localInputs.get(i);

                //First test if we have enough Items based on the Ingredients needed amount for the recipe and then test if the item is am ingredient
                if((invEntry.getValue() - inputEntry.getValue() >= 0) && inputEntry.getKey().test(invEntry.getKey().getDefaultStack())){
                    isValidIngredient = true;

                    matchedIngredients++;

                    localInputs.remove(i);

                    break;
                }
            }

            if(!isValidIngredient){
                return false;
            }
        }

        return matchedIngredients == inputs.size();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        List<Ingredient> allIngredients = new ArrayList<>();

        for(Map.Entry<Ingredient, Integer> entry : inputs.entrySet()){
            for(int i = 0; i < entry.getValue(); i++){
                allIngredients.add(entry.getKey());
            }
        }

        return DefaultedList.copyOf(Ingredient.EMPTY, allIngredients.toArray(new Ingredient[0]));
    }

    public Map<Ingredient, Integer> getIngredientsMap(){
        return inputs;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return ItemStack.EMPTY;
    }

    /**
     * Method to reduce the Items within the Unified Inventory based of the recipe ingredient requirements
     */
    public void consumeNeededIngredients(Inventory inventory){
        UnifiedInventory unifiedInventory = (UnifiedInventory)inventory;

        for(Map.Entry<Item, Integer> invEntry : unifiedInventory.getUnifiedInventory().entrySet()){
            for(Map.Entry<Ingredient, Integer> inputEntry : inputs.entrySet()){
                if(inputEntry.getKey().test(invEntry.getKey().getDefaultStack())){
                    unifiedInventory.removeItems(invEntry.getKey(), inputEntry.getValue());
                }
            }
        }
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

    public record OverrideRange(int lowerBound, int upperBound) {

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
                    var to = " to ".chars().mapToObj(value -> (char) value).collect(Collectors.toList());
                    to.forEach(character -> chars.add(character));

                    var bound = String.valueOf(upperBound).chars().mapToObj(value -> (char) value).collect(Collectors.toList());
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
