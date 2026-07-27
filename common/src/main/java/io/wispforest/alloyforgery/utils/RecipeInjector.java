package io.wispforest.alloyforgery.utils;

import com.google.common.collect.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import io.wispforest.alloyforgery.mixin.RecipeMapAccessor;
import io.wispforest.alloyforgery.mixin.RecipeManagerAccessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Helper class to safety allow for injecting recipes into the Recipe Manager <b>without
 * overriding or modifying existing Recipes. </b>
 * <p/>
 * Primarily used to either add compatibility for existing recipes by converting to another form
 * or adding new recipes.
 */
public final class RecipeInjector {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Event called on `ServerLifecycleEvents#SERVER_STARTED` or {@link DataPackEvents#BEFORE_SYNC} which adds
     * new recipes to the RecipeManager before sync to Players
     */
    public static final Event<AddRecipes> ADD_RECIPES = EventFactory.createArrayBacked(AddRecipes.class, addRecipes -> (instance) -> {
        for (AddRecipes addRecipe : addRecipes) {
            addRecipe.addRecipes(instance);
        }
    });

    private final RecipeManager manager;
    private final Level world;

    private final Multimap<RecipeType<?>, RecipeHolder<?>> recipes = HashMultimap.create();
    private final Map<Identifier, RecipeHolder<?>> recipesById = new HashMap<>();

    public RecipeInjector(RecipeManager manager, Level world) {
        this.manager = manager;
        this.world = world;
    }

    /**
     * Attempts to register a given recipe for addition to the recipe manager if
     * 1. Such recipe has a registered {@link RecipeType}
     * 2. Such is found to not have an existing Identifier within {@link RecipeAccess}
     *
     * @param recipe The Recipe
     * @param <T>    Type of the given Recipe
     */
    public <R extends Recipe<T>, T extends RecipeInput> void addRecipe(Identifier id, R recipe) {
        if (BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType()) == null) {
            throw new IllegalStateException("Unable to add Recipe for a RecipeType not registered!");
        }

        var type = (RecipeType<R>) recipe.getType();

        var bl = getAllOfType(type)
            .stream()
            .anyMatch(recipeEntry -> id.equals(recipeEntry.id().identifier()));

        if (bl) {
            LOGGER.error("[RecipeInjector]: Unable to add a given recipe due to being the same Identifier with the given Type. [ID: {}]", id);

            return;
        }

        var recipeEntry = new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, id), recipe);

        recipes.put(recipe.getType(), recipeEntry);
        recipesById.put(id, recipeEntry);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level world) {
        return GeneralPlatformUtils.INSTANCE.getAllMatches(this.manager, type, input, world);
    }

    /**
     * @return the collection of recipe entries of given type
     */
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type) {
        return GeneralPlatformUtils.INSTANCE.getAllOfType(this.manager, type);
    }

    /**
     * @return The current instance of the {@link RecipeAccess}
     */
    public RecipeManager manager() {
        return this.manager;
    }

    public HolderLookup.Provider lookup() {
        return ((RecipeManagerAccessor) this.manager).af$getRegistryLookup();
    }

    public List<ItemStack> getStacks(Ingredient ingredient) {
        var ctx = SlotDisplayContext.fromLevel(this.world);

        return ingredient.display().resolveForStacks(ctx);
    }

    /**
     * Primary Event for adding new Recipes
     */
    public interface AddRecipes {
        void addRecipes(RecipeInjector instance);
    }

    //--

    public static void injectRecipes(MinecraftServer server) {
        var manager = server.getRecipeManager();
        var injector = new RecipeInjector(server.getRecipeManager(), server.getLevel(Level.OVERWORLD));

        ADD_RECIPES.invoker().addRecipes(injector);

        var preparedRecipesAccessor = (RecipeMapAccessor) ((RecipeManagerAccessor) manager).af$preparedRecipes();

        injector.recipes.putAll(preparedRecipesAccessor.af$getRecipes());
        injector.recipesById.putAll(preparedRecipesAccessor.af$getRecipesById());

        preparedRecipesAccessor.af$setRecipes(ImmutableMultimap.copyOf(injector.recipes));
        preparedRecipesAccessor.af$setRecipesById(ImmutableMap.copyOf(injector.recipesById));

        injector.recipes.clear();
        injector.recipesById.clear();
    }
}
