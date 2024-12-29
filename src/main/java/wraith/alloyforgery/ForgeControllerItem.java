package wraith.alloyforgery;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import wraith.alloyforgery.block.ForgeControllerBlock;
import wraith.alloyforgery.forges.ForgeDefinition;
import wraith.alloyforgery.forges.ForgeTierRegistry;

import java.util.List;

public class ForgeControllerItem extends BlockItem {

    public ForgeControllerItem(ForgeControllerBlock block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        var tier = ForgeTierRegistry.getForgeRegistry(true).getForgeTier(getForgeDefinition());

        if (tier == null) return;

        tooltip.add(Text.translatable("tooltip.alloy_forgery.forge_tier", tier.value()).formatted(Formatting.GRAY));
        tooltip.add(Text.translatable("tooltip.alloy_forgery.fuel_capacity", tier.fuelCapacity()).formatted(Formatting.GRAY));
    }

    public ForgeDefinition getForgeDefinition() {
        return ((ForgeControllerBlock) getBlock()).forgeDefinition;
    }
}
