package wraith.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import io.wispforest.owo.util.RecipeRemainderStorage;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgery;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AlloyForgeRecipe implements Recipe<Inventory> {

    private static final Map<Item, ItemStack> GLOBAL_REMAINDERS = new HashMap<>();

    private static final List<Integer> INPUT_SLOT_INDICES = IntStream.rangeClosed(0, 9).boxed().toList();

    public static final Map<AlloyForgeRecipe, PendingRecipeData> PENDING_RECIPES = new HashMap<>();

    private final Identifier id;
    private Optional<Identifier> secondaryID = Optional.empty();

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

    public AlloyForgeRecipe setSecondaryID(Identifier id){
        this.secondaryID = Optional.of(id);

        return this;
    }

    public Optional<Identifier> secondaryID(){
        return this.secondaryID;
    }

    public void finishRecipe(PendingRecipeData pendingData) {
        if (pendingData.defaultTag() != null) {
            final var itemEntryList = Registry.ITEM.getEntryList(pendingData.defaultTag().getLeft());

            itemEntryList.ifPresentOrElse(registryEntries -> {
                this.output = registryEntries.get(0).value().getDefaultStack();
                this.output.setCount(pendingData.defaultTag().getRight());

            }, () -> {
                throw new InvalidTagException("Default tag " + pendingData.defaultTag().getLeft().id() + " of recipe " + this.id + " must not be empty");
            });
        }

        final var overrides = ImmutableMap.<OverrideRange, ItemStack>builder();

        pendingData.unfinishedTierOverrides().forEach((range, override) -> {
            if (override.isCountOnly()) {
                ItemStack stack = this.output.copy();
                stack.setCount(override.count());

                overrides.put(range, stack);
            } else if (override.stack() != null) {
                overrides.put(range, override.stack());
            }
        });

        this.tierOverrides = overrides.build();
    }

    public static void addRemainders(Map<Item, ItemStack> remainders){
        GLOBAL_REMAINDERS.putAll(remainders);
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return tryBind(inventory) != null;
    }

    public Int2IntMap tryBind(Inventory inventory) {
        var indices = new ConcurrentLinkedQueue<>(INPUT_SLOT_INDICES);
        var boundSlots = new Int2IntLinkedOpenHashMap();

        for (var ingredient : this.inputs.entrySet()) {
            int remaining = ingredient.getValue();

            for (int index : indices) {
                var stack = inventory.getStack(index);

                if (ingredient.getKey().test(stack)) {
                    boundSlots.put(index, Math.min(stack.getCount(), remaining));
                    indices.remove(index);

                    remaining -= stack.getCount();
                    if (remaining <= 0) break;
                }
            }

            if (remaining > 0) {
                return null;
            }
        }

        verification:
        for (int index : indices) {
            var stack = inventory.getStack(index);
            if (stack.isEmpty()) continue;

            for (var ingredient : this.inputs.keySet()) {
                if (ingredient.test(stack)) {
                    continue verification;
                }
            }

            return null;
        }

        return boundSlots;
    }

    @SuppressWarnings("SuspiciousToArrayCall")
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
        this.tryBind(inventory).forEach(inventory::removeStack);
        return ItemStack.EMPTY;
    }

    @Nullable
    public DefaultedList<ItemStack> gatherRemainders(Inventory inventory) {
        final var remainders = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);
        //noinspection UnstableApiUsage
        final var owoRemainders = RecipeRemainderStorage.has(this.getId()) ? RecipeRemainderStorage.get(this.getId()) : Map.<Item, ItemStack>of();

        if(owoRemainders.isEmpty() && GLOBAL_REMAINDERS.isEmpty()) return null;

        var setAnyRemainders = false;

        for (int i : this.tryBind(inventory).keySet()) {
            var item = inventory.getStack(i).getItem();

            if (!owoRemainders.isEmpty()) {
                if (!owoRemainders.containsKey(item)) continue;

                remainders.set(i, owoRemainders.get(item).copy());

                setAnyRemainders = true;
            } else if(GLOBAL_REMAINDERS.containsKey(item)){
                remainders.set(i, GLOBAL_REMAINDERS.get(item).copy());

                setAnyRemainders = true;
            }
        }

        return setAnyRemainders ? remainders : null;
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    // Do not override
    @Override
    @ApiStatus.Internal
    @Deprecated
    public ItemStack getOutput() {
        return this.output.copy();
    }

    /**
     * Quickly copy the base output for a recipe, skips calculations from {@link #getOutput(int)}
     */
    @ApiStatus.Internal
    public ItemStack getBaseOutput() {
    }

    public ItemStack getOutput(int forgeTier) {
        ItemStack stack = tierOverrides.getOrDefault(tierOverrides.keySet().stream()
                        .filter(overrideRange -> overrideRange.test(forgeTier))
                        .findAny()
                        .orElse(null), output)
                .copy();

        if (stack.getItem() == Items.AIR) {
            int stackCount = stack.getCount();

            stack = this.output.copy();

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

        // Any attempt to optimize this mess has been unilaterally denied
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

    public record PendingRecipeData(@Nullable Pair<TagKey<Item>, Integer> defaultTag,
                                    ImmutableMap<OverrideRange, PendingOverride> unfinishedTierOverrides) {
    }

    public record PendingOverride(@Nullable ItemStack stack, int count) {
        public boolean isCountOnly() {
            return this.stack == null;
        }

        public static PendingOverride onlyCount(int count) {
            return new PendingOverride(null, count);
        }

        public static PendingOverride ofStack(ItemStack stack) {
            return new PendingOverride(stack, stack.getCount());
        }
    }

    public static class InvalidTagException extends RuntimeException {
        public InvalidTagException(String message) {
            super(message);
        }
    }
}
