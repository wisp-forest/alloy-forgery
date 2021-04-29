package wraith.alloy_forgery.compat.rei;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonSyntaxException;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.LiveRecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.MaterialWorth;
import wraith.alloy_forgery.api.ForgeRecipes;
import wraith.alloy_forgery.api.MaterialWorths;

import java.util.*;
import java.util.stream.Collectors;

public class AlloyForgeRecipeGenerator implements LiveRecipeGenerator<AlloyForgeDisplay> {

    private static final List<AlloyForgeDisplay> recipes = new ArrayList<>();

    public AlloyForgeRecipeGenerator() {

        recipes.clear();

        ForgeRecipes.getRecipes().forEach(hashMapRecipeOutputEntry -> {

            try {
                HashMap<List<Ingredient>, Integer> inputs = new HashMap<>();
                hashMapRecipeOutputEntry.getKey().forEach((s, integer) -> {

                    if (!s.contains(":")) {
                        Pair<List<Ingredient>, Integer> parsed = getIngredientsForWorth(s, integer);

                        inputs.put(parsed.getLeft(), parsed.getRight());
                    } else {
                        if (s.startsWith("#")) {
                            Tag<Item> tag = ItemTags.getTagGroup().getTag(new Identifier(s.substring(1)));
                            if (tag == null) {
                                throw new JsonSyntaxException("Unknown item tag: " + s);
                            }
                            inputs.put(Collections.singletonList(Ingredient.fromTag(tag)), integer);
                        } else {
                            Item item = Registry.ITEM.getOrEmpty(new Identifier(s)).orElseThrow(() -> new JsonSyntaxException("Unknown item: " + s));
                            inputs.put(Collections.singletonList(Ingredient.ofItems(item)), integer);
                        }
                    }

                });

                recipes.add(new AlloyForgeDisplay(inputs, hashMapRecipeOutputEntry.getValue()));
            } catch (Exception e) {
                AlloyForgery.LOGGER.warn(e.getMessage());
            }

        });

    }

    public static Pair<List<Ingredient>, Integer> getIngredientsForWorth(String material, int targetWorth) {

        HashMap<String, MaterialWorth> worthMap = MaterialWorths.getMaterialWorthMap(material);
        Multimap<Integer, Ingredient> amountMap = HashMultimap.create();

        for (Map.Entry<String, MaterialWorth> entry : worthMap.entrySet()) {
            if (targetWorth % entry.getValue().worth != 0) {
                continue;
            }

            int count = targetWorth / entry.getValue().worth;

            if (entry.getKey().startsWith("#")) {
                Tag<Item> tag = ItemTags.getTagGroup().getTag(new Identifier(entry.getKey().substring(1)));
                if (tag == null) {
                    throw new JsonSyntaxException("Unknown item tag: " + entry.getKey());
                }
                amountMap.put(count, Ingredient.fromTag(tag));
            } else {
                Item item = Registry.ITEM.getOrEmpty(new Identifier(entry.getKey())).orElseThrow(() -> new JsonSyntaxException("Unknown item: " + entry.getKey()));
                amountMap.put(count, Ingredient.ofItems(item));
            }
        }

        int resultCount = amountMap.keySet().stream().sorted().collect(Collectors.toList()).get(0);
        List<Ingredient> resultList = new ArrayList<>(amountMap.get(resultCount));

        return new Pair<>(resultList, resultCount);
    }

    @Override
    public Identifier getCategoryIdentifier() {
        return AlloyForgeryREIPlugin.ALLOY_FORGE_CATEGORY_ID;
    }

    @Override
    public Optional<List<AlloyForgeDisplay>> getRecipeFor(EntryStack entry) {

        if (entry.isEmpty()) {
            return Optional.of(new ArrayList<>());
        }

        List<AlloyForgeDisplay> applicable = new ArrayList<>();
        recipes.forEach(alloyForgeDisplay -> {
            if (entry.equalsIgnoreAmount(alloyForgeDisplay.getResultingEntries().get(0).get(0))) {
                applicable.add(alloyForgeDisplay);
            }
        });

        return Optional.of(applicable);
    }

    @Override
    public Optional<List<AlloyForgeDisplay>> getUsageFor(EntryStack entry) {

        if (entry.isEmpty()) {
            return Optional.of(new ArrayList<>());
        }

        List<AlloyForgeDisplay> applicable = new ArrayList<>();
        recipes.forEach(alloyForgeDisplay -> alloyForgeDisplay.getInputEntries().forEach(entryStacks -> entryStacks.forEach(entryStack -> {
            if (entry.equalsIgnoreAmount(entryStack) && !applicable.contains(alloyForgeDisplay)) {
                applicable.add(alloyForgeDisplay);
            }
        })));
        return Optional.of(applicable);
    }

    @Override
    public Optional<List<AlloyForgeDisplay>> getDisplaysGenerated(ClientHelper.ViewSearchBuilder builder) {

        List<AlloyForgeDisplay> applicable = new ArrayList<>();

        if (builder.getInputNotice() != null) {
            applicable.addAll(getUsageFor(builder.getInputNotice()).get());
        }
        if (builder.getOutputNotice() != null) {
            applicable.addAll(getRecipeFor(builder.getOutputNotice()).get());
        }

        if (builder.getCategories().contains(AlloyForgeryREIPlugin.ALLOY_FORGE_CATEGORY_ID)) {
            applicable.addAll(recipes);
        }

        return Optional.of(applicable);
    }
}
