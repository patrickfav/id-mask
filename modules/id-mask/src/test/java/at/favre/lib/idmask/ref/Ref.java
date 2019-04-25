package at.favre.lib.idmask.ref;

/**
 * Reference values for a test
 *
 * @param <T>
 */
public class Ref<T> {
    private final T id;
    private final String encoded;

    Ref(T id, String encoded) {
        this.id = id;
        this.encoded = encoded;
    }

    public T getId() {
        return id;
    }

    public String getEncoded() {
        return encoded;
    }
}
