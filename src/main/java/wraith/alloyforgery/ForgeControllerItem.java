package wraith.alloyforgery;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import wraith.alloyforgery.block.ForgeControllerBlock;
import wraith.alloyforgery.forges.ForgeDefinition;

import java.util.List;

public class ForgeControllerItem extends BlockItem {

    public ForgeControllerItem(ForgeControllerBlock block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(new TranslatableText("tooltip.alloy_forgery.forge_tier", getForgeDefinition().forgeTier()).formatted(Formatting.GRAY));
        tooltip.add(new TranslatableText("tooltip.alloy_forgery.fuel_capacity", getForgeDefinition().fuelCapacity()).formatted(Formatting.GRAY));
    }

    public ForgeDefinition getForgeDefinition() {
        return ((ForgeControllerBlock) getBlock()).forgeDefinition;
    }
}
