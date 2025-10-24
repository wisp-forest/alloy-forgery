package io.wispforest.alloyforgery.utils;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public interface FluidStorage {
    void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup);

    void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup);

    float amountInBuckets();

    long getFluidAmount();

    void setFluidAmount(long amount);
}
