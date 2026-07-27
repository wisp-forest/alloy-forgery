package io.wispforest.alloyforgery.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonSyntaxException;
import io.wispforest.alloyforgery.forges.ForgeTier;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.util.RecipeRemainderStorage;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.tags.TagKey;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AlloyForgeRecipe implements Recipe<AlloyForgeRecipeInput> {

    private static final Map<Item, ItemStack> GLOBAL_REMAINDERS = new HashMap<>();

    private static final List<Integer> INPUT_SLOT_INDICES = IntStream.rangeClosed(0, 9).boxed().toList();

    public static final Map<AlloyForgeRecipe, PendingRecipeData> PENDING_RECIPES = new HashMap<>();

    public final Optional<RawAlloyForgeRecipe> rawRecipeData;

    /**
     * Used for Recipes that were adapted to Alloy Forge Recipes instead of created from scratch.
     * Such serves as a holder for the original Identifier of the Recipe for Item Viewer Mods like REI and EMI
     */
    private Optional<Identifier> secondaryID = Optional.empty();

    private final Map<Ingredient, Integer> inputs;
    private ItemStack output;

    private final int minForgeTier;
    private final int fuelPerTick;

    private ImmutableMap<OverrideRange, ItemStack> tierOverrides;

    public AlloyForgeRecipe(Optional<RawAlloyForgeRecipe> rawRecipeData, Map<Ingredient, Integer> inputs, ItemStack output, int minForgeTier, int fuelPerTick, Map<OverrideRange, ItemStack> overrides) {
        this.rawRecipeData = rawRecipeData;

        this.inputs = inputs;
        this.output = output;
        this.minForgeTier = minForgeTier;
        this.fuelPerTick = fuelPerTick;

        this.tierOverrides = ImmutableMap.copyOf(overrides);
    }

    public AlloyForgeRecipe(Map<Ingredient, Integer> inputs, ItemStack output, int minForgeTier, int fuelPerTick, Map<OverrideRange, ItemStack> overrides, Optional<Identifier> secondaryID) {
        this(Optional.empty(), inputs, output, minForgeTier, fuelPerTick, overrides);

        this.secondaryID = secondaryID;
    }

    public Optional<Identifier> secondaryID() {
        return this.secondaryID;
    }

    public void finishRecipe(HolderGetter.Provider registryLookup, PendingRecipeData pendingData, Function<AlloyForgeRecipe, Identifier> lookup) {
        if (pendingData.defaultTag() != null) {
            final var itemEntryList = registryLookup.lookupOrThrow(Registries.ITEM).get(pendingData.defaultTag().getA());

            itemEntryList.ifPresentOrElse(registryEntries -> {
                this.output = registryEntries.get(0).value().getDefaultInstance();
                this.output.setCount(pendingData.defaultTag().getB());
            }, () -> {
                throw new InvalidTagException("Default tag " + pendingData.defaultTag().getA().location() + " of recipe " + lookup.apply(this) + " must not be empty");
            });
        }

        final var overrides = ImmutableMap.<OverrideRange, ItemStack>builder();

        pendingData.unfinishedTierOverrides().forEach((range, override) -> {
            if (override.isCountOnly()) {
                ItemStack stack = this.output.copy();
                stack.setCount(override.count());

                if (!override.components().isEmpty()) {
                    stack.applyComponentsAndValidate(override.components());
                }

                overrides.put(range, stack);
            } else {
                overrides.put(range, override.stack());
            }
        });

        this.tierOverrides = overrides.build();
    }

    public static void addRemainders(Map<Item, ItemStack> remainders) {
        GLOBAL_REMAINDERS.putAll(remainders);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public boolean matches(AlloyForgeRecipeInput input, Level world) {
        return tryBind(input) != null;
    }

    public Int2IntMap tryBind(AlloyForgeRecipeInput input) {
        var indices = new ConcurrentLinkedQueue<>(INPUT_SLOT_INDICES);
        var boundSlots = new Int2IntLinkedOpenHashMap();

        for (var ingredient : this.inputs.entrySet()) {
            int remaining = ingredient.getValue();

            for (int index : indices) {
                var stack = input.getItem(index);

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
            var stack = input.getItem(index);
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
    @Nullable
    private PlacementInfo ingredientPlacement;

    @Override
    public PlacementInfo placementInfo() {
        if (this.ingredientPlacement == null) {
            this.ingredientPlacement = PlacementInfo.createFromOptionals(
                    getIngredientsMap().keySet()
                            .stream()
                            .map(Optional::of)
                            .toList());
        }

        return this.ingredientPlacement;
    }

    public Map<Ingredient, Integer> getIngredientsMap() {
        return inputs;
    }

    // Attempt to test if the passed inventory is a Controller to try and get the forgeTier
    // Better to use the getOutput though other means rather than this if not a controller
    @Override
    public ItemStack assemble(AlloyForgeRecipeInput input) {
        return (input.inventory() instanceof ForgeControllerBlockEntity controller)
            ? getResult(controller.forgeTier().value())
            : getBaseResult();
    }

    public void consumeIngredients(AlloyForgeRecipeInput input) {
        var inventory = input.inventory();
        this.tryBind(input).forEach(inventory::removeItem);
    }

    @Nullable
    public static NonNullList<ItemStack> gatherRemainders(RecipeHolder<AlloyForgeRecipe> recipeEntry, AlloyForgeRecipeInput input) {
        final var id = recipeEntry.id().identifier();
        final var recipe = recipeEntry.value();
        final var remainders = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        //noinspection UnstableApiUsage
        final var owoRemainders = RecipeRemainderStorage.has(id) ? RecipeRemainderStorage.get(id) : Map.<Item, ItemStack>of();

        if (owoRemainders.isEmpty() && GLOBAL_REMAINDERS.isEmpty()) return null;

        var setAnyRemainders = false;

        for (int i : recipe.tryBind(input).keySet()) {
            var item = input.getItem(i).getItem();

            if (!owoRemainders.isEmpty()) {
                if (!owoRemainders.containsKey(item)) continue;

                // FIXME - owo remainders no longer have any real item context
                //remainders.set(i, new ItemStack(owoRemainders.get(item)));

                setAnyRemainders = true;
            } else if (GLOBAL_REMAINDERS.containsKey(item)) {
                remainders.set(i, GLOBAL_REMAINDERS.get(item).copy());

                setAnyRemainders = true;
            }
        }

        return setAnyRemainders ? remainders : null;
    }

    /**
     * Quickly copy the base output for a recipe, skips calculations from {@link #getResult(int)}
     */
    @ApiStatus.Internal
    public ItemStack getBaseResult() {
        return this.output.copy();
    }

    public ItemStack getResult(int forgeTier) {
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
    public RecipeSerializer<? extends Recipe<AlloyForgeRecipeInput>> getSerializer() {
        return AlloyForgeRecipeSerializer.INSTANCE;
    }

    @Override
    public RecipeType<? extends Recipe<AlloyForgeRecipeInput>> getType() {
        return Type.INSTANCE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
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

    public static class Type implements RecipeType<AlloyForgeRecipe> {
        private Type() {}

        public static final Identifier ID = AlloyForgery.id("forging");
        public static final Type INSTANCE = new Type();
    }

    public record PendingRecipeData(@Nullable Tuple<TagKey<Item>, Integer> defaultTag, Map<OverrideRange, PendingOverride> unfinishedTierOverrides) { }

    public record PendingOverride(@Nullable Item item, int count, DataComponentPatch components) {
        public boolean isCountOnly() {
            return this.item == null;
        }

        public static PendingOverride onlyCount(int count) {
            return new PendingOverride(null, count, DataComponentPatch.EMPTY);
        }

        public static PendingOverride ofItem(Item item, int count) {
            return new PendingOverride(item, count, DataComponentPatch.EMPTY);
        }

        public ItemStack stack() {
            var stack = new ItemStack(item, count);

            stack.applyComponentsAndValidate(components);

            return new ItemStack(item, count);
        }
    }

    public static class InvalidTagException extends RuntimeException {
        public InvalidTagException(String message) {
            super(message);
        }
    }

    public record OverrideRange(int lowerBound, int upperBound) {

        public static Endec<OverrideRange> OVERRIDE_RANGE = StructEndecBuilder.of(
            Endec.INT.fieldOf("lowerBound", AlloyForgeRecipe.OverrideRange::lowerBound),
            Endec.INT.fieldOf("upperBound", AlloyForgeRecipe.OverrideRange::upperBound),
            AlloyForgeRecipe.OverrideRange::new
        );

        public OverrideRange(int lowerBound) {
            this(lowerBound, -1);
        }

        public boolean test(int value) {
            return value >= lowerBound && (upperBound == -1 || value <= upperBound);
        }

        public static AlloyForgeRecipe.OverrideRange fromString(String s) {
            AlloyForgeRecipe.OverrideRange overrideRange;

            if (s.matches("\\d+\\+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(s.substring(0, s.length() - 1)));
            } else if (s.matches("\\d+ to \\d+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(s.substring(0, s.indexOf(" "))), Integer.parseInt(s.substring(s.lastIndexOf(" ") + 1, s.length())));
            } else if (s.matches("\\d+")) {
                overrideRange = new AlloyForgeRecipe.OverrideRange(Integer.parseInt(s), Integer.parseInt(s));
            } else {
                throw new JsonSyntaxException("Invalid override range token: " + s);
            }

            return overrideRange;
        }

        public Component toText(boolean isClientSide) {
            var lowerTierName = ForgeTier.toName(isClientSide, lowerBound);

            if (upperBound != lowerBound) {
                if (upperBound == -1) {
                    return AlloyForgery.tooltipTranslation("override_range.greater_or_equal_to", lowerTierName);
                } else {
                    var to = " to ".chars().mapToObj(value -> (char) value).toArray(Character[]::new);

                    var upperTierName = ForgeTier.toName(isClientSide, lowerBound);

                    return AlloyForgery.tooltipTranslation("override_range.range", lowerTierName, upperTierName);
                }
            } else {
                return AlloyForgery.tooltipTranslation("override_range.equal_to", lowerTierName);
            }
        }

        // Biograpgy:
        // - Proginator: glisco
        // - Reason: Used massive intellect to handcraft one of the methods of all time as an act of defence to all
        //   proper coding practices meaning it is ART, and it must be protected!
        //
        // Curator Notes:
        // - Noaaan: Any attempt to optimize this mess has been unilaterally denied
        // - Blodhgarm: With permission from proginator, adjustments to the code WAS made
        //
        // Any issues may be direct at the original author though the below Code:
        //
        // ██████████████████████████████████████████████████████
        // ██              ████  ██████  ████  ██              ██
        // ██  ██████████  ██    ██            ██  ██████████  ██
        // ██  ██      ██  ████      ████  ██████  ██      ██  ██
        // ██  ██      ██  ██    ██          ████  ██      ██  ██
        // ██  ██      ██  ██████  ██    ████  ██  ██      ██  ██
        // ██  ██████████  ██  ██  ██████    ████  ██████████  ██
        // ██              ██  ██  ██  ██  ██  ██              ██
        // ████████████████████████  ████  ██  ██████████████████
        // ██          ██          ████  ██████  ██  ██  ██  ████
        // ████      ██  ██████  ██  ██  ██████████  ██████  ████
        // ██  ██████  ██    ██  ██████      ██████████  ██    ██
        // ██        ██████          ████            ████████  ██
        // ██    ████  ██    ██  ██████  ██████        ██      ██
        // ██    ████  ██████          ██  ██  ████  ██  ██  ████
        // ██  ██  ██      ██  ██  ██        ██  ██      ██    ██
        // ██  ██    ██  ████  ██    ██████████████    ██████  ██
        // ██  ██  ██  ██  ██  ██        ██            ██  ██████
        // ██████████████████  ████  ██    ██  ██████    ████████
        // ██              ██  ██  ██████████  ██  ██  ██      ██
        // ██  ██████████  ████  ██  ████  ██  ██████    ████  ██
        // ██  ██      ██  ██  ██  ████                ██    ████
        // ██  ██      ██  ██  ██          ████    ██          ██
        // ██  ██      ██  ██      ██  ████████  ██████    ██  ██
        // ██  ██████████  ██  ██████████████  ████      ████  ██
        // ██              ██  ████████    ████                ██
        // ██████████████████████████████████████████████████████
        //
        @Override
        public String toString() {
            var outString = String.valueOf(lowerBound);
            var chars = outString.chars().mapToObj(value -> (char) value);

            if (upperBound != lowerBound) {
                if (upperBound == -1) {
                    chars = Stream.concat(chars, Stream.of('+'));
                } else {
                    var to = " to ".chars().mapToObj(value -> (char) value).toArray(Character[]::new);
                    chars = Stream.concat(chars, Arrays.stream(to));

                    var bound = String.valueOf(upperBound).chars().mapToObj(value -> (char) value).toArray(Character[]::new);
                    chars = Stream.concat(chars, Arrays.stream(bound));
                }
            }

            var output = new StringBuffer();
            chars.forEach(character -> output.append(character));

            return output.toString();
        }
    }
}
