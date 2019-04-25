package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

import java.util.Objects;
import java.util.regex.Pattern;

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
     * Example: <code>jKVJx8yQPP8wkBNQhZBkJr6vKhwH</code>
     */
    class CleanBase32Encoding extends BaseMod8Encoding {
        public CleanBase32Encoding() {
            super("bghjkmnprvwxyzBGHJKMNPQRVWXYZ689".toCharArray(), null);
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

    /**
     * ID formatting decorator.
     * Wrap an existing {@link ByteToTextEncoding} instance to add simple formatting capabilities.
     * <p>
     * A call can set the interval and separator. An id will be formatted similar to:
     *
     * <pre>
     *     xxxx-yyyyyy-xxxx-yyyyyy-xx
     * </pre>
     * <p>
     * where <code>-</code> is the separator and the interval is 4. Note that every other id part will have length
     * <code>interval+2</code> to improve readability.
     */
    final class IdFormatter implements ByteToTextEncoding {
        private final ByteToTextEncoding byteToTextEncoding;
        private static final String SEPARATOR = "-";
        private static final int INTERVAL = 4;

        private final int currentInterval;
        private final String currentSeparator;

        /**
         * Create new formatter instance with default separator and interval.
         *
         * @param byteToTextEncoding to wrap
         * @return formatted byteToTextEncoding
         */
        public static IdFormatter wrap(ByteToTextEncoding byteToTextEncoding) {
            return new IdFormatter(byteToTextEncoding, INTERVAL, SEPARATOR);
        }

        /**
         * Create new formatter instance.
         *
         * @param byteToTextEncoding to wrap
         * @param interval           of the single parts of the id. E.g. 4 looks like this: <code>xxxx-yyyyyy-xxxxx-...</code>
         * @return formatted byteToTextEncoding
         */
        public static IdFormatter wrap(ByteToTextEncoding byteToTextEncoding, int interval) {
            return new IdFormatter(byteToTextEncoding, interval, SEPARATOR);
        }

        /**
         * Create new formatter instance.
         *
         * @param byteToTextEncoding to wrap
         * @param interval           of the single parts of the id. E.g. 4 looks like this: <code>xxxx-yyyyyy-xxxxx-...</code>
         * @param separator          between the id parts; be aware not to use a character used in the encoding alphabet
         * @return formatted byteToTextEncoding
         */
        public static IdFormatter wrap(ByteToTextEncoding byteToTextEncoding, int interval, String separator) {
            return new IdFormatter(byteToTextEncoding, interval, separator);
        }

        IdFormatter(ByteToTextEncoding byteToTextEncoding, int currentInterval, String currentSeparator) {
            this.byteToTextEncoding = Objects.requireNonNull(byteToTextEncoding, "byteToTextEncoding");
            this.currentInterval = currentInterval;
            this.currentSeparator = Objects.requireNonNull(currentSeparator, "separator");

            if (currentInterval < 2) {
                throw new IllegalArgumentException("interval must be at least 2");
            }

            if (currentSeparator.length() < 1 || currentSeparator.length() > 6) {
                throw new IllegalArgumentException("separator must be between 1 and 6 chars long");
            }
        }

        @Override
        public String encode(byte[] bytes) {
            return format(byteToTextEncoding.encode(bytes));
        }

        @Override
        public byte[] decode(CharSequence encoded) {
            return byteToTextEncoding.decode(encoded.toString().replaceAll(Pattern.quote(currentSeparator), ""));
        }

        private String format(String unformatted) {
            if (unformatted.length() < currentInterval * 2 - 1) {
                return unformatted;
            } else {
                if (unformatted.contains(currentSeparator)) {
                    throw new IllegalArgumentException("Current separator '" + currentSeparator +
                            "' can be found in the encoding of the id '" + unformatted + "' - please choose different one.");
                }
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
}
