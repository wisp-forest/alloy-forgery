package wraith.alloy_forgery;

public class ForgeFuel {

    private final int cookTime;
    private final String item;
    private final String returnableItem;
    private final boolean rightclickable;

    public ForgeFuel(String item, int cookTime, String returnableItem, boolean rightclickable) {
        this.item = item;
        this.cookTime = cookTime;
        this.returnableItem = returnableItem;
        this.rightclickable = rightclickable;
    }

    public int getCookTime() {
        return cookTime;
    }

    public String getItem() {
        return item;
    }

    public String getReturnableItem() {
        return returnableItem;
    }

    public boolean hasReturnableItem() {
        return this.returnableItem != null;
    }

    public boolean isRightclickable() {
        return rightclickable;
    }

}
