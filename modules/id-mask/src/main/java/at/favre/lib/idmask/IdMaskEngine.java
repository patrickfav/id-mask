package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.crypto.HKDF;
import org.cryptomator.siv.SivMode;
import org.cryptomator.siv.UnauthenticCiphertextException;

import javax.crypto.IllegalBlockSizeException;
import java.nio.ByteBuffer;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * The underlying engine responsible for encrypting the provided id.
 * <p>
 * IdMaskEngines are thread safe.
 */
public interface IdMaskEngine {

    /**
     * Mask (or encrypt) given id as bytes. This process is reversible with {@link #unmask(CharSequence)}
     *
     * @param plainId (aka plaintext) to mask
     * @return masked (aka cipher text)
     * @throws IllegalArgumentException if basic parameter validation fails
     */
    CharSequence mask(byte[] plainId);

    /**
     * Unmask (or decrypt) given masked id.
     *
     * @param maskedId to unmask
     * @return unmasked, plain id as passed in {@link #mask(byte[])}
     * @throws IdMaskSecurityException  if used secret key, authentication tag, or version identifiers are incorrect
     * @throws IllegalArgumentException if basic parameter validation fails
     */
    byte[] unmask(CharSequence maskedId);

    /**
     * Internal maximal engine id used in version byte
     */
    int MAX_ENGINE_ID = 0x0F; //4 bit or 0-15

    /**
     * Internal maximal key id used in version byte
     */
    int MAX_KEY_ID = 0x0F; //4 bit or 0-15

    final class AesSivEngine implements IdMaskEngine {
        private static final int ENGINE_ID = 2;
        static int MAX_MASKED_ID_ENCODED_LENGTH = 768;
        static int MIN_MASKED_ID_ENCODED_LENGTH = 8;

        private final ThreadLocal<SivMode> sivModeThreadLocal = new ThreadLocal<>();
        private final HKDF hkdf;
        private final KeyManager keyManager;
        private final ByteToTextEncoding encoding;
        private final boolean randomizeIds;
        private final Provider provider;
        private final SecureRandom secureRandom;
        private final IdEncConfig idEncConfig;
        private final byte[] obfuscationKey;

        AesSivEngine(KeyManager keyManager, IdEncConfig idEncConfig) {
            this(keyManager, idEncConfig, Bytes.allocate(8).array(), new ByteToTextEncoding.Base64Url(), false, new SecureRandom(), null);
        }

        AesSivEngine(KeyManager keyManager, IdEncConfig idEncConfig, byte[] obfuscationKey, ByteToTextEncoding encoding, boolean randomizeIds, SecureRandom secureRandom, Provider provider) {
            this.keyManager = KeyManager.CachedKdfConverter.wrap(keyManager, new KeyManager.CachedKdfConverter.KdfConverter() {
                @Override
                public byte[] convert(KeyManager.IdSecretKey original) {
                    return hkdf.extract(Bytes.from(original.getKeyId()).array(), original.getKeyBytes());
                }
            });
            this.idEncConfig = idEncConfig;
            this.encoding = encoding;
            this.randomizeIds = randomizeIds;
            this.hkdf = HKDF.fromHmacSha512();
            this.secureRandom = secureRandom;
            this.provider = provider;
            this.obfuscationKey = hkdf.expand(obfuscationKey, null, outputLength());
        }

        private int entropyLength() {
            return idEncConfig.randomizedIdLengthBytes;
        }

        private int outputLength() {
            return idEncConfig.valueLengthBytes + idEncConfig.macLengthBytes + 1 + (randomizeIds ? idEncConfig.randomizedIdLengthBytes : 0);
        }

        @Override
        public CharSequence mask(byte[] plainId) {
            if (Objects.requireNonNull(plainId, "id").length != idEncConfig.valueLengthBytes) {
                throw new IllegalArgumentException(String.format("id length must be exactly %d bytes in length", idEncConfig.valueLengthBytes));
            }

            byte[] entropy = getEntropyBytes(entropyLength());
            byte[] keys = keyManager.getActiveKey().getKeyBytes();

            SivMode sivMode = getSiv();
            byte[] cipherText = sivMode.encrypt(
                    Bytes.from(keys, 0, 16).array(),
                    Bytes.from(keys, 16, 16).array(),
                    plainId,
                    Bytes.from((byte) keyManager.getActiveKeyId(), engineId()).append(entropy).array());

            ByteBuffer bb;
            byte version = createVersionByte((byte) keyManager.getActiveKeyId(), cipherText);

            if (randomizeIds) {
                bb = ByteBuffer.allocate(1 + entropy.length + cipherText.length);
                bb.put(version);
                bb.put(entropy);
                bb.put(cipherText);
            } else {
                bb = ByteBuffer.allocate(1 + cipherText.length);
                bb.put(version);
                bb.put(cipherText);
            }

            return encoding.encode(Bytes.from(bb).xor(obfuscationKey).array());
        }

        @Override
        public byte[] unmask(CharSequence maskedId) {
            checkInput(maskedId);
            byte[] decoded = encoding.decode(maskedId);

            if (decoded.length != outputLength()) {
                throw new IllegalArgumentException("unexpected decoded length '" + decoded.length + "' expected '" + outputLength() + "'");
            }

            ByteBuffer bb = ByteBuffer.wrap(Bytes.wrap(decoded).xor(obfuscationKey).array());

            byte version = bb.get();
            byte[] entropy = getEntropyBytes(entropyLength());
            if (randomizeIds) {
                bb.get(entropy);
            }
            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);

