package wraith.alloy_forgery;

public class RecipeOutput {

    public final int outputAmount;
    public final String outputItem;
    public final int heatAmount;
    public final float requiredTier;

    public RecipeOutput(String outputItem, int outputAmount, int heatAmount, float requiredTier) {
        this.outputAmount = outputAmount;
        this.outputItem = outputItem;
        this.heatAmount = heatAmount;
        this.requiredTier = requiredTier;
    }
}
