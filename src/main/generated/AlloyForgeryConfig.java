package wraith.alloyforgery.compat;

import blue.endless.jankson.Jankson;
import io.wispforest.owo.config.ConfigWrapper;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.util.Observable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AlloyForgeryConfig extends ConfigWrapper<wraith.alloyforgery.compat.AlloyForgeryConfigModel> {

    public final Keys keys = new Keys();

    private final Option<java.lang.Boolean> strictRecipeChecks = this.optionForKey(this.keys.strictRecipeChecks);
    private final Option<java.lang.Boolean> allowHigherTierOutput = this.optionForKey(this.keys.allowHigherTierOutput);
    private final Option<java.lang.Boolean> allowBlastingFurnaceAdaption = this.optionForKey(this.keys.allowBlastingFurnaceAdaption);
    private final Option<java.lang.Integer> baseInputAmount = this.optionForKey(this.keys.baseInputAmount);
    private final Option<java.lang.Integer> higherTierOutputIncrease = this.optionForKey(this.keys.higherTierOutputIncrease);

    private AlloyForgeryConfig() {
        super(wraith.alloyforgery.compat.AlloyForgeryConfigModel.class);
    }

    private AlloyForgeryConfig(Consumer<Jankson.Builder> janksonBuilder) {
        super(wraith.alloyforgery.compat.AlloyForgeryConfigModel.class, janksonBuilder);
    }

    public static AlloyForgeryConfig createAndLoad() {
        var wrapper = new AlloyForgeryConfig();
        wrapper.load();
        return wrapper;
    }

    public static AlloyForgeryConfig createAndLoad(Consumer<Jankson.Builder> janksonBuilder) {
        var wrapper = new AlloyForgeryConfig(janksonBuilder);
        wrapper.load();
        return wrapper;
    }

    public boolean strictRecipeChecks() {
        return strictRecipeChecks.value();
    }

    public void strictRecipeChecks(boolean value) {
        strictRecipeChecks.set(value);
    }

    public boolean allowHigherTierOutput() {
        return allowHigherTierOutput.value();
    }

    public void allowHigherTierOutput(boolean value) {
        allowHigherTierOutput.set(value);
    }

    public boolean allowBlastingFurnaceAdaption() {
        return allowBlastingFurnaceAdaption.value();
    }

    public void allowBlastingFurnaceAdaption(boolean value) {
        allowBlastingFurnaceAdaption.set(value);
    }

    public int baseInputAmount() {
        return baseInputAmount.value();
    }

    public void baseInputAmount(int value) {
        baseInputAmount.set(value);
    }

    public int higherTierOutputIncrease() {
        return higherTierOutputIncrease.value();
    }

    public void higherTierOutputIncrease(int value) {
        higherTierOutputIncrease.set(value);
    }


    public static class Keys {
        public final Option.Key strictRecipeChecks = new Option.Key("strictRecipeChecks");
        public final Option.Key allowHigherTierOutput = new Option.Key("allowHigherTierOutput");
        public final Option.Key allowBlastingFurnaceAdaption = new Option.Key("allowBlastingFurnaceAdaption");
        public final Option.Key baseInputAmount = new Option.Key("baseInputAmount");
        public final Option.Key higherTierOutputIncrease = new Option.Key("higherTierOutputIncrease");
    }
}

