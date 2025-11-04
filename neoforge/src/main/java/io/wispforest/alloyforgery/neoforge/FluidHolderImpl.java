package io.wispforest.alloyforgery.neoforge;

import io.wispforest.alloyforgery.utils.FluidStorage;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class FluidHolderImpl implements IFluidHandler, IFluidTank, FluidStorage {

    protected FluidStack fluid = new FluidStack(Fluids.LAVA, 0);
    protected long amountInDroplets = 0;

    private final Runnable onCommitAction;
    private final long capacity = 81000 + 81;

    public FluidHolderImpl(Runnable onCommitAction) {
        this.onCommitAction = onCommitAction;
    }

    private void setAmountOnStack() {
        fluid.setAmount(getFluidAmount());
    }

    @Override
    public FluidStack getFluid() {
        setAmountOnStack();

        return fluid;
    }

    //--

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookupProvider) {
        this.amountInDroplets = nbt.getLong("Amount");

        var amount = this.fluid.getAmount();
        var variant = Registries.FLUID.getEntry(Identifier.of(nbt.getString("Variant"))).orElseThrow();

        this.fluid = new FluidStack(variant, amount);
    }

    @Override
    public void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookupProvider) {
        nbt.putLong("Amount", this.amountInDroplets);
        nbt.putString("Variant", fluid.getFluidHolder().getIdAsString());
    }

    @Override
    public float fullnessAmount() {
        return amountInDroplets / 81000f;
    }

    @Override
    public long getFluidAmountInDroplets() {
        return this.amountInDroplets;
    }

    @Override
    public long setFluidAmountInDroplets(long amount) {
        var cappedAmount = Math.min(amount, (capacity * 81) + 80);
        var spilledAmount = amount - cappedAmount;

        this.amountInDroplets = cappedAmount;

        setAmountOnStack();

        return spilledAmount;
    }

    //--

    @Override
    public int getFluidAmount() {
        return (int) Math.ceil(getFluidAmountInDroplets() / 81.0);
    }

    @Override
    public int getCapacity() {
        return Math.toIntExact(capacity / 81);
    }

    @Override
    public boolean isFluidValid(FluidStack fluidStack) {
        return fluidStack.is(Fluids.LAVA);
    }

    private boolean isEmpty() {
        return this.fluid == FluidStack.EMPTY
            || this.fluid.getFluid() == Fluids.EMPTY
            || this.amountInDroplets <= 0;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        // If we try to fill with an empty resource or is not valid, return early
        if (resource.isEmpty() || !isFluidValid(resource)) return 0;

        var capacity = getCapacity();

        var resourceAmountInDroplets = resource.getAmount() * 81L;

        if (action.simulate()) {
            if (this.isEmpty()) return Math.min(capacity, resource.getAmount());
            if(!FluidStack.isSameFluidSameComponents(fluid, resource)) return 0;

            return Math.min(capacity - getFluidAmount(), resource.getAmount());
        }

        if (this.isEmpty()) {
            this.amountInDroplets = Math.min(this.capacity, resourceAmountInDroplets);

            this.fluid = resource.copyAndClear();
            setAmountOnStack();

            this.onCommitAction.run();

            return (int) Math.ceil((this.amountInDroplets / 81.0));
        } else if (!FluidStack.isSameFluidSameComponents(fluid, resource)) {
            return 0;
        }

        var filled = this.capacity - this.amountInDroplets;

        int filledMilliBuckets;

        if (resourceAmountInDroplets < filled) {
            this.amountInDroplets += resourceAmountInDroplets;
            filledMilliBuckets = resource.getAmount();
        } else {
            this.amountInDroplets = this.capacity + (filled % 81);
            filledMilliBuckets = getCapacity();
        }

        this.setAmountOnStack();

        if (filled > 0) this.onCommitAction.run();

        return filledMilliBuckets;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.EMPTY;
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
