package io.wispforest.alloyforgery.neoforge.utils;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.client.BlockEntityLocation;
import io.wispforest.alloyforgery.data.providers.ResourceConditionHolder;
import io.wispforest.alloyforgery.forges.ForgeRegistry;
import io.wispforest.alloyforgery.neoforge.FluidHolderImpl;
import io.wispforest.alloyforgery.neoforge.data.NeoforgeResourceConditionHolder;
import io.wispforest.alloyforgery.utils.FluidStorage;
import io.wispforest.alloyforgery.utils.GeneralPlatformUtils;
import io.wispforest.alloyforgery.utils.data.EndecDataLoader;
import io.wispforest.endec.format.bytebuf.ByteBufSerializer;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class NeoforgeGeneralPlatformUtils implements GeneralPlatformUtils {
    @Override
    public <T extends ScreenHandler, D> ScreenHandlerType<T> createScreenHandlerType(ExtendedFactory<T, D> factory, PacketCodec<? super RegistryByteBuf, D> packetCodec) {
        return new ScreenHandlerType<>(
            (IContainerFactory<T>) (i, inv, buf) -> factory.create(i, inv, packetCodec.decode(buf)),
            FeatureFlags.VANILLA_FEATURES
        );
    }

    //--

    private static final Set<Block> CONTROLLER_BLOCKS = new HashSet<>();

    @Override
    public <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(Factory<T> factory) {
        return new BlockEntityType<T>(factory::create, CONTROLLER_BLOCKS);
    }

    @Override
    public void addToBlockEntity(BlockEntityType<ForgeControllerBlockEntity> type, Block... blocks) {
        if (type != ForgeControllerBlockEntity.FORGE_CONTROLLER_BLOCK_ENTITY) throw new IllegalStateException("UHHHHHHHHHHHHH");

        for (var block : blocks) CONTROLLER_BLOCKS.add(block);
    }

    //--

    @Override
    public boolean interactWithFluidStorage(ForgeControllerBlockEntity controller, PlayerEntity player, Hand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, null, controller.<FluidHolderImpl>getFluidHolder());
    }

    @Override
    public FluidStorage createStorage(ForgeControllerBlockEntity controller) {
        return new FluidHolderImpl(controller::markDirty);
    }

    //--

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeEntry<T>> getAllMatches(ServerRecipeManager manager, RecipeType<T> type, I input, World world) {
        return manager.recipeMap().find(type, input, world);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeEntry<T>> getAllOfType(ServerRecipeManager manager, RecipeType<T> type) {
        return manager.recipeMap().getAll(type);
    }

    @Override
    public Ingredient createStackIngredient(ItemStack stack) {
        var builder = ComponentMap.builder();

        for (var entry : stack.getComponentChanges().entrySet()) {
            addUnsafe(builder, entry.getKey(), entry.getValue());
        }

        return DataComponentIngredient.of(false, builder.build(), stack.getRegistryEntry());
    }

    private static <T> void addUnsafe(ComponentMap.Builder builder, ComponentType<T> type, Optional<?> data) {
        builder.add(type, (T) data.orElse(null));
    }

    //--

    private static final Map<ResourceType, Map<Identifier, EndecDataLoader<?>>> LOADER_MAP = new HashMap<>();

    @Override
    public void registerLoader(Identifier id, ResourceType packType, EndecDataLoader<?> loader, boolean requiresRegistries) {
        var map = LOADER_MAP.computeIfAbsent(packType, resourceType -> new LinkedHashMap<>());

        if (map.containsKey(id)) {
            throw new IllegalStateException("Unable to register Data Loader as the given identifier has already been registered: " + id);
        }

        map.put(id, loader);
    }

    public static void registerEndecDataLoaders(ReloadListenerRegistration registration) {
        LOADER_MAP.getOrDefault(registration.getType(), Map.of()).forEach((identifier, loader) -> {
            if (loader.requiresRegistries()) {
                loader.setRegistryGetter(store -> registration.getRegistry());
            }

            registration
                .addListener(identifier, loader)
                .addDependency(identifier, loader.getDependencyIds());
        });
    }

    public interface ReloadListenerRegistration {
        ResourceType getType();

        default DynamicRegistryManager getRegistry() {
            throw new IllegalStateException("Unable to get DynamicRegistryManager on the Client!");
        }

        ReloadListenerRegistration addListener(Identifier id, ResourceReloader listener);

        ReloadListenerRegistration addDependency(Identifier id, Collection<Identifier> dependencies);
    }

    //--

    @Override
    public OptionalInt openHandledScreen(PlayerEntity player, ForgeControllerBlockEntity blockEntity, @Nullable NamedScreenHandlerFactory factory) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            return serverPlayer.openMenu(factory, registryByteBuf -> {
                BlockEntityLocation.ENDEC.encodeFully(() -> ByteBufSerializer.of(registryByteBuf), blockEntity.getExtraScreenData(serverPlayer));
            });
        }

        return GeneralPlatformUtils.super.openHandledScreen(player, blockEntity, factory);
    }


    //--

    @Override
    public ResourceConditionHolder createConditionsHolder() {
        return new NeoforgeResourceConditionHolder(new ArrayList<>());
    }

    //--

    @Override
    public void handleDefinitionEntry(ForgeRegistry.EntryHolder holder) {
        if (isFrozen) {
            REGISTERED_ENTRIES.put(holder.controllerId, holder);
        } else {
            holder.registerBlock();
            holder.registerItem();
        }
    }


    private static boolean isFrozen = true;
    private static final Map<Identifier, ForgeRegistry.EntryHolder> REGISTERED_ENTRIES = new LinkedHashMap<>();

    public static void handleLoadedEntries() {
        if (!REGISTERED_ENTRIES.isEmpty()) {
            for (var value : REGISTERED_ENTRIES.values()) {
                value.registerBlock();
                value.registerItem();
            }

            REGISTERED_ENTRIES.clear();
        }

        isFrozen = false;
    }

    //--
}
