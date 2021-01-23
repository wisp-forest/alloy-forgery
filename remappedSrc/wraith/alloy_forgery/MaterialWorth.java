package wraith.alloy_forgery;

public class MaterialWorth {

    public final int worth;
    public final boolean canReturn;

    public MaterialWorth(int worth, boolean canReturn) {
        this.worth = worth;
        this.canReturn = canReturn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        MaterialWorth worth = (MaterialWorth) o;
        return worth.worth == this.worth && worth.canReturn == this.canReturn;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Integer.hashCode(worth);
        hash = 31 * hash + Boolean.hashCode(canReturn);
        return hash;
    }

}
