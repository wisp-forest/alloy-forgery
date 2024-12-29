package wraith.alloyforgery.forges;

import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record ForgeTier(int value, float speedMultiplier, int fuelCapacity, int maxSmeltTime) {

    public static final ForgeTier DEFAULT = new ForgeTier(1, 1f, 48000, Optional.empty());

    public static final Endec<ForgeTier> ENDEC = StructEndecBuilder.of(
            Endec.INT.optionalFieldOf("tier", ForgeTier::value, 1),
            Endec.FLOAT.optionalFieldOf("speed_multiplier", ForgeTier::speedMultiplier, 1f),
            Endec.INT.optionalFieldOf("fuel_capacity", ForgeTier::fuelCapacity, 48000),
            Endec.INT.optionalOf().optionalFieldOf("max_smelt_time", forgeTier -> Optional.of(forgeTier.maxSmeltTime()), Optional.empty()),
            ForgeTier::new
    );

    public static final int BASE_MAX_SMELT_TIME = 200;

    public ForgeTier(int forgeTier, float speedMultiplier, int fuelCapacity, Optional<Integer> maxSmeltTime) {
        this(forgeTier, speedMultiplier, fuelCapacity, maxSmeltTime.orElse((int) (BASE_MAX_SMELT_TIME / speedMultiplier)));
    }

    public Identifier tierId(boolean isClientSide) {
        return ForgeTierRegistry.getForgeTierId(isClientSide, this);
    }
}
