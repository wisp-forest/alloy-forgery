package io.wispforest.alloyforgery.neoforge;

import io.wispforest.alloyforgery.utils.FluidStorage;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class FluidHolderImpl implements IFluidHandler, IFluidTank, FluidStorage {

    protected FluidStack fluid = new FluidStack(Fluids.LAVA, 0);

    private final Runnable onCommitAction;
    private final int capacity = FluidType.BUCKET_VOLUME + 1;

    public FluidHolderImpl(Runnable onCommitAction) {
        this.onCommitAction = onCommitAction;
    }

    @Override
    public FluidStack getFluid() {
        return fluid;
    }

    //--

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookupProvider) {
        var amount = nbt.getLong("Amount");
        var variant = Registries.FLUID.getEntry(Identifier.of(nbt.getString("Variant"))).orElseThrow();

        fluid = new FluidStack(variant, (int) amount);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookupProvider) {
        nbt.putLong("Amount", fluid.getAmount());
        nbt.putString("Variant", fluid.getFluidHolder().getIdAsString());
    }

    @Override
    public float amountInBuckets() {
        return this.getFluidAmount() / (float) FluidType.BUCKET_VOLUME;
    }

    @Override
    public long getFluidAmountAsLong() {
        return getFluidAmount();
    }

    @Override
    public void setFluidAmount(long amount) {
        this.fluid.setAmount((int) amount);
    }

    //--

    @Override
    public int getFluidAmount() {
        return fluid.getAmount();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean isFluidValid(FluidStack fluidStack) {
        return fluidStack.is(Fluids.LAVA);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) return 0;
        if (action.simulate()) {
            if (fluid.isEmpty()) return Math.min(capacity, resource.getAmount());
            if (!FluidStack.isSameFluidSameComponents(fluid, resource)) return 0;

            return Math.min(capacity - fluid.getAmount(), resource.getAmount());
        }

        if (fluid.isEmpty()) {
            fluid = resource.copyWithAmount(Math.min(capacity, resource.getAmount()));
            onCommitAction.run();
            return fluid.getAmount();
        }

        if (!FluidStack.isSameFluidSameComponents(fluid, resource)) return 0;

        int filled = capacity - fluid.getAmount();

        if (resource.getAmount() < filled) {
            fluid.grow(resource.getAmount());
            filled = resource.getAmount();
        } else {
            fluid.setAmount(capacity);
        }
        if (filled > 0) onCommitAction.run();
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluid))
            ? FluidStack.EMPTY
            : drain(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        int drained = maxDrain;

        if (fluid.getAmount() < drained) drained = fluid.getAmount();

        var stack = fluid.copyWithAmount(drained);

        if (action.execute() && drained > 0) {
            fluid.shrink(drained);
            onCommitAction.run();
        }

        return stack;
    }

    //--

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int i) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int i) {
        return getCapacity();
    }

    @Override
    public boolean isFluidValid(int i, FluidStack fluidStack) {
        return isFluidValid(fluidStack);
    }
}
