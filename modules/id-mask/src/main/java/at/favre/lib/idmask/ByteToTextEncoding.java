package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

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
     *
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
     * Base encoding with alphabet of length 2^x (16, 32, 64, etc.)
     */
    class BaseMod8Encoding implements ByteToTextEncoding {
        private final BaseEncoding encoding;

        public BaseMod8Encoding(char[] alphabet, Character paddingChar) {
            int alphabetLength = alphabet.length;
            if (alphabetLength != 16 && alphabetLength != 32 && alphabetLength != 64) {
                throw new IllegalArgumentException("only alphabet length with 16, 32 or 64 supported");
            }
            this.encoding = new BaseEncoding(new BaseEncoding.Alphabet(alphabet), paddingChar);
        }

        @Override
        public String encode(byte[] bytes) {
            return encoding.encode(bytes);
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return encoding.decode(encoded);
        }
    }

    /**
     * Base32 encoding dialect with some letters removed so to omit the accidental creation of english
     * curse words. Also does not include usual letters/digits which can easily be confused (1,l,O,0,etc)
     * <p>
     * Example: <code>9RzRnY7XxzDYa5x3zxZ7PeE6yB</code>
     */
    class CleanBase32Encoding extends BaseMod8Encoding {
        public CleanBase32Encoding() {
            super("abeknpqrwxyzBDEGKMPRVXYZ23456789".toCharArray(), null);
        }
    }

    /**
     * Base32Rfc4648 uses a 32-character set comprising the twenty-six upper-case letters A–Z, and the digits 2–7. Does NOT include padding.
     * <p>
     * Example: <code>36YV2BTECHOTDTU4I23VND46HVXQ</code>
     */
    class Base32Rfc4648 extends BaseMod8Encoding {
        public Base32Rfc4648() {
            super("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray(), null);
        }
    }

    final class Base32Formatted extends Base32Rfc4648 {
        private static final String SEPARATOR = "-";
        private static final int INTERVAL = 4;

        private final int currentInterval;
        private final String currentSeparator;

        public Base32Formatted() {
            this(INTERVAL, SEPARATOR);
        }

        public Base32Formatted(int currentInterval, String currentSeparator) {
            this.currentInterval = currentInterval;
            this.currentSeparator = currentSeparator;
        }

        @Override
        public String encode(byte[] bytes) {
            return format(super.encode(bytes));
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return super.decode(encoded.toString().replaceAll(currentSeparator, ""));
        }

        private String format(String unformatted) {
            if (unformatted.length() < currentInterval * 2 - 1) {
                return unformatted;
            } else {
                StringBuilder sb = new StringBuilder();
                int remainingLength = unformatted.length();
                boolean even = true;
                while (remainingLength > 0) {
                    int interval = even ? currentInterval : currentInterval + 2;
                    int startIndex = unformatted.length() - remainingLength;
                    if (remainingLength < interval) {
                        sb.append(unformatted, startIndex, unformatted.length());
                        remainingLength = 0;
                    } else {
                        sb.append(unformatted, startIndex, startIndex + interval);
                        remainingLength -= interval;
                        if (remainingLength != 0) {
                            sb.append(currentSeparator);
                        }
                    }
                    even = !even;
                }
                return sb.toString();
            }
        }
    }

    /**
     * Hexadecimal (also base 16, or hex) is a positional numeral system with a radix,
     * or base, of 16. It uses sixteen distinct symbols, most often the symbols "0"–"9"
     * to represent values zero to nine, and "a"–"f" to represent values ten to fifteen.
     * <p>
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
