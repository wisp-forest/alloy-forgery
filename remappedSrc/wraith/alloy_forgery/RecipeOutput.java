package wraith.alloy_forgery;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RecipeOutput {

    public final int outputAmount;
    public final String outputItem;
    public final int heatAmount;
    public final float requiredTier;

    public RecipeOutput(String outputItem, int outputAmount, int heatAmount, float requiredTier) {
        this.outputAmount = outputAmount;
        this.outputItem = outputItem;
        this.heatAmount = heatAmount;
        this.requiredTier = requiredTier;
    }

    public Item getOutputAsItem() {
        Item outputItem;
        if (this.outputItem.startsWith("#")) {
            List<Item> tags = new ArrayList<>(TagRegistry.item(new Identifier(this.outputItem.substring(1))).values());
            tags.sort(Comparator.comparing(Registry.ITEM::getId));
            outputItem = tags.get(0);
        } else {
            outputItem = Registry.ITEM.get(new Identifier(this.outputItem));
        }
        return outputItem;
    }
}
