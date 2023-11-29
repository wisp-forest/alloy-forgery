package wraith.alloyforgery.utils;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
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

    private final Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes = new HashMap<>();
    private final Map<Identifier, Recipe<?>> recipesById = new HashMap<>();

    public RecipeInjector(RecipeManager manager){
        this.manager = manager;
    }

    /**
     * Attempts to register a given recipe for addition to the recipe manager if
     *  1. Such recipe has a registered {@link RecipeType}
     *  2. Such is found to not have an existing Identifier within {@link RecipeManager}
     *
     * @param recipe The Recipe
     * @param <T> Type of the given Recipe
     */
    public <T extends Recipe<C>, C extends Inventory> void addRecipe(T recipe){
        if(Registries.RECIPE_TYPE.getId(recipe.getType()) == null){
            throw new IllegalStateException("Unable to add Recipe for a RecipeType not registered!");
        }

        var type = (RecipeType<T>) recipe.getType();

        var bl = manager.listAllOfType(type)
                .stream()
                .anyMatch(recipe1 -> recipe1.getId().equals(recipe.getId()));

        if(bl){
            LOGGER.error("[RecipeInjector]: Unable to add a given recipe due to being the same Identifier with the given Type. [ID: {}]", recipe.getId());

            return;
        }

        recipes.computeIfAbsent(recipe.getType(), t -> new HashMap<>()).put(recipe.getId(), recipe);
        recipesById.put(recipe.getId(), recipe);
    }

    /**
     * @return The current instance of the {@link RecipeManager}
     */
    public RecipeManager manager(){
        return this.manager;
    }

    /**
     * Primary Event for adding new Recipes
     */
    public interface AddRecipes {
        void addRecipes(RecipeInjector instance);
    }

    //--

    public static void initEvents(){
        ServerLifecycleEvents.SERVER_STARTED.register(RecipeInjector::injectRecipes);

        DataPackEvents.BEFORE_SYNC.register(RecipeInjector::injectRecipes);
    }

    public static void injectRecipes(MinecraftServer server){
        var manager = server.getRecipeManager();
        var injector = new RecipeInjector(server.getRecipeManager());

        ADD_RECIPES.invoker().addRecipes(injector);

        var managerAccessor = (RecipeManagerAccessor) manager;

        managerAccessor.af$getRecipes().forEach((recipeType, identifierRecipeMap) -> {
            injector.recipes.computeIfAbsent(recipeType, t -> new HashMap<>()).putAll(identifierRecipeMap);
        });

        injector.recipesById.putAll(managerAccessor.af$getRecipesById());

        managerAccessor.af$setRecipes(ImmutableMap.copyOf(injector.recipes));
        managerAccessor.af$setRecipesById(ImmutableMap.copyOf(injector.recipesById));

        injector.recipes.clear();
        injector.recipesById.clear();
    }
}
