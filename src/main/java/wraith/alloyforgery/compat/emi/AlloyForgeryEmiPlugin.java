package wraith.alloyforgery.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.forges.ForgeRegistry;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

public class AlloyForgeryEmiPlugin /*implements EmiPlugin*/ {

    private static final Identifier FORGE_ID = AlloyForgery.id("alloy_forge");
    public static final EmiRecipeCategory FORGE_CATEGORY = new EmiRecipeCategory(FORGE_ID, EmiStack.of(ForgeRegistry.getControllerBlocks().get(0)));


    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(FORGE_CATEGORY);

        for (Block controller : ForgeRegistry.getControllerBlocks()) {
            registry.addWorkstation(FORGE_CATEGORY, EmiStack.of(controller));
        }

        for (AlloyForgeRecipe recipe : registry.getRecipeManager().listAllOfType(AlloyForgeRecipe.Type.INSTANCE)) {
            registry.addRecipe(new AlloyForgeryEmiRecipe(recipe));
        }

        registry.addRecipeHandler(AlloyForgery.ALLOY_FORGE_SCREEN_HANDLER_TYPE, new AlloyForgeryEmiRecipeHandler());
    }
}
