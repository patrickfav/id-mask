package at.favre.lib.idmask;

import java.util.Objects;

/**
 * Simple tuple encapsulating 2 longs (64 bit integers).
 * Can be used to easily encode 2 ids to one masked id.
 */
@SuppressWarnings("WeakerAccess")
public final class LongTuple {
    private final long num1;
    private final long num2;

    /**
     * Create new instance with given long numbers
     *
     * @param num1
     * @param num2
     */
    public LongTuple(long num1, long num2) {
        this.num1 = num1;
        this.num2 = num2;
    }

    public long getNum1() {
        return num1;
    }

    public long getNum2() {
        return num2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LongTuple longTuple = (LongTuple) o;
        return num1 == longTuple.num1 &&
                num2 == longTuple.num2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(num1, num2);
    }
}
