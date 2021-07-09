package wraith.alloyforgery;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.block.ForgeControllerBlockEntity;
import wraith.alloyforgery.forges.FuelDataLoader;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;
import wraith.alloyforgery.recipe.AlloyForgeRecipeSerializer;

public class AlloyForgery implements ModInitializer {

    public static final String MOD_ID = "alloy_forgery";

    public static ItemGroup ALLOY_FORGERY_GROUP;

    public static BlockEntityType<ForgeControllerBlockEntity> FORGE_CONTROLLER_BLOCK_ENTITY;
    public static ScreenHandlerType<AlloyForgeScreenHandler> ALLOY_FORGE_SCREEN_HANDLER_TYPE;

    @Override
    public void onInitialize() {
        ALLOY_FORGERY_GROUP = FabricItemGroupBuilder.create(id("alloy_forgery")).icon(() -> new ItemStack(Items.BRICKS)).build();
        ALLOY_FORGE_SCREEN_HANDLER_TYPE = ScreenHandlerRegistry.registerSimple(id("alloy_forge"), AlloyForgeScreenHandler::new);

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new FuelDataLoader());

        Registry.register(Registry.RECIPE_TYPE, AlloyForgeRecipe.Type.ID, AlloyForgeRecipe.Type.INSTANCE);
        Registry.register(Registry.RECIPE_SERIALIZER, AlloyForgeRecipe.Type.ID, AlloyForgeRecipeSerializer.INSTANCE);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
