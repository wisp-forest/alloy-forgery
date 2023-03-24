package wraith.alloyforgery;

import io.wispforest.owo.itemgroup.Icon;
import io.wispforest.owo.itemgroup.OwoItemGroup;
import io.wispforest.owo.itemgroup.gui.ItemGroupButton;
import io.wispforest.owo.itemgroup.gui.ItemGroupTab;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import wraith.alloyforgery.block.ForgeControllerBlock;
import wraith.alloyforgery.forges.ForgeRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlloyForgeryItemGroup {

    private static List<ItemStack> CONTROLLER_CACHE = null;

    public static final OwoItemGroup GROUP = OwoItemGroup.builder(AlloyForgery.id("alloy_forgery"), () -> {
        if (CONTROLLER_CACHE == null) return null;
        return Icon.of(CONTROLLER_CACHE.get(0));
    }).initializer(group -> {
        group.tabs.add(new ItemGroupTab(Icon.of(ItemStack.EMPTY), Text.empty(), (context, entries) -> {
            if (CONTROLLER_CACHE == null) createControllerCache();
            CONTROLLER_CACHE.forEach(entries::add);
        }, ItemGroupTab.DEFAULT_TEXTURE, true));

        group.addButton(ItemGroupButton.github(group, "https://github.com/LordDeatHunter/Alloy-Forgery"));
        group.addButton(ItemGroupButton.curseforge(group, "https://www.curseforge.com/minecraft/mc-mods/alloy-forgery"));
        group.addButton(ItemGroupButton.modrinth(group, "https://modrinth.com/mod/alloy-forgery"));
        group.addButton(ItemGroupButton.discord(group, "https://discord.gg/Pa5wDVm8Xv"));
    }).build();

    private static void createControllerCache() {
        final var blockList = new ArrayList<>(ForgeRegistry.getControllerBlocks());
        blockList.sort(Comparator.comparingInt(value -> ((ForgeControllerBlock) value).forgeDefinition.forgeTier()));

        CONTROLLER_CACHE = new ArrayList<>(blockList.size());
        blockList.forEach(block -> CONTROLLER_CACHE.add(block.asItem().getDefaultStack()));
    }

}
