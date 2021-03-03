package wraith.alloy_forgery.compat.rei;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import javafx.util.Pair;
import me.shedaniel.rei.api.ClientHelper;
import me.shedaniel.rei.api.EntryStack;
import me.shedaniel.rei.api.LiveRecipeGenerator;
import net.minecraft.recipe.Ingredient;
import net.minecraft.util.Identifier;
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

                        inputs.put(parsed.getKey(), parsed.getValue());
                    } else {
                        JsonObject toParse = new JsonObject();

                        if (s.startsWith("#")) {
                            toParse.addProperty("tag", s.substring(1));
                            inputs.put(Collections.singletonList(Ingredient.fromJson(toParse)), integer);
                        } else {
                            toParse.addProperty("item", s);
                            inputs.put(Collections.singletonList(Ingredient.fromJson(toParse)), integer);
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
            if (targetWorth % entry.getValue().worth != 0) continue;

            int count = targetWorth / entry.getValue().worth;
            JsonObject toParse = new JsonObject();

            if (entry.getKey().startsWith("#")) {
                toParse.addProperty("tag", entry.getKey().substring(1));
                amountMap.put(count, Ingredient.fromJson(toParse));
            } else {
                toParse.addProperty("item", entry.getKey());
                amountMap.put(count, Ingredient.fromJson(toParse));
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

        if (entry.isEmpty()) return Optional.of(new ArrayList<>());

        List<AlloyForgeDisplay> applicable = new ArrayList<>();
        recipes.forEach(alloyForgeDisplay -> {
            if (entry.equalsIgnoreAmount(alloyForgeDisplay.getResultingEntries().get(0).get(0))) applicable.add(alloyForgeDisplay);
        });

        return Optional.of(applicable);
    }

    @Override
    public Optional<List<AlloyForgeDisplay>> getUsageFor(EntryStack entry) {

        if (entry.isEmpty()) return Optional.of(new ArrayList<>());

        List<AlloyForgeDisplay> applicable = new ArrayList<>();
        recipes.forEach(alloyForgeDisplay -> {

            alloyForgeDisplay.getInputEntries().forEach(entryStacks -> entryStacks.forEach(entryStack -> {
                if (entry.equalsIgnoreAmount(entryStack) && !applicable.contains(alloyForgeDisplay)) {
                    applicable.add(alloyForgeDisplay);
                }
            }));

        });
        return Optional.of(applicable);
    }

    @Override
    public Optional<List<AlloyForgeDisplay>> getDisplaysGenerated(ClientHelper.ViewSearchBuilder builder) {

        List<AlloyForgeDisplay> applicable = new ArrayList<>();

        if (builder.getInputNotice() != null) applicable.addAll(getUsageFor(builder.getInputNotice()).get());
        if (builder.getOutputNotice() != null) applicable.addAll(getRecipeFor(builder.getOutputNotice()).get());

        if (builder.getCategories().contains(AlloyForgeryREIPlugin.ALLOY_FORGE_CATEGORY_ID)) applicable.addAll(recipes);

        return Optional.of(applicable);
    }
}
