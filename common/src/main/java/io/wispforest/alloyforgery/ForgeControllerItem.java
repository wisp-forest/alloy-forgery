package io.wispforest.alloyforgery;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import io.wispforest.alloyforgery.block.ForgeControllerBlock;
import io.wispforest.alloyforgery.forges.ForgeTierDataLoader;
import java.util.List;
import java.util.function.Consumer;

public class ForgeControllerItem extends BlockItem {

    public ForgeControllerItem(ForgeControllerBlock block, Properties settings) {
        super(block, settings);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
        var tier = ForgeTierDataLoader.getForgeRegistry(true).getBoundForgeTier(getForgeDefinition());

        if (tier != null) tier.tooltip(true, textConsumer);
    }

    public Identifier getForgeDefinition() {
        return ((ForgeControllerBlock) getBlock()).forgeDefinitionId;
    }
}
