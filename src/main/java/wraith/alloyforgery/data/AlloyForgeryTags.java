package wraith.alloyforgery.data;

import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import wraith.alloyforgery.AlloyForgery;

public class AlloyForgeryTags {

    private static final String COMMON_NAMESPACE = TagUtil.C_TAG_NAMESPACE;

    public static class Items {

        //Zinc
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_ZINC = common("storage_blocks/raw_zinc");
        public static final TagKey<Item> STORAGE_BLOCKS_ZINC = common("storage_blocks/zinc");
        public static final TagKey<Item> RAW_MATERIALS_ZINC = common("raw_materials/zinc");
        public static final TagKey<Item> ORES_ZINC = common("ores/zinc");
        public static final TagKey<Item> INGOTS_ZINC = common("ingots/zinc");


        //tungsten
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_TUNGSTEN = common("storage_blocks/raw_tungsten");
        public static final TagKey<Item> STORAGE_BLOCKS_TUNGSTEN = common("storage_blocks/tungsten");
        public static final TagKey<Item> RAW_MATERIALS_TUNGSTEN = common("raw_materials/tungsten");
        public static final TagKey<Item> ORES_TUNGSTEN = common("ores/tungsten");
        public static final TagKey<Item> INGOTS_TUNGSTEN = common("ingots/tungsten");

        //titanium
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_TITANIUM = common("storage_blocks/raw_titanium");
        public static final TagKey<Item> STORAGE_BLOCKS_TITANIUM = common("storage_blocks/titanium");
        public static final TagKey<Item> RAW_MATERIALS_TITANIUM = common("raw_materials/titanium");
        public static final TagKey<Item> ORES_TITANIUM = common("ores/titanium");
        public static final TagKey<Item> INGOTS_TITANIUM = common("ingots/titanium");

        //tin
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_TIN = common("storage_blocks/raw_tin");
        public static final TagKey<Item> STORAGE_BLOCKS_TIN = common("storage_blocks/tin");
        public static final TagKey<Item> RAW_MATERIALS_TIN = common("raw_materials/tin");
        public static final TagKey<Item> ORES_TIN = common("ores/tin");
        public static final TagKey<Item> INGOTS_TIN = common("ingots/tin");

        //silver
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_SILVER = common("storage_blocks/raw_silver");
        public static final TagKey<Item> STORAGE_BLOCKS_SILVER = common("storage_blocks/silver");
        public static final TagKey<Item> RAW_MATERIALS_SILVER = common("raw_materials/silver");
        public static final TagKey<Item> ORES_SILVER = common("ores/silver");
        public static final TagKey<Item> INGOTS_SILVER = common("ingots/silver");

        //platinum
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_PLATINUM = common("storage_blocks/raw_platinum");
        public static final TagKey<Item> STORAGE_BLOCKS_PLATINUM = common("storage_blocks/platinum");
        public static final TagKey<Item> RAW_MATERIALS_PLATINUM = common("raw_materials/platinum");
        public static final TagKey<Item> ORES_PLATINUM = common("ores/platinum");
        public static final TagKey<Item> INGOTS_PLATINUM = common("ingots/platinum");

        //palladium
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_PALLADIUM = common("storage_blocks/raw_palladium");
        public static final TagKey<Item> STORAGE_BLOCKS_PALLADIUM = common("storage_blocks/palladium");
        public static final TagKey<Item> RAW_MATERIALS_PALLADIUM = common("raw_materials/palladium");
        public static final TagKey<Item> ORES_PALLADIUM = common("ores/palladium");
        public static final TagKey<Item> INGOTS_PALLADIUM = common("ingots/palladium");

        //osmium
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_OSMIUM = common("storage_blocks/raw_osmium");
        public static final TagKey<Item> STORAGE_BLOCKS_OSMIUM = common("storage_blocks/osmium");
        public static final TagKey<Item> RAW_MATERIALS_OSMIUM = common("raw_materials/osmium");
        public static final TagKey<Item> ORES_OSMIUM = common("ores/osmium");
        public static final TagKey<Item> INGOTS_OSMIUM = common("ingots/osmium");

