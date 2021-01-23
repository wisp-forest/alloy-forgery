package wraith.alloy_forgery;

import java.util.HashSet;

public class Forge {

    public HashSet<String> materials;
    public HashSet<String> recipeMaterials;
    public String controller;
    public float tier;
    public int maxHeat;

    public Forge(HashSet<String> materials, HashSet<String> recipeMaterials, float tier, String controller, int maxHeat) {
        this.materials = materials;
        this.tier = tier;
        this.controller = controller;
        this.maxHeat = maxHeat;
        this.recipeMaterials = recipeMaterials;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Forge forge = (Forge) o;
        return forge.tier == this.tier && forge.materials.equals(this.materials) && forge.controller.equals(this.controller);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Float.hashCode(tier);
        hash = 31 * hash + (materials == null ? 0 : materials.hashCode());
        hash = 31 * hash + (controller == null ? 0 : controller.hashCode());
        return hash;
    }

}
