package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

public interface ByteToTextEncoding {

    String encode(byte[] bytes);

    byte[] decode(CharSequence encoded);

    final class Base64 implements ByteToTextEncoding {
        @Override
        public String encode(byte[] bytes) {
            return Bytes.wrap(bytes).encodeBase64(true, false);
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return Bytes.parseBase64(encoded).array();
        }
    }

    final class Base32 implements ByteToTextEncoding {
        @Override
        public String encode(byte[] bytes) {
            return Bytes.wrap(bytes).encodeBase32();
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return Bytes.parseBase32(encoded).array();
        }
    }
}
