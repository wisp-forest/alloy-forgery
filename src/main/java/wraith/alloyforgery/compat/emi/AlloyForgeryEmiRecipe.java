package wraith.alloyforgery.compat.emi;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.AlloyForgery;
import wraith.alloyforgery.recipe.AlloyForgeRecipe;

import java.util.*;

public class AlloyForgeryEmiRecipe implements EmiRecipe {

    static final Identifier GUI_TEXTURE = AlloyForgery.id("textures/gui/forge_controller.png");

    private final List<EmiIngredient> inputs;
    private final EmiStack output;

    private final int minForgeTier;
    private final int requiredFuel;

    private final Map<AlloyForgeRecipe.OverrideRange, ItemStack> overrides;

    private final Identifier recipeID;

    public AlloyForgeryEmiRecipe(AlloyForgeRecipe recipe){
        //Convert inputs to a list of EMI Ingredients
        List<EmiIngredient> convertedInputs = new ArrayList<>();
        for (Map.Entry<Ingredient, Integer> entry : recipe.getIngredientsMap().entrySet()) {
            for (int i = entry.getValue(); i > 0; ) {
                int stackCount = Math.min(i, 64);

                convertedInputs.add(
                        EmiIngredient.of(Arrays.stream(entry.getKey().getMatchingStacks())
                                .map(ItemStack::copy)
                                .peek(stack -> stack.setCount(stackCount))
                                .map(EmiStack::of)
                                .toList()));

                i -= stackCount;
            }
        }

        this.inputs = convertedInputs;
        this.output = EmiStack.of(recipe.getBaseOutput());

        this.minForgeTier = recipe.getMinForgeTier();
        this.requiredFuel = recipe.getFuelPerTick();

        this.overrides = recipe.getTierOverrides();

        this.recipeID = recipe.secondaryID().orElse(recipe.getId());
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        //the min tier text
        var tierTextWidget = widgets.add(new CustomTextWidget(minTierText(minForgeTier), 8, 7, 0x404040, false));

        //the fuel required text
        widgets.addText(Text.translatable("container.alloy_forgery.rei.fuel_per_tick", requiredFuel).asOrderedText(), 8, 20, 0x404040, false);

        //the ten input slots background
        widgets.addTexture(GUI_TEXTURE, 6, 34, 92, 38, 42, 41);
        widgets.addTexture(GUI_TEXTURE, 107, 14, 10, 10, 208, 30);
        widgets.addTexture(GUI_TEXTURE, 111, 17, 16, 20, 176, 0);

        //the input slots themselves
        for (int i = 0; i < inputs.size(); i++) {
            int x = 7 + i % 5 * 18;
            int y = 35 + i / 5 * 18;
            widgets.addTexture(GUI_TEXTURE, x, y, 18, 18, 208, 0);
            widgets.addSlot(inputs.get(i), x, y).drawBack(false);
        }

        var outputWidget = widgets.add(new CustomSlotWidget(this.getOutputs().get(0), 104, 38, this));

        final MutableInt index = new MutableInt(0);
        final List<AlloyForgeRecipe.OverrideRange> overridesKeys = overrides.keySet().stream().toList();

        //add the tier cycling button
        widgets.add(new CustomButtonWidget(127, 2, () -> !overrides.isEmpty(), ((mouseX, mouseY, button) -> {
            if (index.incrementAndGet() > overrides.size()) index.setValue(0);

            Object tierArg;
            EmiStack currentStack;

            var currentIndex = index.getValue();

            if (currentIndex == 0) {
                tierArg = minForgeTier;
                currentStack = getOutputs().get(0);
            } else {
                AlloyForgeRecipe.OverrideRange range = overridesKeys.get(currentIndex - 1);

                tierArg = range;
                currentStack = EmiStack.of(overrides.get(range));
            }

            tierTextWidget.setText(minTierText(tierArg));
            outputWidget.setMutableStack(currentStack);
        })));
    }

    private static OrderedText minTierText(Object tierArg){
        return Text.translatable("container.alloy_forgery.rei.min_tier", tierArg).asOrderedText();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return AlloyForgeryEmiPlugin.FORGE_CATEGORY;
    }

    @Override
    public @Nullable Identifier getId() {
        return recipeID;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return Collections.singletonList(output);
    }

    @Override
    public int getDisplayWidth() {
        return 142;
    }

    @Override
    public int getDisplayHeight() {
        return 78;
    }
}
