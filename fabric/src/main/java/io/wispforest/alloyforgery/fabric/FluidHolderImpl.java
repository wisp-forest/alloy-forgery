package io.wispforest.alloyforgery.fabric;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import io.wispforest.alloyforgery.utils.EndecUtils;
import io.wispforest.alloyforgery.utils.FluidStorage;
import io.wispforest.endec.Endec;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.format.nbt.NbtDeserializer;
import io.wispforest.owo.serialization.format.nbt.NbtSerializer;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.Iterator;
import java.util.Objects;

public final class FluidHolderImpl extends SingleVariantStorage<FluidVariant> implements InsertionOnlyStorage<FluidVariant>, FluidStorage {
    public static final Endec<FluidVariant> FLUID_VARIANT = CodecUtils.toEndec(FluidVariant.CODEC).catchErrors((ctx, deserializer, e) -> FluidVariant.blank());

    private final Runnable onCommitAction;

    public FluidHolderImpl(Runnable onCommitAction) {
        this.onCommitAction = onCommitAction;
    }

    @Override
    protected FluidVariant getBlankVariant() {
        return FluidVariant.blank();
    }

    @Override
    protected long getCapacity(FluidVariant variant) {
        return FluidConstants.BUCKET + 81;
    }

    @Override
    protected void onFinalCommit() {
        onCommitAction.run();
    }

    @Override
    protected boolean canInsert(FluidVariant variant) {
        return variant.isOf(Fluids.LAVA);
    }

    @Override
    protected boolean canExtract(FluidVariant variant) {
        return false;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return InsertionOnlyStorage.super.iterator();
    }

    //--

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.amount = nbt.getLong("Amount");
        this.variant = FLUID_VARIANT.decodeFully(NbtDeserializer::of, nbt.getCompound("Variant"));
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putLong("Amount", this.amount);
        nbt.put("Variant", FLUID_VARIANT.encodeFully(NbtSerializer::of, this.variant));
    }

    @Override
    public float amountInBuckets() {
        return this.getAmount() / (float) FluidConstants.BUCKET;
    }

    @Override
    public long getFluidAmountAsLong() {
        return this.amount;
    }

    @Override
    public void setFluidAmount(long amount) {
        this.amount = amount;
    }
}
