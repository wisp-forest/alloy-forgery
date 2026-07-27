package io.wispforest.alloyforgery.fabric.utils;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import io.wispforest.alloyforgery.fabric.FluidHolderImpl;
import io.wispforest.alloyforgery.fabric.data.FabricResourceConditionHolder;
import io.wispforest.alloyforgery.forges.ForgeRegistry;
import io.wispforest.alloyforgery.utils.FluidStorage;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorageUtil;
import net.fabricmc.fabric.impl.recipe.ingredient.builtin.ComponentsIngredient;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public final class FabricGeneralPlatformUtils implements GeneralPlatformUtils {
    @Override
    public <T extends AbstractContainerMenu, D> MenuType<T> createScreenHandlerType(ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> packetCodec) {
        return new ExtendedMenuType<>(factory::create, packetCodec);
    }

    //--

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(Factory<T> factory) {
        return FabricBlockEntityTypeBuilder.create(factory::create).build();
    }

    @Override
    public void addToBlockEntity(BlockEntityType<ForgeControllerBlockEntity> type, Block... blocks) {
        for (var block : blocks) type.addValidBlock(block);
    }

    //--

    @Override
    public boolean interactWithFluidStorage(ForgeControllerBlockEntity controller, Player player, InteractionHand hand) {
        return FluidStorageUtil.interactWithFluidStorage(controller.<FluidHolderImpl>getFluidHolder(), player, hand);
    }

    @Override
    public FluidStorage createStorage(ForgeControllerBlockEntity controller) {
        return new FluidHolderImpl(controller::setChanged);
    }

    //--

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeManager manager, RecipeType<T> type, I input, Level world) {
        return manager.getAllMatches(type, input, world);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeManager manager, RecipeType<T> type) {
        return manager.getAllOfType(type);
    }

    @Override
    public Ingredient createStackIngredient(ItemStack stack) {
        return new ComponentsIngredient(Ingredient.of(stack.getItem()), stack.getComponentsPatch())
            .toVanilla();
    }

    //--

    @Override
    public void registerLoader(Identifier id, PackType packType, EndecDataLoader<?> loader, boolean requiresRegistries) {
        if (requiresRegistries) {
            loader.setRegistryGetter(store -> store.get(ResourceLoader.REGISTRY_LOOKUP_KEY));
        }

        ResourceLoader.get(packType).registerReloadListener(id, loader);
    }

    //--

    @Override
    public ResourceConditionHolder createConditionsHolder() {
        return new FabricResourceConditionHolder(new ArrayList<>());
    }

    //--

    @Override
    public void handleDefinitionEntry(ForgeRegistry.EntryHolder holder) {
        holder.registerBlock();
        holder.registerItem();
    }
}
