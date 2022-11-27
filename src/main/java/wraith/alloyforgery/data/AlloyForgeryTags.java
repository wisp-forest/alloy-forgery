package wraith.alloyforgery.data;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.alloyforgery.AlloyForgery;

public class AlloyForgeryTags {

    private static final String COMMON_NAMESPACE = "c";

    public static class Items {

        public static final TagKey<Item> RAW_COPPER_ORE_BLOCKS = registerCommon("raw_copper_blocks");
        public static final TagKey<Item> RAW_IRON_ORE_BLOCKS = registerCommon("raw_iron_blocks");
        public static final TagKey<Item> RAW_GOLD_ORE_BLOCKS = registerCommon("raw_gold_blocks");

        //Zinc
        public static final TagKey<Item> RAW_ZINC_ORE_BLOCKS = registerCommon("raw_zinc_blocks");
        public static final TagKey<Item> ZINC_BLOCKS = registerCommon("zinc_blocks");

        //tungsten
        public static final TagKey<Item> RAW_TUNGSTEN_ORE_BLOCKS = registerCommon("raw_tungsten_blocks");
        public static final TagKey<Item> TUNGSTEN_BLOCKS = registerCommon("tungsten_blocks");

        //titanium
        public static final TagKey<Item> RAW_TITANIUM_ORE_BLOCKS = registerCommon("raw_titanium_blocks");
        public static final TagKey<Item> TITANIUM_BLOCKS = registerCommon("titanium_blocks");

        //tin
        public static final TagKey<Item> RAW_TIN_ORE_BLOCKS = registerCommon("raw_tin_blocks");
        public static final TagKey<Item> TIN_BLOCKS = registerCommon("tin_blocks");

        //silver
        public static final TagKey<Item> RAW_SILVER_ORE_BLOCKS = registerCommon("raw_silver_blocks");
        public static final TagKey<Item> SILVER_BLOCKS = registerCommon("silver_blocks");

        //platinum
        public static final TagKey<Item> RAW_PLATINUM_ORE_BLOCKS = registerCommon("raw_platinum_blocks");
        public static final TagKey<Item> PLATINUM_BLOCKS = registerCommon("platinum_blocks");

        //palladium
        public static final TagKey<Item> RAW_PALLADIUM_ORE_BLOCKS = registerCommon("raw_palladium_blocks");
        public static final TagKey<Item> PALLADIUM_BLOCKS = registerCommon("palladium_blocks");

        //osmium
        public static final TagKey<Item> RAW_OSMIUM_ORE_BLOCKS = registerCommon("raw_osmium_blocks");
        public static final TagKey<Item> OSMIUM_BLOCKS = registerCommon("osmium_blocks");

        //orichalcum
        public static final TagKey<Item> RAW_ORICHALCUM_ORE_BLOCKS = registerCommon("raw_orichalcum_blocks");
        public static final TagKey<Item> ORICHALCUM_BLOCKS = registerCommon("orichalcum_blocks");

        //nickel
        public static final TagKey<Item> RAW_NICKEL_ORE_BLOCKS = registerCommon("raw_nickel_blocks");
        public static final TagKey<Item> NICKEL_BLOCKS = registerCommon("nickel_blocks");

        //mythril
        public static final TagKey<Item> RAW_MYTHRIL_ORE_BLOCKS = registerCommon("raw_mythril_blocks");
        public static final TagKey<Item> MYTHRIL_BLOCKS = registerCommon("mythril_blocks");

        //mythril
        public static final TagKey<Item> RAW_MANGANESE_ORE_BLOCKS = registerCommon("raw_manganese_blocks");
        public static final TagKey<Item> MANGANESE_BLOCKS = registerCommon("manganese_blocks");

        //lead
        public static final TagKey<Item> RAW_LEAD_ORE_BLOCKS = registerCommon("raw_lead_blocks");
        public static final TagKey<Item> LEAD_BLOCKS = registerCommon("lead_blocks");

        //iridium
        public static final TagKey<Item> RAW_IRIDIUM_ORE_BLOCKS = registerCommon("raw_iridium_blocks");
        public static final TagKey<Item> IRIDIUM_BLOCKS = registerCommon("iridium_blocks");

        //antimony
        public static final TagKey<Item> RAW_ANTIMONY_ORE_BLOCKS = registerCommon("raw_antimony_blocks");
        public static final TagKey<Item> ANTIMONY_BLOCKS = registerCommon("antimony_blocks");

        //adamantite
        public static final TagKey<Item> RAW_ADAMANTITE_ORE_BLOCKS = registerCommon("raw_adamantite_blocks");
        public static final TagKey<Item> ADAMANTITE_BLOCKS = registerCommon("adamantite_blocks");


        private static TagKey<Item> registerCommon(String path) {
            return register(common(path));
        }

        private static TagKey<Item> registerAlloyForgery(String path) {
            return register(alloyForgery(path));
        }

        private static TagKey<Item> register(Identifier id) {
            return TagKey.of(Registry.ITEM_KEY, id);
        }
    }

    private static Identifier common(String path){
        return new Identifier(COMMON_NAMESPACE, path);
    }

    private static Identifier alloyForgery(String path){
        return AlloyForgery.id(path);
    }
}
