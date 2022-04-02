package wraith.alloyforgery;

import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import wraith.alloyforgery.block.ForgeControllerBlock;
import wraith.alloyforgery.forges.ForgeRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlloyForgeryItemGroup extends OwoItemGroup {

    private List<ItemStack> controllerCache = null;

    protected AlloyForgeryItemGroup(Identifier id) {
        super(id);
    }

    @Override
    public ItemStack createIcon() {
        return this.controllerCache != null && this.controllerCache.isEmpty() ? Items.BRICKS.getDefaultStack() : this.controllerCache.get(0);
    }

    @Override
    public void appendStacks(DefaultedList<ItemStack> stacks) {
        if (controllerCache == null) this.createControllerCache();
        stacks.addAll(this.controllerCache);
    }

    private void createControllerCache() {
        final var blockList = new ArrayList<>(ForgeRegistry.getControllerBlocks());
        blockList.sort(Comparator.comparingInt(value -> ((ForgeControllerBlock) value).forgeDefinition.forgeTier()));

        this.controllerCache = new ArrayList<>(blockList.size());
        blockList.forEach(block -> this.controllerCache.add(block.asItem().getDefaultStack()));
    }

    @Override
    protected void setup() {
        this.addButton(ItemGroupButton.github("https://github.com/LordDeatHunter/Alloy-Forgery"));
        this.addButton(ItemGroupButton.curseforge("https://www.curseforge.com/minecraft/mc-mods/alloy-forgery"));
        this.addButton(ItemGroupButton.modrinth("https://modrinth.com/mod/alloy-forgery"));
        this.addButton(ItemGroupButton.discord("https://discord.gg/Pa5wDVm8Xv"));
    }
}
