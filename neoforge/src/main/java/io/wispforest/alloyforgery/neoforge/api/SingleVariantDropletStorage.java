package io.wispforest.alloyforgery.neoforge.api;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class SingleVariantDropletStorage<T extends Resource> extends SnapshotJournal<LongResourceStack<T>> implements ResourceHandler<T> {

    public static final long DROPLETS_PER_MB = 81;

    public static final long BUCKET_VOLUME = DROPLETS_PER_MB * FluidType.BUCKET_VOLUME;

    public T variant = getBlankVariant();
    public long amount = 0;

    protected long getCapacity() {
        return getCapacity(this.variant);
    }

    /**
     * Return the blank variant.
     *
     * <p>Note: this is called very early in the constructor.
     * If fields need to be accessed from this function, make sure to re-initialize {@link #variant} yourself.
     */
    protected abstract T getBlankVariant();

    /**
     * Return the maximum capacity of this storage for the passed transfer variant.
     * If the passed variant is blank, an estimate should be returned.
     */
    protected abstract long getCapacity(T variant);

    /**
     * @return {@code true} if the passed non-blank variant can be inserted, {@code false} otherwise.
     */
    protected boolean canInsert(T variant) {
        return true;
    }

    /**
     * @return {@code true} if the passed non-blank variant can be extracted, {@code false} otherwise.
     */
    protected boolean canExtract(T variant) {
        return true;
    }

    protected void onFinalCommit() {}

    //--


    @Override
    protected void onRootCommit(LongResourceStack<T> originalState) {
        onFinalCommit();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean isValid(int i, T resource) {
        return canExtract(resource) && canInsert(resource);
    }

    //--

    private static void checkNonEmptyNonNegative(Resource resource, long value) {
        if (resource.isEmpty()) throw new IllegalArgumentException("Expected resource to be non-empty: " + resource);
        if (value < 0) throw new IllegalArgumentException("Expected value to be non-negative: " + value);
    }

    public long insertDroplets(T insertedVariant, long maxAmount, TransactionContext transaction) {
        checkNonEmptyNonNegative(insertedVariant, maxAmount);

        if ((insertedVariant.equals(variant) || variant.isEmpty()) && canInsert(insertedVariant)) {
            long insertedAmount = Math.min(maxAmount, getCapacity(insertedVariant) - amount);

            if (insertedAmount > 0) {
                long overflowAmount = 81 - (insertedAmount % 81);

                insertedAmount += overflowAmount;

                updateSnapshots(transaction);

                if (variant.isEmpty()) {
                    variant = insertedVariant;
                    amount = insertedAmount;
                } else {
                    amount += insertedAmount;
                }

                return insertedAmount;
            }
        }

        return 0;
    }

    @Override
    public int insert(int i, T insertedVariant, int maxAmount, TransactionContext transaction) {
        return (int) Math.ceil(insertDroplets(insertedVariant, maxAmount * 81L, transaction) / 81f);
    }

    public long extractDroplets(T extractedVariant, long maxAmount, TransactionContext transaction) {
        checkNonEmptyNonNegative(extractedVariant, maxAmount);

        if (extractedVariant.equals(variant) && canExtract(extractedVariant)) {
            long extractedAmount = Math.min(maxAmount, amount);

            if (extractedAmount > 0) {
                updateSnapshots(transaction);
                amount -= extractedAmount;

                if (amount == 0) {
                    variant = getBlankVariant();
                }

                return extractedAmount;
            }
        }

        return 0;
    }

    @Override
    public int extract(int i, T extractedVariant, int maxAmount, TransactionContext transaction) {
        return (int) Math.floor(extractDroplets(extractedVariant, maxAmount * 81L, transaction) / 81f);
    }

    public boolean isResourceBlank() {
        return variant.isEmpty();
    }

    @Override
    public T getResource(int i) {
        return variant;
    }

    @Override
    public long getAmountAsLong(int i) {
        return (long) Math.ceil(amount / 81d);
    }

    @Override
    public long getCapacityAsLong(int i, T resource) {
        return getCapacity(resource) / 81L;
    }

    @Override
    protected LongResourceStack<T> createSnapshot() {
        return new LongResourceStack<>(variant, amount);
    }

    @Override
    protected void revertToSnapshot(LongResourceStack<T> snapshot) {
        variant = snapshot.resource();
        amount = snapshot.amount();
    }

    @Override
    public String toString() {
        return "SingleVariantStorage[%d %s]".formatted(amount, variant);
    }
}
