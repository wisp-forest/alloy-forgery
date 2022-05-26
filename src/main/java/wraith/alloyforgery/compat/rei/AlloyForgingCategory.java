package wraith.alloyforgery.compat.rei;

// TODO re-enable rei plugin
public class AlloyForgingCategory /*implements DisplayCategory<AlloyForgingDisplay>*/ {
//
//    final Identifier GUI_TEXTURE = AlloyForgery.id("textures/gui/forge_controller.png");
//
//    public static final CategoryIdentifier<AlloyForgingDisplay> ID = CategoryIdentifier.of(AlloyForgery.id("forging"));
//
//    @Override
//    public int getDisplayHeight() {
//        return 88;
//    }
//
//    @Override
//    public Renderer getIcon() {
//        return ForgeRegistry.getControllerBlocks().isEmpty() ? EntryStack.empty() : EntryStacks.of(ForgeRegistry.getControllerBlocks().get(0));
//    }
//
//    @Override
//    public Text getTitle() {
//        return Text.translatable("container.alloy_forgery.rei.title");
//    }
//
//    @Override
//    public List<Widget> setupDisplay(AlloyForgingDisplay display, Rectangle bounds) {
//        Point origin = bounds.getLocation();
//
//        final var widgets = new ArrayList<Widget>();
//
//        widgets.add(Widgets.createRecipeBase(bounds));
//
//        widgets.add(Widgets.createTexturedWidget(GUI_TEXTURE, origin.x + 10, origin.y + 18, 42, 21, 124, 58));
//        widgets.add(Widgets.createTexturedWidget(GUI_TEXTURE, origin.x + 115, origin.y + 21, 176, 0, 15, 19));
//
//        for (int i = 0; i < display.getInputEntries().size(); i++) {
//            final var slotLocation = new Point(origin.x + 12 + i % 5 * 18, origin.y + 40 + (i > 4 ? 1 : 0) * 18);
//            widgets.add(Widgets.createSlot(slotLocation).entries(display.getInputEntries().get(i)).markInput().disableBackground());
//            widgets.add(Widgets.createTexturedWidget(GUI_TEXTURE, slotLocation.x - 1, slotLocation.y - 1, 208, 0, 18, 18));
//        }
//
//        final var resultSlot = Widgets.createSlot(new Point(origin.x + 113, origin.y + 47));
//        widgets.add(resultSlot.entries(display.getOutputEntries().get(0)).disableBackground().markOutput());
//
//        final var tierLabel = Widgets.createLabel(new Point(origin.x + 12, origin.y + 11), Text.translatable("container.alloy_forgery.rei.min_tier", display.minForgeTier));
//        widgets.add(tierLabel.leftAligned().color(0x3f3f3f).noShadow());
//        widgets.add(Widgets.createLabel(new Point(origin.x + 12, origin.y + 24), Text.translatable("container.alloy_forgery.rei.fuel_per_tick", display.requiredFuel)).leftAligned().color(0x3f3f3f).noShadow());
//
//        final MutableInt overrideIndex = new MutableInt(1);
//        final List<AlloyForgeRecipe.OverrideRange> overrides = new ArrayList<>(display.overrides.keySet());
//
//        widgets.add(Widgets.createButton(new Rectangle(origin.x + 131, origin.y + 6, 12, 12), Text.of("...")).onClick(button -> {
//            int index = overrideIndex.intValue();
//            tierLabel.setMessage(Text.translatable("container.alloy_forgery.rei.min_tier", index == 0 ? display.minForgeTier : overrides.get(index - 1)));
//
//            resultSlot.clearEntries();
//            resultSlot.entries(index == 0 ? display.getOutputEntries().get(0) : EntryIngredients.of(display.overrides.get(overrides.get(index - 1))));
//
//            overrideIndex.increment();
//            if (overrideIndex.intValue() - 1 > overrides.size() - 1) overrideIndex.setValue(0);
//        }).tooltipLine(Text.of("Cycle Tier Overrides")).enabled(overrides.size() != 0));
//
//        return widgets;
//    }
//
//    @Override
//    public CategoryIdentifier<? extends AlloyForgingDisplay> getCategoryIdentifier() {
//        return ID;
//    }
}
