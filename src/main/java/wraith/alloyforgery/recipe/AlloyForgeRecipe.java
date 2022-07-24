package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntryList;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;

import java.util.*;
import java.util.stream.Collectors;

public class AlloyForgeRecipe implements Recipe<Inventory> {

    private static final Logger LOGGER = LogManager.getLogger(AlloyForgeRecipe.class);

    public static final Map<AlloyForgeRecipe, RecipeFinisher> PENDING_RECIPES = new HashMap<>();

    private final Identifier id;

    private final Map<Ingredient, Integer> inputs;

    private ItemStack output;

    private final int minForgeTier;
    private final int fuelPerTick;

    private ImmutableMap<OverrideRange, ItemStack> tierOverrides;

    public AlloyForgeRecipe(Identifier id, Map<Ingredient, Integer> inputs, ItemStack output, int minForgeTier, int fuelPerTick, ImmutableMap<OverrideRange, ItemStack> overrides) {
        this.id = id;
        this.inputs = inputs;
        this.output = output;
        this.minForgeTier = minForgeTier;
        this.fuelPerTick = fuelPerTick;

        this.tierOverrides = overrides;
    }

    public void setTierOverrides(ImmutableMap<OverrideRange, ItemStack> overrides) {
        this.tierOverrides = overrides;
    }

    public void finishRecipe(RecipeFinisher finisher) {
        if(finisher.pair != null) {
            final var itemEntryList = Registry.ITEM.getEntryList(finisher.pair.getLeft());

            itemEntryList.ifPresentOrElse(registryEntries -> {
                this.output = registryEntries.get(0).value().getDefaultStack();

                this.output.setCount(finisher.pair.getRight());

            }, () -> {
                throw new InvaildRecipeTagException("[AlloyForgeRecipe]: A Recipe with a Default tag was found to be empty and was loaded!!!!");
            });
        }

        final var mapBuilder = ImmutableMap.<OverrideRange, ItemStack>builder();

        finisher.unfinishedTierOverrides.forEach((key, pair) -> {
            if (pair.getRight() != -1) {
                ItemStack stack = output.copy();

                stack.setCount(pair.getRight());

                mapBuilder.put(key, stack);
            } else {
                mapBuilder.put(key, pair.getLeft());
            }
        });

        tierOverrides = mapBuilder.build();
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        int matchedIngredients = 0;
        final var unifiedView = ((ForgeControllerBlockEntity) inventory).asUnifiedView();

        //Confirm that the there is enough items for this recipe to even work
        if (unifiedView.getUnifiedInventory().size() != inputs.size())
            return false;

        final var localInputs = new ArrayList<>(inputs.entrySet());

        for (Map.Entry<Item, Integer> invEntry : unifiedView.getUnifiedInventory().entrySet()) {
            boolean isValidIngredient = false;

            for (int i = 0; i < localInputs.size(); i++) {
                Map.Entry<Ingredient, Integer> inputEntry = localInputs.get(i);

                //First test if we have enough Items based on the Ingredients needed amount for the recipe and then test if the item is am ingredient
                if ((invEntry.getValue() - inputEntry.getValue() >= 0) && inputEntry.getKey().test(invEntry.getKey().getDefaultStack())) {
                    isValidIngredient = true;
                    matchedIngredients++;

                    localInputs.remove(i);
                    break;
                }
            }

            if (!isValidIngredient) {
                return false;
            }
        }

        return matchedIngredients == inputs.size();
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        final var allIngredients = new ArrayList<>();

        for (Map.Entry<Ingredient, Integer> entry : inputs.entrySet()) {
            for (int i = 0; i < entry.getValue(); i++) {
                allIngredients.add(entry.getKey());
            }
        }

        return DefaultedList.copyOf(Ingredient.EMPTY, allIngredients.toArray(Ingredient[]::new));
    }

    public Map<Ingredient, Integer> getIngredientsMap() {
        return inputs;
    }

    @Override
    public ItemStack craft(Inventory inventory) {
        return ItemStack.EMPTY;
    }

    /**
     * Method to reduce the Items within the Unified Inventory based of the recipe ingredient requirements
     */
    public void consumeNeededIngredients(Inventory inventory) {
        final var unifiedView = ((ForgeControllerBlockEntity) inventory).asUnifiedView();

        for (final var item : new HashSet<>(unifiedView.getUnifiedInventory().keySet())) {
            inputs.forEach((input, inputCount) -> {
                if (input.test(item.getDefaultStack())) {
                    unifiedView.removeItems(item, inputCount);
                }
            });
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
        ItemStack stack = tierOverrides.getOrDefault(tierOverrides.keySet().stream().filter(overrideRange -> overrideRange.test(forgeTier)).findAny().orElse(null), output).copy();

        if (stack.getItem() == Items.AIR) {
            int stackCount = stack.getCount();

            stack = output.copy();

            stack.setCount(stackCount);
        }

        return stack;
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
        private Type() {
        }

        public static final Identifier ID = AlloyForgery.id("forging");
        public static final Type INSTANCE = new Type();
    }

    public static record RecipeFinisher(@Nullable Pair<TagKey<Item>, Integer> pair,
                                        ImmutableMap<OverrideRange, Pair<ItemStack, Integer>> unfinishedTierOverrides) {}

    public static class InvaildRecipeTagException extends RuntimeException {
        public InvaildRecipeTagException(String message) {
            super(message);
        }
    }
}
