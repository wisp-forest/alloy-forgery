package wraith.alloy_forgery.utils;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.FileUtils;
import wraith.alloy_forgery.AlloyForgery;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Utils {
    public static Identifier ID(String id) {
        return new Identifier(AlloyForgery.MOD_ID, id);
    }

    // Dynamic generators for Alloy Forger blocks
    public static String createBlockItemModelJson(String id) {
        return "{\n" +
                "  \"parent\": \"alloy_forgery:block/" + id + "\"\n" +
                "}";
    }

    public static String createBlockModelJson(String id) {
        String isOn = "";
        if (id.endsWith("_on")) {
            id = id.substring(0, id.length() - 3);
            isOn = "_on";
        }
        return "{\n" +
                "  \"parent\": \"minecraft:block/orientable\",\n" +
                "  \"textures\": {\n" +
                "    \"top\": \"alloy_forgery:block/" + id + "_side\",\n" +
                "    \"front\": \"alloy_forgery:block/" + id + "_front" + isOn + "\",\n" +
                "    \"side\": \"alloy_forgery:block/" + id + "_side\"\n" +
                "  }\n" +
                "}";
    }

    public static String createBlockStateJson(String id) {
        return "{\n" +
                "  \"variants\": {\n" +
                "    \"facing=east,lit=false\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "\",\n" +
                "      \"y\": 90\n" +
                "    },\n" +
                "    \"facing=east,lit=true\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "_on\",\n" +
                "      \"y\": 90\n" +
                "    },\n" +
                "    \"facing=north,lit=false\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "\"\n" +
                "    },\n" +
                "    \"facing=north,lit=true\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "_on\"\n" +
                "    },\n" +
                "    \"facing=south,lit=false\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "\",\n" +
                "      \"y\": 180\n" +
                "    },\n" +
                "    \"facing=south,lit=true\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "_on\",\n" +
                "      \"y\": 180\n" +
                "    },\n" +
                "    \"facing=west,lit=false\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "\",\n" +
                "      \"y\": 270\n" +
                "    },\n" +
                "    \"facing=west,lit=true\": {\n" +
                "      \"model\": \"alloy_forgery:block/" + id + "_on\",\n" +
                "      \"y\": 270\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
    }


    public static void saveFilesFromJar(String dir, String outputDir, boolean overwrite) {
        JarFile jar = null;
        try {
            jar = new JarFile(Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (IOException | URISyntaxException ignored) {}

        if (jar != null) {
            Enumeration<JarEntry> entries = jar.entries();
            while(entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(dir) || (!entry.getName().endsWith(".json") && !entry.getName().endsWith(".png") && !entry.getName().endsWith(".mcmeta"))) {
                    continue;
                }
                String[] segments = entry.getName().split("/");
                String filename = segments[segments.length - 1];
                if (entry.isDirectory()) {
                    continue;
                }
                InputStream is = Utils.class.getResourceAsStream("/" + entry.getName());
                String path = "config/alloy_forgery/" + outputDir + ("".equals(outputDir) ? "" : File.separator) + filename;
                if (filename.endsWith(".png")) {
                    if (Files.exists(new File(path).toPath()) && overwrite) {
                        try {
                            Files.delete(new File(path).toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!Files.exists(new File(path).toPath())) {
                        try {
                            new File(path).getParentFile().mkdirs();
                            Files.copy(is, new File(path).toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        inputStreamToFile(is, new File(path), overwrite);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("Launched from IDE.");
            File[] files = FabricLoader.getInstance().getModContainer(AlloyForgery.MOD_ID).get().getPath(dir).toFile().listFiles();
            for(File file : files) {
                if (file.isDirectory()) {
                    continue;
                }
                String[] segments = file.getName().split("/");
                String filename = segments[segments.length - 1];
                String path = "config/alloy_forgery/" + outputDir + ("".equals(outputDir) ? "" : File.separator) + filename;
                if (filename.endsWith(".png")) {
                    if (Files.exists(new File(path).toPath()) && overwrite) {
                        try {
                            Files.delete(new File(path).toPath());
                        } catch (IOException e) {
                            AlloyForgery.LOGGER.warn("ERROR OCCURRED WHILE DELETING OLD TEXTURES FOR " + filename);
                            e.printStackTrace();
                        }
                    }
                    if (!Files.exists(new File(path).toPath())) {
                        try {
                            AlloyForgery.LOGGER.info("Regenerating " + filename);
                            new File(path).getParentFile().mkdirs();
                            Files.copy(file.toPath(), new File(path).toPath());
                        } catch (IOException e) {
                            AlloyForgery.LOGGER.warn("ERROR OCCURRED WHILE REGENERATING " + filename + " TEXTURE");
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Config.createFile(path, FileUtils.readFileToString(file, StandardCharsets.UTF_8), overwrite);
                    } catch (IOException e) {
                        AlloyForgery.LOGGER.warn("ERROR OCCURRED WHILE REGENERATING " + filename + " TEXTURE");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void inputStreamToFile(InputStream inputStream, File file, boolean overwrite) throws IOException{
        if (!file.exists() || overwrite) {
            FileUtils.copyInputStreamToFile(inputStream, file);
        }
    }

    public static InputStream stringToInputStream(String s) {
        return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
    }

    public static final Random random = new Random(Calendar.getInstance().getTimeInMillis());
    public static int getRandomIntInRange(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }
}
