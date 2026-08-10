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
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class NeoforgeGeneralPlatformUtils implements GeneralPlatformUtils {
    @Override
    public <T extends AbstractContainerMenu, D> MenuType<T> createScreenHandlerType(ExtendedFactory<T, D> factory, StreamCodec<? super RegistryFriendlyByteBuf, D> packetCodec) {
        return new MenuType<>(
            (IContainerFactory<T>) (i, inv, buf) -> factory.create(i, inv, packetCodec.decode(buf)),
            FeatureFlags.VANILLA_SET
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
    public boolean interactWithFluidStorage(ForgeControllerBlockEntity controller, Player player, InteractionHand hand) {
        return FluidUtil.interactWithFluidHandler(player, hand, null, controller.<FluidHolderImpl>getFluidHolder());
    }

    @Override
    public FluidStorage createStorage(ForgeControllerBlockEntity controller) {
        return new FluidHolderImpl(controller::setChanged);
    }

    //--

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeManager manager, RecipeType<T> type, I input, Level world) {
        return manager.recipeMap().getRecipesFor(type, input, world);
    }

    @Override
    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeManager manager, RecipeType<T> type) {
        return manager.recipeMap().byType(type);
    }

    @Override
    public Ingredient createStackIngredient(ItemStack stack) {
        var builder = DataComponentMap.builder();

        for (var entry : stack.getComponentsPatch().entrySet()) {
            addUnsafe(builder, entry.getKey(), entry.getValue());
        }

        return DataComponentIngredient.of(false, builder.build(), stack.getItemHolder());
    }

    private static <T> void addUnsafe(DataComponentMap.Builder builder, DataComponentType<T> type, Optional<?> data) {
        builder.set(type, (T) data.orElse(null));
    }

    //--

    private static final Map<PackType, Map<Identifier, EndecDataLoader<?>>> LOADER_MAP = new HashMap<>();

    @Override
    public void registerLoader(Identifier id, PackType packType, EndecDataLoader<?> loader, boolean requiresRegistries) {
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
        PackType getType();

        default RegistryAccess getRegistry() {
            throw new IllegalStateException("Unable to get DynamicRegistryManager on the Client!");
        }

        ReloadListenerRegistration addListener(Identifier id, PreparableReloadListener listener);

        ReloadListenerRegistration addDependency(Identifier id, Collection<Identifier> dependencies);
    }

    //--

    @Override
    public OptionalInt openHandledScreen(Player player, ForgeControllerBlockEntity blockEntity, @Nullable MenuProvider factory) {
        if (player instanceof ServerPlayer serverPlayer) {
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
