package io.wispforest.alloyforgery.utils;

import io.wispforest.alloyforgery.block.ForgeControllerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

///
/// A simple abstraction used to allow for multiplatform fluid handling within [ForgeControllerBlockEntity]
///
public interface FluidStorage {

    void readData(ValueInput view);

    void writeData(ValueOutput view);

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
