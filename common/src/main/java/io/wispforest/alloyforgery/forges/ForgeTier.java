package io.wispforest.alloyforgery.forges;

import io.wispforest.alloyforgery.AlloyForgery;
import io.wispforest.endec.Endec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.function.Consumer;

public final class ForgeTier {

    public static final Identifier DEFAULT_ID = AlloyForgery.id("default");
    public static final ForgeTier DEFAULT = new ForgeTier(DEFAULT_ID, 1, 1f, 48000);

    public static final Endec<ForgeTier> ENDEC = StructEndecBuilder.of(
        MinecraftEndecs.IDENTIFIER.fieldOf("id", ForgeTier::id),
        Endec.INT.fieldOf("tier", ForgeTier::value),
        Endec.FLOAT.fieldOf("speed_multiplier", ForgeTier::speedMultiplier),
        Endec.FLOAT.fieldOf("fuel_consumption_multiplier", ForgeTier::fuelConsumptionMultiplier),
        Endec.INT.fieldOf("fuel_capacity", ForgeTier::fuelCapacity),
        ForgeTier::new
    );

    public static final int BASE_MAX_SMELT_TIME = 200;

    private final Identifier id;
    private final int value;
    private final float speedMultiplier;
    private final float fuelConsumptionMultiplier;
    private final int fuelCapacity;

    public ForgeTier(Identifier id, int value, float speedMultiplier, float fuelConsumptionMultiplier, int fuelCapacity) {
        this.id = id;
        this.value = value;
        this.speedMultiplier = speedMultiplier;
        this.fuelConsumptionMultiplier = fuelConsumptionMultiplier;
        this.fuelCapacity = fuelCapacity;
    }

    public ForgeTier(Identifier id, int value, float speedMultiplier, int fuelCapacity) {
        this(id, value, speedMultiplier, speedMultiplier, fuelCapacity);
    }

    @Deprecated(forRemoval = true)
    public ForgeTier(Identifier id, int forgeTier, float speedMultiplier, int fuelCapacity, Optional<Integer> maxSmeltTime) {
        this(id, forgeTier, maxSmeltTime.map(integer -> integer / (float) BASE_MAX_SMELT_TIME).orElse(speedMultiplier), speedMultiplier, fuelCapacity);
    }

    public Identifier id() {
        return id;
    }

    public int value() {
        return value;
    }

    public float speedMultiplier() {
        return speedMultiplier;
    }

    public float fuelConsumptionMultiplier() {
        return fuelConsumptionMultiplier;
    }

    public int fuelCapacity() {
        return fuelCapacity;
    }

    public int maxSmeltTime() {
        return (int) (BASE_MAX_SMELT_TIME / speedMultiplier);
    }

    public Text name(boolean advancedName) {
        return Text.translatable(id.toTranslationKey(AlloyForgery.translationKey("tier." + (advancedName ? "advanced_" : "") + "name")), value);
    }

    public static Text toName(boolean isClientSide, int value) {
        var tier = ForgeTierDataLoader.getForgeRegistry(isClientSide).getPrimaryTier(value);

        return  tier != null ? tier.name(true) : Text.literal(String.valueOf(value));
    }

    public void tooltip(boolean advancedName, Consumer<Text> tooltipCallback) {
        tooltipCallback.accept(AlloyForgery.tooltipTranslation("forge_tier", this.name(advancedName)).formatted(Formatting.GRAY));
        tooltipCallback.accept(AlloyForgery.tooltipTranslation("speed_multiplier", this.speedMultiplier()).formatted(Formatting.GRAY));
        tooltipCallback.accept(AlloyForgery.tooltipTranslation("fuel_consumption_multiplier", this.fuelConsumptionMultiplier()).formatted(Formatting.GRAY));
        tooltipCallback.accept(AlloyForgery.tooltipTranslation("fuel_capacity", this.fuelCapacity()).formatted(Formatting.GRAY));
    }

    @Override
    public String toString() {
        return "ForgeTier[" +
            "id: " + id + ", " +
            "value: " + value + ", " +
            "speedMultiplier: " + speedMultiplier + ", " +
            "fuelConsumptionMultiplier: " + fuelConsumptionMultiplier + ", " +
            "fuelCapacity: " + fuelCapacity +
            ']';
    }
}
