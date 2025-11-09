package io.wispforest.alloyforgery.neoforge;

import io.wispforest.alloyforgery.neoforge.api.SingleVariantDropletStorage;
import io.wispforest.alloyforgery.utils.FluidStorage;
import net.minecraft.fluid.Fluids;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class FluidHolderImpl extends SingleVariantDropletStorage<FluidResource> implements FluidStorage {
    private final Runnable onCommitAction;

    public FluidHolderImpl(Runnable onCommitAction) {
        this.onCommitAction = onCommitAction;
    }

    @Override
    protected FluidResource getBlankVariant() {
        return FluidResource.EMPTY;
    }

    @Override
    protected long getCapacity(FluidResource variant) {
        return SingleVariantDropletStorage.BUCKET_VOLUME + 81;
    }

    @Override
    protected void onFinalCommit() {
        onCommitAction.run();
    }

    @Override
    protected boolean canInsert(FluidResource variant) {
        return variant.is(Fluids.LAVA);
    }

    @Override
    protected boolean canExtract(FluidResource variant) {
        return false;
    }

    @Override
    public long extractDroplets(FluidResource extractedVariant, long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int i, FluidResource extractedVariant, int maxAmount, TransactionContext transaction) {
        return 0;
    }

    //--

    @Override
    public void readData(ReadView data) {
        this.amount = data.getLong("Amount", 0);
        this.variant = data.read("Variant", FluidResource.OPTIONAL_CODEC).orElse(FluidResource.EMPTY);
    }

    @Override
    public void writeData(WriteView data) {
        data.putLong("Amount", this.amount);
        data.put("Variant", FluidResource.OPTIONAL_CODEC, this.variant);
    }

    @Override
    public float fullnessAmount() {
        return this.amount / (float) SingleVariantDropletStorage.BUCKET_VOLUME;
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
