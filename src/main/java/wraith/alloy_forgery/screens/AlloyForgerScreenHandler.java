package wraith.alloy_forgery.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.MaterialWorth;
import wraith.alloy_forgery.RecipeOutput;
import wraith.alloy_forgery.api.MaterialWorths;
import wraith.alloy_forgery.blocks.ForgeControllerBlockEntity;
import wraith.alloy_forgery.registry.ScreenHandlerRegistry;
import wraith.alloy_forgery.screens.slots.AlloyOutputSlot;
import wraith.alloy_forgery.screens.slots.LavaInputSlot;

import java.util.*;

public class AlloyForgerScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate delegate;
    public final PlayerEntity player;
    private final BlockPos frontPos;

    public AlloyForgerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(12), new ArrayPropertyDelegate(3), null);
    }

    public AlloyForgerScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, PropertyDelegate delegate, BlockPos pos) {
        super(ScreenHandlerRegistry.SCREEN_HANDLERS.get("alloy_forger"), syncId);
        this.frontPos = pos;
        checkDataCount(delegate, 3);
        this.delegate = delegate;
        this.player = playerInventory.player;
        this.inventory = inventory;
        this.addSlot(new LavaInputSlot(inventory, 0, 8, 58)); //Fuel Slot
        this.addSlot(new AlloyOutputSlot(inventory, 1, 145, 34)); //Alloy Output

        for (int y = 0; y < 2; ++y) {
            for (int x = 0; x < 5; ++x) {
                this.addSlot(new Slot(inventory,2 + y * 5 + x, 44 + x * 18, 27 + y * 18)); //Slot Generator, generates 5 slots, then moves a row down and makes 5 more
            }
        }

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(playerInventory, x + y * 9 + 9, 8 + x * 18, 91 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            this.addSlot(new Slot(playerInventory, x, 8 + x * 18, 149));
        }
        this.addProperties(delegate);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            } else if (originalStack.getItem() == Items.LAVA_BUCKET) {
                if (!this.insertItem(originalStack, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 2, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }

            if (originalStack.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTakeItem(player, originalStack);

        }
        return newStack;
    }

    @Environment(EnvType.CLIENT)
    public int getHeatProgress() {
        return (this.delegate.get(0));
    }

    @Environment(EnvType.CLIENT)
    public int getSmeltingProgress() {
        return 19 - (this.delegate.get(1) * 19 / (this.delegate.get(2) == 0 ? 200 : this.delegate.get(2)));
    }

    @Environment(EnvType.CLIENT)
    public boolean isHeating() {
        return this.delegate.get(0) > 0;
    }

    @Environment(EnvType.CLIENT)
    public boolean isSmelting() {
        return this.delegate.get(1) > 0;
    }

    public void updateItems(Map.Entry<HashMap<String, Integer>, RecipeOutput> oldRecipe) {
        if (player.world.isClient || !(inventory instanceof ForgeControllerBlockEntity)) {
            return;
        }
        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
        if (oldRecipe == null) {
            return;
        }
        HashMap<String, Integer> inputs = new HashMap<>(oldRecipe.getKey());
        HashMap<String, Integer> drops = new HashMap<>();
        for (int i = 2; i < this.inventory.size(); ++i) {
            if (this.inventory.getStack(i).isEmpty()) {
                continue;
            }
            String itemId = Registry.ITEM.getId(inventory.getStack(i).getItem()).toString();
            ItemStack currentStack = this.inventory.getStack(i);

            String material = null;
            String materialKey = null;
            ArrayList<String> materialWorth = null;
            String recipeMaterial = itemId;
            if (!inputs.containsKey(itemId)) {
                for (Map.Entry<String, HashMap<String, MaterialWorth>> materialWorths : MaterialWorths.getEntries()) {
                    boolean materialFound = false;
                    for (Map.Entry<String, MaterialWorth> itemWorths : materialWorths.getValue().entrySet()) {
                        if (itemWorths.getKey().equals(itemId) || itemWorths.getKey().startsWith("#") && TagRegistry.item(new Identifier(itemWorths.getKey().substring(1))).values().contains(currentStack.getItem())) {
                            material = materialWorths.getKey();
                            materialKey = itemWorths.getKey();
                            materialFound = true;
                            break;
                        }
                    }
                    if (materialFound) {
                        break;
                    }
                }

                materialWorth = new ArrayList<>();
                if (material != null) {
                    for (Map.Entry<String, MaterialWorth> values : MaterialWorths.getMaterialWorthMapEntries(material)) {
                        HashMap<String, MaterialWorth> entry = MaterialWorths.getMaterialWorthMap(material);
                        boolean added = false;
                        for (int j = 0; j < materialWorth.size(); ++j) {
                            if (entry.get(materialWorth.get(j)).worth >= values.getValue().worth) {
                                materialWorth.add(j, values.getKey());
                                added = true;
                                break;
                            }
                        }
                        if (!added) {
                            materialWorth.add(values.getKey());
                        }
                    }
                    Collections.reverse(materialWorth);
                }
                for (Map.Entry<String, Integer> recipeItem : inputs.entrySet()) {
                    if (recipeItem.getKey().startsWith("#") && TagRegistry.item(new Identifier(recipeItem.getKey().substring(1))).contains(currentStack.getItem())) {
                        recipeMaterial = recipeItem.getKey();
                        break;
                    }
                }
            }

            int worth = 1;
            int recipeAmount;
            // Checks the inputs in order if they are a material, recipeMaterial(item), or a materialKey(tag).
            // The order is tag, item, and material. If it finds a material it gets it value as well.
            if(inputs.containsKey(materialKey)) {
                recipeAmount = inputs.get(materialKey);
            } else if(inputs.containsKey(recipeMaterial)) {
                recipeAmount = inputs.get(recipeMaterial);
            } else {
                recipeAmount = inputs.get(material);
                worth = MaterialWorths.getMaterialWorthFromId(material, materialKey).worth;
            }

            int stackAmount = currentStack.getCount() * worth;
            if (recipeAmount > 0) {
                inputs.put(material == null ? recipeMaterial : material, recipeAmount - stackAmount);
                currentStack.decrement((int) Math.ceil((float)recipeAmount / worth));
                if (material != null) {
                    int leftOver = worth - recipeAmount;
                    HashMap<String, MaterialWorth> entry = MaterialWorths.getMaterialWorthMap(material);
                    while (leftOver > 0) {
                        boolean changed = false;
                        for (String matWorth : materialWorth) {
                            if (!entry.get(matWorth).canReturn) {
                                continue;
                            }
                            int currentWorth = entry.get(matWorth).worth;
                            if (currentWorth <= leftOver) {
                                int amount = (int) Math.floor((float) leftOver / currentWorth);
                                if (drops.containsKey(matWorth)) {
                                    drops.put(matWorth, drops.get(matWorth) + amount);
                                } else {
                                    drops.put(matWorth, amount);
                                }
                                leftOver %= currentWorth;
                                changed = true;
                            }
                        }
                        if (!changed) {
                            break;
                        }
                    }
                }
            }
            serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, i, currentStack));
        }
        for (Map.Entry<String, Integer> drop : drops.entrySet()) {
            if (drop.getValue() <= 0) {
                break;
            }
            Item item;
            if (drop.getKey().startsWith("#")) {
                List<Item> items = new ArrayList<>(TagRegistry.item(new Identifier(drop.getKey().substring(1))).values());
                if (items.size() <= 0) {
                    continue;
                }
                items.sort((Comparator.comparing(Registry.ITEM::getId)));
                item = items.get(0);
            } else {
                item = Registry.ITEM.get(new Identifier(drop.getKey()));
            }
            for (int i = 2; i < inventory.size(); ++i){
                if (drop.getValue() <= 0) {
                    break;
                }
                ItemStack stack = inventory.getStack(i);
                if (stack.getItem().equals(item)) {
                    int total = drop.getValue() + stack.getCount();
                    if (total <= stack.getMaxCount()) {
                        stack.setCount(total);
                        drop.setValue(0);
                    } else {
                        stack.setCount(stack.getMaxCount());
                        drop.setValue(drop.getValue() - stack.getMaxCount() + stack.getCount());
                    }
                    serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, i, stack));
                }
            }
            for (int i = 2; i < inventory.size(); ++i) {
                if (drop.getValue() <= 0) {
                    break;
                }
                ItemStack stack = inventory.getStack(i);
                if (stack.isEmpty()) {
                    int amount = item.getMaxCount();
                    if (drop.getValue() < amount) {
                        amount = drop.getValue();
                        drop.setValue(0);
                    } else {
                        drop.setValue(drop.getValue() - amount);
                    }
                    inventory.setStack(i, new ItemStack(item, amount));
                    serverPlayerEntity.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(syncId, i, stack));
                }
            }
        }
        for (Map.Entry<String, Integer> drop : drops.entrySet()) {
            if (drop.getValue() <= 0) {
                continue;
            }
            Item item;
            if (drop.getKey().startsWith("#")) {
                List<Item> items = new ArrayList<>(TagRegistry.item(new Identifier(drop.getKey().substring(1))).values());
                if (items.size() <= 0) {
                    continue;
                }
                items.sort((Comparator.comparing(Registry.ITEM::getId)));
                item = items.get(0);
            } else {
                item = Registry.ITEM.get(new Identifier(drop.getKey()));
            }
            while (drop.getValue() > 0) {
                int amount = item.getMaxCount();
                if (drop.getValue() < amount) {
                    amount = drop.getValue();
                    drop.setValue(0);
                } else {
                    drop.setValue(drop.getValue() - amount);
                }
                ItemStack stack = new ItemStack(item, amount);
                player.world.spawnEntity(new ItemEntity(player.world, frontPos.getX() + 0.5, frontPos.getY() + 0.5, frontPos.getZ() + 0.5, stack));
            }
        }
    }
}
