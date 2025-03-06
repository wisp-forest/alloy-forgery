package wraith.alloyforgery.utils;

import com.google.common.collect.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import wraith.alloyforgery.mixin.RecipeManagerAccessor;
import java.util.HashMap;
import java.util.Map;

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
     * Event called on {@link ServerLifecycleEvents#SERVER_STARTED} or {@link DataPackEvents#BEFORE_SYNC} which adds
     * new recipes to the RecipeManager before sync to Players
     */
    public static final Event<AddRecipes> ADD_RECIPES = EventFactory.createArrayBacked(AddRecipes.class, addRecipes -> (instance) -> {
        for (AddRecipes addRecipe : addRecipes) {
            addRecipe.addRecipes(instance);
        }
    });

    private final RecipeManager manager;

    private final Multimap<RecipeType<?>, RecipeEntry<?>> recipes = HashMultimap.create();
    private final Map<Identifier, RecipeEntry<?>> recipesById = new HashMap<>();

    public RecipeInjector(RecipeManager manager) {
        this.manager = manager;
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

        var bl = manager.listAllOfType(type)
            .stream()
            .anyMatch(recipeEntry -> id.equals(recipeEntry.id()));

        if (bl) {
            LOGGER.error("[RecipeInjector]: Unable to add a given recipe due to being the same Identifier with the given Type. [ID: {}]", id);

            return;
        }

        var recipeEntry = new RecipeEntry<>(id, recipe);

        recipes.put(recipe.getType(), recipeEntry);
        recipesById.put(id, recipeEntry);
    }

    /**
     * @return The current instance of the {@link RecipeManager}
     */
    public RecipeManager manager() {
        return this.manager;
    }

    public RegistryWrapper.WrapperLookup lookup() {
        return ((RecipeManagerAccessor) this.manager).af$getRegistryLookup();
    }

    /**
     * Primary Event for adding new Recipes
     */
    public interface AddRecipes {
        void addRecipes(RecipeInjector instance);
    }

    //--

    public static void initEvents() {
        ServerLifecycleEvents.SERVER_STARTED.register(RecipeInjector::injectRecipes);

        DataPackEvents.BEFORE_SYNC.register(RecipeInjector::injectRecipes);
    }

    public static void injectRecipes(MinecraftServer server) {
        var manager = server.getRecipeManager();
        var injector = new RecipeInjector(server.getRecipeManager());

        ADD_RECIPES.invoker().addRecipes(injector);

        var managerAccessor = (RecipeManagerAccessor) manager;

        injector.recipes.putAll(managerAccessor.af$getRecipes());
        injector.recipesById.putAll(managerAccessor.af$getRecipesById());

        managerAccessor.af$setRecipes(ImmutableMultimap.copyOf(injector.recipes));
        managerAccessor.af$setRecipesById(ImmutableMap.copyOf(injector.recipesById));

        injector.recipes.clear();
        injector.recipesById.clear();
    }
}
