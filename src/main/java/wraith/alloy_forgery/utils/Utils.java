package wraith.alloy_forgery.utils;

import net.minecraft.util.Identifier;
import wraith.alloy_forgery.AlloyForgery;

import static wraith.alloy_forgery.AlloyForgery.MOD_ID;

public class Utils {
    public static Identifier ID(String id) {
        return new Identifier(MOD_ID, id);
    }

    // Dynamic generators for Alloy Forger blocks
    public static String createBlockItemModelJson(String id) {
        return "{\n" +
                "  \"parent\": \"alloy_forgery:block/" + id + "\"\n" +
                "}";
    }

    public static String createBlockModelJson(String id, boolean on) {
        return "{\n" +
                "  \"parent\": \"minecraft:block/orientable\",\n" +
                "  \"textures\": {\n" +
                "    \"top\": \"alloy_forgery:block/" + id + "_side\",\n" +
                "    \"front\": \"alloy_forgery:block/" + id + "_front" + (on ? "_on" : "") + "\",\n" +
                "    \"side\": \"alloy_forgery:block/" + id + "_side\"\n" +
                "  }\n" +
                "}";
    }

    public static String createBlockStateJson(String id) {


    }
}
