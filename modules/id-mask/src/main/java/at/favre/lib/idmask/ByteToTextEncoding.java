package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

import java.nio.ByteOrder;

/**
 * Responsible for encoding byte arrays to ASCII safe text and vice versa.
 * More precisely, it is an encoding of binary data in a sequence of printable characters.
 */
public interface ByteToTextEncoding {

    /**
     * Encode given byte array to printable text
     *
     * @param bytes to mask
     * @return printable text
     */
    String encode(byte[] bytes);

    /**
     * Decode given encoded string (see {@link #encode(byte[])} to a byte array
     * @param encoded text to unmask
     * @return raw bytes as array
     */
    byte[] decode(CharSequence encoded);

    /**
     * RFC 4648 compatible Base64 encoding with url safe schema.
     * <p>
     * Example: <code>1oRwxy-z15R1tQ8oYQxq4tYfGTwa</code>
     */
    final class Base64Url implements ByteToTextEncoding {
        @Override
        public String encode(byte[] bytes) {
            return Bytes.wrap(bytes).encodeBase64(true, false);
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return Bytes.parseBase64(encoded).array();
        }
    }

    /**
     * Base32Rfc4648 uses a 32-character set comprising the twenty-six upper-case letters A–Z, and the digits 2–7.
     *
     * Example: <code>36YV2BTECHOTDTU4I23VND46HVXQ</code>
     */
    final class Base32Rfc4648 implements ByteToTextEncoding {
        @Override
        public String encode(byte[] bytes) {
            return Bytes.wrap(bytes).encodeBase32().replaceAll("=", "");
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return Bytes.parseBase32(encoded).array();
        }
    }


    /**
     * Base32Rfc4648 uses a 32-character set comprising the twenty-six upper-case letters A–Z, and the digits 2–7.
     * <p>
     * Example: <code>36YV2BTECHOTDTU4I23VND46HVXQ</code>
     */
    final class SafeBase32Encoding implements ByteToTextEncoding {
        private final BaseEncoding encoding;

        //abdegkmnpqrvwxyzABDEGKMNPQRVWXYZ23456789
        //abdegjklmnopqrvwxyzABDEGJKLMNOPQRVWXYZ23456789
        public SafeBase32Encoding() {
            this.encoding = new BaseEncoding(new BaseEncoding.Alphabet("abeknpqrwxyzBDEGKMPRVXYZ23456789".toCharArray()), '=');
        }

        @Override
        public String encode(byte[] bytes) {
            return encoding.encode(bytes, ByteOrder.BIG_ENDIAN);
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return encoding.decode(encoded);
        }
    }

    /**
     * Hexadecimal (also base 16, or hex) is a positional numeral system with a radix,
     * or base, of 16. It uses sixteen distinct symbols, most often the symbols "0"–"9"
     * to represent values zero to nine, and "a"–"f" to represent values ten to fifteen.
     *
     * Example: <code>b6f3044af5d8c14f447e5ae7f30d9d3a3c</code>
     */
    final class Base16 implements ByteToTextEncoding {
        @Override
        public String encode(byte[] bytes) {
            return Bytes.wrap(bytes).encodeHex();
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return Bytes.parseHex(encoded).array();
        }
    }
}
