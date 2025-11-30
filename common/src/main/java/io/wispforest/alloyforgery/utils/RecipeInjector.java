package io.wispforest.alloyforgery.utils;

import com.google.common.collect.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.display.SlotDisplayContexts;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import io.wispforest.alloyforgery.mixin.PreparedRecipesAccessor;
import io.wispforest.alloyforgery.mixin.ServerRecipeManagerAccessor;

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

    private final ServerRecipeManager manager;
    private final World world;

    private final Multimap<RecipeType<?>, RecipeEntry<?>> recipes = HashMultimap.create();
    private final Map<Identifier, RecipeEntry<?>> recipesById = new HashMap<>();

    public RecipeInjector(ServerRecipeManager manager, World world) {
        this.manager = manager;
        this.world = world;
    }

    /**
     * Attempts to register a given recipe for addition to the recipe manager if
     * 1. Such recipe has a registered {@link RecipeType}
     * 2. Such is found to not have an existing Identifier within {@link RecipeManager}
     *
     * @param recipe The Recipe
     * @param <T>    Type of the given Recipe
     */
    public <R extends Recipe<T>, T extends RecipeInput> void addRecipe(Identifier id, R recipe) {
        if (Registries.RECIPE_TYPE.getId(recipe.getType()) == null) {
            throw new IllegalStateException("Unable to add Recipe for a RecipeType not registered!");
        }

        var type = (RecipeType<R>) recipe.getType();

        var bl = getAllOfType(type)
            .stream()
            .anyMatch(recipeEntry -> id.equals(recipeEntry.id().getValue()));

        if (bl) {
            LOGGER.error("[RecipeInjector]: Unable to add a given recipe due to being the same Identifier with the given Type. [ID: {}]", id);

            return;
        }

        var recipeEntry = new RecipeEntry<>(RegistryKey.of(RegistryKeys.RECIPE, id), recipe);

        recipes.put(recipe.getType(), recipeEntry);
        recipesById.put(id, recipeEntry);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(RecipeType<T> type, I input, World world) {
        return GeneralPlatformUtils.INSTANCE.getAllMatches(this.manager, type, input, world);
    }

    /**
     * @return the collection of recipe entries of given type
     */
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(RecipeType<T> type) {
        return GeneralPlatformUtils.INSTANCE.getAllOfType(this.manager, type);
    }

    /**
     * @return The current instance of the {@link RecipeManager}
     */
    public ServerRecipeManager manager() {
        return this.manager;
    }

    public RegistryWrapper.WrapperLookup lookup() {
        return ((ServerRecipeManagerAccessor) this.manager).af$getRegistryLookup();
    }

    public List<ItemStack> getStacks(Ingredient ingredient) {
        var ctx = SlotDisplayContexts.createParameters(this.world);

        return ingredient.toDisplay().getStacks(ctx);
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
        var injector = new RecipeInjector(server.getRecipeManager(), server.getWorld(World.OVERWORLD));

        ADD_RECIPES.invoker().addRecipes(injector);

        var preparedRecipesAccessor = (PreparedRecipesAccessor) ((ServerRecipeManagerAccessor) manager).af$preparedRecipes();

        injector.recipes.putAll(preparedRecipesAccessor.af$getRecipes());
        injector.recipesById.putAll(preparedRecipesAccessor.af$getRecipesById());

        preparedRecipesAccessor.af$setRecipes(ImmutableMultimap.copyOf(injector.recipes));
        preparedRecipesAccessor.af$setRecipesById(ImmutableMap.copyOf(injector.recipesById));

        injector.recipes.clear();
        injector.recipesById.clear();
    }
}