        //orichalcum
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_ORICHALCUM = common("storage_blocks/raw_orichalcum");
        public static final TagKey<Item> STORAGE_BLOCKS_ORICHALCUM = common("storage_blocks/orichalcum");
        public static final TagKey<Item> RAW_MATERIALS_ORICHALCUM = common("raw_materials/orichalcum");
        public static final TagKey<Item> ORES_ORICHALCUM = common("ores/orichalcum");
        public static final TagKey<Item> INGOTS_ORICHALCUM = common("ingots/orichalcum");

        //nickel
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_NICKEL = common("storage_blocks/raw_nickel");
        public static final TagKey<Item> STORAGE_BLOCKS_NICKEL = common("storage_blocks/nickel");
        public static final TagKey<Item> RAW_MATERIALS_NICKEL = common("raw_materials/nickel");
        public static final TagKey<Item> ORES_NICKEL = common("ores/nickel");
        public static final TagKey<Item> INGOTS_NICKEL = common("ingots/nickel");

        //mythril
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_MYTHRIL = common("storage_blocks/raw_mythril");
        public static final TagKey<Item> STORAGE_BLOCKS_MYTHRIL = common("storage_blocks/mythril");
        public static final TagKey<Item> RAW_MATERIALS_MYTHRIL = common("raw_materials/mythril");
        public static final TagKey<Item> ORES_MYTHRIL = common("ores/mythril");
        public static final TagKey<Item> INGOTS_MYTHRIL = common("ingots/mythril");

        //mythril
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_MANGANESE = common("storage_blocks/raw_manganese");
        public static final TagKey<Item> STORAGE_BLOCKS_MANGANESE = common("storage_blocks/manganese");
        public static final TagKey<Item> RAW_MATERIALS_MANGANESE = common("raw_materials/manganese");
        public static final TagKey<Item> ORES_MANGANESE = common("ores/manganese");
        public static final TagKey<Item> INGOTS_MANGANESE = common("ingots/manganese");

        //lead
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_LEAD = common("storage_blocks/raw_lead");
        public static final TagKey<Item> STORAGE_BLOCKS_LEAD = common("storage_blocks/lead");
        public static final TagKey<Item> RAW_MATERIALS_LEAD = common("raw_materials/lead");
        public static final TagKey<Item> ORES_LEAD = common("ores/lead");
        public static final TagKey<Item> INGOTS_LEAD = common("ingots/lead");

        //iridium
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRIDIUM = common("storage_blocks/raw_iridium");
        public static final TagKey<Item> STORAGE_BLOCKS_IRIDIUM = common("storage_blocks/iridium");
        public static final TagKey<Item> RAW_MATERIALS_IRIDIUM = common("raw_materials/iridium");
        public static final TagKey<Item> ORES_IRIDIUM = common("ores/iridium");
        public static final TagKey<Item> INGOTS_IRIDIUM = common("ingots/iridium");

        //antimony
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_ANTIMONY = common("storage_blocks/raw_antimony");
        public static final TagKey<Item> STORAGE_BLOCKS_ANTIMONY = common("storage_blocks/antimony");
        public static final TagKey<Item> RAW_MATERIALS_ANTIMONY = common("raw_materials/antimony");
        public static final TagKey<Item> ORES_ANTIMONY = common("ores/antimony");
        public static final TagKey<Item> INGOTS_ANTIMONY = common("ingots/antimony");

        //adamantite
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_ADAMANTITE = common("storage_blocks/raw_adamantite");
        public static final TagKey<Item> STORAGE_BLOCKS_ADAMANTITE = common("storage_blocks/adamantite");
        public static final TagKey<Item> RAW_MATERIALS_ADAMANTITE = common("raw_materials/adamantite");
        public static final TagKey<Item> ORES_ADAMANTITE = common("ores/adamantite");
        public static final TagKey<Item> INGOTS_ADAMANTITE = common("ingots/adamantite");

        public static TagKey<Item> common(String path) {
            return createItemTag(AlloyForgeryTags.common(path));
        }

        public static TagKey<Item> alloyForgery(String path) {
            return createItemTag(AlloyForgeryTags.alloyForgery(path));
        }

        private static TagKey<Item> createItemTag(Identifier id) {
            return TagKey.of(RegistryKeys.ITEM, id);
        }
    }

    private static Identifier common(String path) {
        return Identifier.of(COMMON_NAMESPACE, path);
    }

    private static Identifier alloyForgery(String path) {
        return AlloyForgery.id(path);
    }
}
