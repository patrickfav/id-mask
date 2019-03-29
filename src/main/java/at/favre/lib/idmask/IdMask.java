package at.favre.lib.idmask;

public interface IdMask<T extends ByteArrayAble> {

    String encode(T id);

    T decode(String encoded);

    final class Default implements IdMask {

        private final IdMaskEngine engine = new IdMaskEngine.Default(null, null);

        @Override
        public String encode(ByteArrayAble id) {
            return null;
        }

        @Override
        public ByteArrayAble decode(String encoded) {
            return null;
        }
    }
}
