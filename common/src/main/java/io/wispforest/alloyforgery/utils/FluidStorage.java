package io.wispforest.alloyforgery.utils;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

///
/// A simple abstraction used to allow for multiplatform fluid handling within [ForgeControllerBlockEntity]
///
public interface FluidStorage {

    void readData(ReadView view);

    void writeData(WriteView view);

    ///
    /// Basically the tanks amount minus a single 1/1000 unit value for the given platforms bucket
    ///
    float fullnessAmount();

    ///
    /// @return the tanks current fluid amount in Fabrics Droplets
    ///
    long getFluidAmountInDroplets();

    ///
    /// Sets the amount of the take from the fluid amount in Fabrics droplets
    ///
    /// @return The amount not taken into the given storage in Fabric droplets
    ///
    long setFluidAmountInDroplets(long amount);
}