            KeyManager.IdSecretKey currentKey = checkAndGetCurrentKey(version, cipherText);

            byte[] keys = currentKey.getKeyBytes();
            SivMode sivMode = getSiv();

            try {
                return sivMode.decrypt(
                        Bytes.from(keys, 0, 16).array(),
                        Bytes.from(keys, 16, 16).array(),
                        cipherText,
                        Bytes.from((byte) currentKey.getKeyId(), engineId()).append(entropy).array());
            } catch (UnauthenticCiphertextException | IllegalBlockSizeException e) {
                throw new IdMaskSecurityException("could not decrypt", IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY, e);
            }
        }

        private synchronized SivMode getSiv() {
            if (sivModeThreadLocal.get() == null) {

                try {
                    if (provider != null) {
                        throw new UnsupportedOperationException("currently setting custom provider is not supported");
                    } else {
                        sivModeThreadLocal.set(new SivMode());
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("could not get cipher instance", e);
                }
            }
            return sivModeThreadLocal.get();
        }

        byte[] getEntropyBytes(int size) {
            if (randomizeIds) {
                byte[] rnd = new byte[size];
                secureRandom.nextBytes(rnd);
                return rnd;
            } else {
                return Bytes.allocate(size).array();
            }
        }

        /**
         * Creates a version byte encoding keyId and cipherText
         *
         * @param keyId      to encode in version (encoded to first 4 bit)
         * @param cipherText to encode in version (encoded to last 4 bit)
         * @return version byte
         */
        byte createVersionByte(byte keyId, byte[] cipherText) {
            byte engineId = engineId();
            if (keyId < 0 || keyId > MAX_KEY_ID || engineId < 0 || engineId > MAX_ENGINE_ID) {
                throw new IllegalArgumentException("key and engine id must can only be 4 bit long");
            }

            return Bytes.from(keyId).leftShift(4).or(Bytes.from(engineId)).xor(Bytes.from(cipherText, 0, 1)).toByte();
        }

        /**
         * Checks given version byte if it matches current implementation
         *
         * @param version    to decode
         * @param cipherText used to de obfuscate version byte
         * @return secret key to decode
         */
        KeyManager.IdSecretKey checkAndGetCurrentKey(byte version, byte[] cipherText) {
            byte versionEngineId = getEngineIdFromVersion(version, cipherText);
            if (versionEngineId != engineId()) {
                throw new IdMaskSecurityException("wrong idMask engine used according to version byte - expected '" + engineId() + "' got '" + versionEngineId + "'",
                        IdMaskSecurityException.Reason.UNKNOWN_ENGINE_ID);
            }
            byte keyId = getKeyIdFromVersion(version, cipherText);
            KeyManager.IdSecretKey currentSecretKey = getKeyForId(keyId);

            if (currentSecretKey == null || currentSecretKey.getKeyBytes() == null) {
                throw new IdMaskSecurityException("unknown key id '" + keyId + "'", IdMaskSecurityException.Reason.UNKNOWN_KEY_ID);
            }
            return currentSecretKey;
        }

        private byte engineId() {
            return ENGINE_ID;
        }

        byte getKeyIdFromVersion(byte obfuscatedVersion, byte[] cipherText) {
            return Bytes.from(obfuscatedVersion).xor(Bytes.from(cipherText, 0, 1)).rightShift(4).and(Bytes.from((byte) 0b00001111)).toByte();
        }

        byte getEngineIdFromVersion(byte obfuscatedVersion, byte[] cipherText) {
            return Bytes.from(obfuscatedVersion).xor(Bytes.from(cipherText, 0, 1)).and(Bytes.from((byte) 0b00001111)).toByte();
        }

        KeyManager.IdSecretKey getKeyForId(byte keyId) {
            KeyManager.IdSecretKey key;
            if ((key = keyManager.getById(keyId)) == null) {
                return null;
            }
            return key;
        }

        /**
         * Parameter input validation for masked ids
         *
         * @param maskedId to validate
         * @throws IllegalArgumentException if input is not valid
         */
        void checkInput(CharSequence maskedId) {
            if (Objects.requireNonNull(maskedId, "maskedId").length() > MAX_MASKED_ID_ENCODED_LENGTH || maskedId.length() < MIN_MASKED_ID_ENCODED_LENGTH) {
                throw new IllegalArgumentException("encoded masked id too long or short, must be between " + MIN_MASKED_ID_ENCODED_LENGTH + " and " + MAX_MASKED_ID_ENCODED_LENGTH + " chars");
            }
        }

        public enum IdEncConfig {
            INTEGER_4_BYTE(0, 4, 16, 10),
            INTEGER_8_BYTE(1, 8, 16, 12),
            INTEGER_16_BYTE(2, 16, 16, 16),
            INTEGER_32_BYTE(4, 32, 16, 16),
            ;
            public final byte id;
            public final int valueLengthBytes;
            public final int macLengthBytes;
            public final int randomizedIdLengthBytes;

            IdEncConfig(int id, int valueLengthBytes, int macLengthBytes, int randomizedIdLengthBytes) {
                this.id = (byte) id;
                this.valueLengthBytes = valueLengthBytes;
                this.macLengthBytes = macLengthBytes;
                this.randomizedIdLengthBytes = randomizedIdLengthBytes;
            }
        }
    }
}
