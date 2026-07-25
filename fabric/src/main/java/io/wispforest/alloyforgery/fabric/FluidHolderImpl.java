package io.wispforest.alloyforgery.fabric;

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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Iterator;

public final class FluidHolderImpl extends SingleVariantStorage<FluidVariant> implements InsertionOnlyStorage<FluidVariant>, FluidStorage {
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
    public void readData(ValueInput data) {
        this.amount = data.getLongOr("Amount", 0);
        this.variant = data.read("Variant", FluidVariant.CODEC).orElse(FluidVariant.blank());
    }

    @Override
    public void writeData(ValueOutput data) {
        data.putLong("Amount", this.amount);
        data.store("Variant", FluidVariant.CODEC, this.variant);
    }

    @Override
    public float fullnessAmount() {
        return this.getAmount() / (float) FluidConstants.BUCKET;
    }

    @Override
    public long getFluidAmountInDroplets() {
        return this.amount;
    }

    @Override
    public long setFluidAmountInDroplets(long amount) {
        var cappedAmount = Math.min(amount, this.getCapacity());
        var spilledAmount = amount - cappedAmount;

        this.amount = Math.min(amount, this.getCapacity());

        return spilledAmount <= 0 ? 0 : spilledAmount;
    }
}
