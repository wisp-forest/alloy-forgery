package wraith.alloy_forgery.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import wraith.alloy_forgery.AlloyForgery;
import wraith.alloy_forgery.api.ForgeFuels;
import wraith.alloy_forgery.api.ForgeRecipes;
import wraith.alloy_forgery.api.Forges;
import wraith.alloy_forgery.api.MaterialWorths;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Config {

    private static boolean IS_LOADED = false;

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static void init() {
        IS_LOADED = true;

        String defaultConfig = "{\n" +
                               "  \"replace_configs_with_default\": true\n" +
                               "}";

        createFile("config/alloy_forgery/config.json", defaultConfig, false);
        boolean replace = getJsonObject(readFile(new File("config/alloy_forgery/config.json"))).get("replace_configs_with_default").getAsBoolean();

        Utils.saveFilesFromJar("configs/", "", replace);

        loadConfigs();
    }

    private static void loadConfigs() {
        ForgeFuels.readFromJson(Config.getJsonObject(Config.readFile(new File("config/alloy_forgery/fuels.json"))));
        Forges.readFromJson(Config.getJsonObject(Config.readFile(new File("config/alloy_forgery/smelteries.json"))));
        MaterialWorths.readFromJson(Config.getJsonObject(Config.readFile(new File("config/alloy_forgery/material_worth.json"))));
        ForgeRecipes.readFromJson(Config.getJsonObject(Config.readFile(new File("config/alloy_forgery/recipes.json"))));
    }

    public static void createFile(String path, String contents, boolean overwrite) {
        File file = new File(path);
        if (file.exists() && !overwrite) {
            return;
        }
        file.getParentFile().mkdirs();
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file.setReadable(true);
        file.setWritable(true);
        file.setExecutable(true);
        if (contents == null || "".equals(contents)) {
            return;
        }
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(File file) {
        String output = "";
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("\\Z");
            output = scanner.next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static JsonObject getJsonObject(String json) {
        try {
            return new JsonParser().parse(json).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
            AlloyForgery.LOGGER.error("Error while parsing following json:\n\n" + json);
            return null;
        }
    }

    public static File[] getFiles(String path) {
        return new File(path).listFiles();
    }

}
