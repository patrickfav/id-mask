package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.crypto.HKDF;
import org.cryptomator.siv.SivMode;
import org.cryptomator.siv.UnauthenticCiphertextException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
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

    /**
     * Base implementation of the engine
     */
    abstract class BaseEngine {
        static int MAX_MASKED_ID_ENCODED_LENGTH = 768;
        static int MIN_MASKED_ID_ENCODED_LENGTH = 8;
        private ThreadLocal<Cipher> cipherWrapper = new ThreadLocal<>();
        final Provider provider;
        final SecureRandom secureRandom;
        final ByteToTextEncoding encoding;
        final HKDF hkdf;
        final KeyManager keyManager;
        final boolean randomizeIds;
        final boolean autoWipeMemory;
        final int supportedIdByteLength;

        BaseEngine(int supportedIdByteLength, KeyManager keyManager, Provider provider, SecureRandom secureRandom, ByteToTextEncoding encoding, boolean randomizeIds, boolean autoWipeMemory) {
            this.hkdf = HKDF.fromHmacSha512();
            this.provider = provider;
            this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom");
            this.encoding = Objects.requireNonNull(encoding, "encoding");
            this.keyManager = KeyManager.CachedKdfConverter.wrap(keyManager, new KeyManager.CachedKdfConverter.KdfConverter() {
                @Override
                public byte[] convert(KeyManager.IdSecretKey original) {
                    return hkdf.extract(Bytes.from(original.getKeyId()).array(), original.getKeyBytes());
                }
            });
            this.randomizeIds = randomizeIds;
            this.autoWipeMemory = autoWipeMemory;
            this.supportedIdByteLength = supportedIdByteLength;
        }

        /**
         * The used cipher algorithm transformation
         *
         * @return cipher transformation
         */
        protected abstract String getCipherAlgorithm();

        /**
         * Return the engine id. Every implementation or crypto scheme should have its own id which will be
         * encoded with the version byte. An id must be checked against the engine id if it is the correct one.
         *
         * @return 4bit engine id (0-15)
         */
        protected abstract byte engineId();

        /**
         * Get the supported byte length of ids that can be handled by the engine
         *
         * @return byte length
         */
        int getSupportedIdByteLength() {
            return supportedIdByteLength;
        }

        /**
         * Create entropy bytes for further cryptographic functions
         *
         * @param size of the entropy
         * @return entropy bytes
         */
        byte[] getEntropyBytes(int size) {
            if (randomizeIds) {
                byte[] rnd = new byte[size];
                secureRandom.nextBytes(rnd);
                return rnd;
            } else {
                return Bytes.allocate(size).array();
            }
        }

        synchronized Cipher getCipher() {
            Cipher cipher = cipherWrapper.get();
            if (cipher == null) {
                try {
                    if (provider != null) {
                        cipher = Cipher.getInstance(getCipherAlgorithm(), provider);
                    } else {
                        cipher = Cipher.getInstance(getCipherAlgorithm());
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("could not get cipher instance", e);
                }
                cipherWrapper.set(cipher);
                return cipherWrapper.get();
            } else {
                return cipher;
            }
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

        byte getKeyIdFromVersion(byte obfuscatedVersion, byte[] cipherText) {
            return Bytes.from(obfuscatedVersion).xor(Bytes.from(cipherText, 0, 1)).rightShift(4).and(Bytes.from((byte) 0b00001111)).toByte();
        }

        byte getEngineIdFromVersion(byte obfuscatedVersion, byte[] cipherText) {
            return Bytes.from(obfuscatedVersion).xor(Bytes.from(cipherText, 0, 1)).and(Bytes.from((byte) 0b00001111)).toByte();
        }

        byte[] getCurrentIdKey() {
            return keyManager.getActiveKey().getKeyBytes();
        }

        byte[] getKeyForId(byte keyId) {
            KeyManager.IdSecretKey key;
            if ((key = keyManager.getById(keyId)) == null) {
                return null;
            }
            return key.getKeyBytes();
        }

        /**
         * Checks given version byte if it matches current implementation
         *
         * @param version    to decode
         * @param cipherText used to de obfuscate version byte
         * @return secret key to decode
         */
        byte[] checkAndGetCurrentKey(byte version, byte[] cipherText) {
            byte versionEngineId = getEngineIdFromVersion(version, cipherText);
            if (versionEngineId != engineId()) {
                throw new IdMaskSecurityException("wrong idMask engine used according to version byte - expected '" + engineId() + "' got '" + versionEngineId + "'",
                        IdMaskSecurityException.Reason.UNKNOWN_ENGINE_ID);
            }
            byte keyId = getKeyIdFromVersion(version, cipherText);
            byte[] currentSecretKey = getKeyForId(keyId);

            if (currentSecretKey == null) {
                throw new IdMaskSecurityException("unknown key id '" + keyId + "'", IdMaskSecurityException.Reason.UNKNOWN_KEY_ID);
            }
            return currentSecretKey;
        }
    }

    /**
     * This schema uses the following cryptographic primitives:
     *
     * <ul>
     * <li>AES-128 + ECB + No Padding</li>
     * </ul>
     * <p>
     * Using the a full 16 byte AES block, we create a message containing of the 8 byte id (ie. the plaintext) and an 8 byte
     * reference value. Then we encrypt it with AES/ECB (since we encrypt only a single block, a block mode using an IV like CBC
     * wouldn't make a difference):
     *
     * <pre>
     *     message_d = ( refValue_1a | id )
     *     maskedId_d = ciphertext_d = AES_ECB( message_d )
     * </pre>
     * <p>
     * When decrypting, we compare the reference value, and if it has changed we discard the id, since either the key is incorrect,
     * or this was a forgery attempt:
     * <pre>
     *     AES_ECB( maskedId_d ) = refValue_1b | id
     *     refValue_1a == refValue_1b
     * </pre>
     *
     * <h3>Deterministic</h3>
     * <p>
     * In the deterministic mode the reference value is just a 8 byte long array of zeros.
     *
     * <h3>Randomized</h3>
     * <p>
     * In the randomized mode the reference value is a random 8 byte long array. Because the decryption requires knowledge
     * of this value it will be prepended to the cipher text:
     * <pre>
     *     ciphertext_r = AES_ECB( refValue_rnd | id )
     *     maskedId_r = refValue_rnd | ciphertext_r
     * </pre>
     *
     * <h3>Versione Byte</h3>
     * Both modes have a version byte prepended which will be xor-ed with the first byte of the cipher text for simple obfuscation:
     * <pre>
     *     obfuscated_version_byte = version_byte ^ ciphertext[0]
     * </pre>
     * Finally the message looks like this:
     * <pre>
     *     maskeId_msg_d = obfuscated_version_byte | maskedId_d
     * </pre>
     * and
     * <pre>
     *     maskeId_msg_r = obfuscated_version_byte | maskedId_r
     * </pre>
     * for randomized encryption.
     */
    @SuppressWarnings("WeakerAccess")
    final class EightByteEncryptionEngine extends BaseEngine implements IdMaskEngine {
        private static final String ALGORITHM = "AES/ECB/NoPadding";
        private static final int ENGINE_ID = 0;

        EightByteEncryptionEngine(KeyManager keyManager) {
            this(keyManager, null, new SecureRandom(), new ByteToTextEncoding.Base64Url(), false, false);
        }

        public EightByteEncryptionEngine(KeyManager keyManager, Provider provider, SecureRandom secureRandom, ByteToTextEncoding encoding, boolean randomizeIds, boolean autoWipeMemory) {
            super(8, keyManager, provider, secureRandom, encoding, randomizeIds, autoWipeMemory);
        }

        @Override
        public CharSequence mask(byte[] plainId) {
            if (plainId.length != getSupportedIdByteLength()) {
                throw new IllegalArgumentException("input must be 8 byte long");
            }

            byte[] random = null;
            byte[] message = null;
            byte[] cipherText = null;
            try {
                random = getEntropyBytes(getSupportedIdByteLength());
                message = Bytes.wrap(random).append(plainId).array();
                SecretKey secretKey = new SecretKeySpec(Bytes.from(getCurrentIdKey(), 0, 16).array(), "AES");

                Cipher c = getCipher();
                c.init(Cipher.ENCRYPT_MODE, secretKey);
                cipherText = c.doFinal(message);

                final ByteBuffer bb;
                byte version = createVersionByte((byte) keyManager.getActiveKeyId(), cipherText);
                if (randomizeIds) {
                    bb = ByteBuffer.allocate(1 + random.length + cipherText.length);
                    bb.put(version);
                    bb.put(random);
                    bb.put(cipherText);
                } else {
                    bb = ByteBuffer.allocate(1 + cipherText.length);
                    bb.put(version);
                    bb.put(cipherText);
                }

                try {
                    return encoding.encode(bb.array());
                } finally {
                    Bytes.wrap(bb.array()).mutable().secureWipe();
                }
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                if (autoWipeMemory) {
                    Bytes.wrapNullSafe(random).mutable().secureWipe();
                    Bytes.wrapNullSafe(message).mutable().secureWipe();
                    Bytes.wrapNullSafe(cipherText).mutable().secureWipe();
                }
            }
        }

        @Override
        public byte[] unmask(CharSequence maskedId) {
            checkInput(maskedId);

            ByteBuffer bb = ByteBuffer.wrap(encoding.decode(maskedId));

            if (bb.remaining() != 1 + (randomizeIds ? 3 : 2) * getSupportedIdByteLength()) {
                throw new IllegalArgumentException("unexpected message id length " + bb.remaining());
            }

            byte[] entropyData = null;
            byte[] cipherText = null;
            byte[] message = null;
            byte version = bb.get();
            try {
                entropyData = getEntropyBytes(getSupportedIdByteLength());
                if (randomizeIds) {
                    bb.get(entropyData);
                }

                cipherText = new byte[bb.remaining()];
                bb.get(cipherText);

                final byte[] currentSecretKey = checkAndGetCurrentKey(version, cipherText);

                try {
                    SecretKey secretKey = new SecretKeySpec(Bytes.from(currentSecretKey, 0, 16).array(), "AES");
                    Cipher c = getCipher();
                    c.init(Cipher.DECRYPT_MODE, secretKey);
                    message = c.doFinal(cipherText);
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }

                if (!Bytes.from(message, 0, getSupportedIdByteLength()).equalsConstantTime(entropyData)) {
                    throw new IdMaskSecurityException("internal reference entropy does not match, probably forgery attempt or incorrect key", IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY);
                }

                return Bytes.from(message, 8, getSupportedIdByteLength()).array();

            } finally {
                if (autoWipeMemory) {
                    Bytes.wrapNullSafe(entropyData).mutable().secureWipe();
                    Bytes.wrapNullSafe(cipherText).mutable().secureWipe();
                    Bytes.wrapNullSafe(message).mutable().secureWipe();
                }
            }
        }

        @Override
        protected String getCipherAlgorithm() {
            return ALGORITHM;
        }

        @Override
        protected byte engineId() {
            return ENGINE_ID;
        }
    }

    /**
     * Engine for handling 16-byte long ids.
     * <p>
     * This schema uses the following cryptographic primitives:
     *
     * <ul>
     * <li>AES-128 + CBC</li>
     * <li>HMAC-SHA256</li>
     * <li>HKDF-HMAC-SHA512</li>
     * </ul>
     * <p>
     * The basic scheme works as follows:
     * <p>
     * First create the required keys and nonce:
     *
     * <pre>
     * okm = hkdf.expand(key, entropy, 64);
     * key_s = okm[0-16];
     * iv_s = okm[16-32];
     * mac_key_s = okm[32-64];
     *
     * key ......... provided secret key
     * entropy ..... 8 byte value. For randomized-ids it is a random value, otherwise zero bytes
     * </pre>
     * <p>
     * Then encrypt the id:
     *
     * <pre>
     * ciphertext = AES_CBC( iv_s , id ^ entropy)
     * mac = HMAC(ciphertext)
     * maskedId_msg= ciphertext | mac[0-8]
     *
     * id .......... id to mask (aka plaintext)
     * </pre>
     * <p>
     * optionally if randomized ids are enabled, also append `entropy` to the output:
     *
     * <pre>
     * maskedId_msg_r = entropy | maskedId_msg
     * </pre>
     * <p>
     * Finally append the version byte (see explanation in 8 byte schema). Use either the randomized or deterministic version:
     *
     * <pre>
     * maskeId_msg_r = obfuscated_version_byte | maskedId_msg_r
     * maskeId_msg_d = obfuscated_version_byte | maskedId_msg
     * </pre>
     */
    @SuppressWarnings("WeakerAccess")
    final class SixteenByteEngine extends BaseEngine implements IdMaskEngine {
        private static final String ALGORITHM = "AES/CBC/NoPadding";
        private static final String HMAC_ALGORITHM = "HmacSHA256";
        private static final int MAC_LENGTH_SHORT = 8;
        private static final int MAC_LENGTH_LONG = 16;
        private static final int ENGINE_ID = 1;

        private final boolean highSecurityMode;

        private ThreadLocal<Mac> macThreadLocal = new ThreadLocal<>();

        SixteenByteEngine(KeyManager keyManager) {
            this(keyManager, false, new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, false, false);
        }

        public SixteenByteEngine(KeyManager keyManager, boolean highSecurityMode, ByteToTextEncoding encoding, SecureRandom secureRandom, Provider provider, boolean randomizeIds, boolean autoWipeMemory) {
            super(16, keyManager, provider, secureRandom, encoding, randomizeIds, autoWipeMemory);
            this.highSecurityMode = highSecurityMode;
        }

        @SuppressWarnings("Duplicates")
        @Override
        public CharSequence mask(byte[] plainId) {
            Objects.requireNonNull(plainId, "id");

            if (plainId.length != getSupportedIdByteLength()) {
                throw new IllegalArgumentException(String.format("id length must be exactly %d bytes in length", getSupportedIdByteLength()));
            }

            byte[] entropy = null;
            byte[] keys = null;
            byte[] currentKey = null;
            byte[] iv;
            byte[] macKey = null;
            byte[] encryptedId = null;
            byte[] mac = null;
            try {
                entropy = getEntropyBytes(getSupportedIdByteLength());
                keys = hkdf.expand(getCurrentIdKey(), entropy, 64);

                currentKey = Bytes.from(keys, 0, 16).array();
                iv = Bytes.from(keys, 16, 16).array();
                macKey = Bytes.from(keys, 32, 32).array();

                Cipher cipher = getCipher();
                cipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(currentKey, "AES"),
                        new IvParameterSpec(iv));
                encryptedId = cipher.doFinal(Bytes.from(plainId).xor(entropy).array());
                byte version = createVersionByte((byte) keyManager.getActiveKeyId(), encryptedId);
                mac = Bytes.from(macCipherText(macKey, encryptedId, iv, new byte[]{version}), 0, getMacLength()).array();

                ByteBuffer bb = ByteBuffer.allocate(1 + encryptedId.length + mac.length + (randomizeIds ? entropy.length : 0));
                bb.put(version);

                if (randomizeIds) {
                    bb.put(entropy);
                }

                bb.put(encryptedId);
                bb.put(mac);

                return encoding.encode(bb.array());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            } finally {
                if (autoWipeMemory) {
                    Bytes.wrapNullSafe(entropy).mutable().secureWipe();
                    Bytes.wrapNullSafe(keys).mutable().secureWipe();
                    Bytes.wrapNullSafe(currentKey).mutable().secureWipe();
                    Bytes.wrapNullSafe(macKey).mutable().secureWipe();
                    Bytes.wrapNullSafe(encryptedId).mutable().secureWipe();
                    Bytes.wrapNullSafe(mac).mutable().secureWipe();
                }
            }
        }

        @SuppressWarnings("Duplicates")
        @Override
        public byte[] unmask(CharSequence maskedId) {
            checkInput(maskedId);

            ByteBuffer bb = ByteBuffer.wrap(encoding.decode(maskedId));

            checkDecodedLength(bb.remaining());

            byte[] entropy;
            byte[] keys = null;
            byte[] currentKey = null;
            byte[] iv = null;
            byte[] macKey = null;
            byte[] cipherText = null;
            byte[] mac = null;
            byte[] refMac = null;
            try {
                byte version = bb.get();
                entropy = getEntropyBytes(getSupportedIdByteLength());
                if (randomizeIds) {
                    bb.get(entropy);
                }
                cipherText = new byte[getSupportedIdByteLength()];
                bb.get(cipherText);

                mac = new byte[getMacLength()];
                bb.get(mac);

                byte[] currentSecretKey = checkAndGetCurrentKey(version, cipherText);

                keys = hkdf.expand(currentSecretKey, entropy, 64);

                currentKey = Bytes.from(keys, 0, 16).array();
                iv = Bytes.from(keys, 16, 16).array();
                macKey = Bytes.from(keys, 32, 32).array();

                refMac = Bytes.from(macCipherText(macKey, cipherText, iv, new byte[]{version}), 0, getMacLength()).array();
                if (!Bytes.wrap(mac).equalsConstantTime(refMac)) {
                    throw new IdMaskSecurityException("mac does not match", IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY);
                }
                try {
                    Cipher cipher = getCipher();
                    cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(currentKey, "AES"), new IvParameterSpec(iv));
                    return Bytes.wrap(cipher.doFinal(cipherText)).xor(entropy).array();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            } finally {
                if (autoWipeMemory) {
                    Bytes.wrapNullSafe(cipherText).mutable().secureWipe();
                    Bytes.wrapNullSafe(mac).mutable().secureWipe();
                    Bytes.wrapNullSafe(keys).mutable().secureWipe();
                    Bytes.wrapNullSafe(currentKey).mutable().secureWipe();
                    Bytes.wrapNullSafe(iv).mutable().secureWipe();
                    Bytes.wrapNullSafe(macKey).mutable().secureWipe();
                    Bytes.wrapNullSafe(refMac).mutable().secureWipe();
                }
            }
        }

        private void checkDecodedLength(int size) {
            int expectedLength = 1 + getSupportedIdByteLength() + (randomizeIds ? getSupportedIdByteLength() : 0) + getMacLength();
            if (size != expectedLength) {
                throw new IllegalArgumentException("unexpected message id length " + size + " - expected " + expectedLength);
            }
        }

        private int getMacLength() {
            return highSecurityMode ? MAC_LENGTH_LONG : MAC_LENGTH_SHORT;
        }

        private byte[] macCipherText(byte[] rawEncryptionKey, byte[] cipherText, byte[] iv, byte[] associatedData) {
            SecretKey macKey = createMacKey(rawEncryptionKey);

            try {
                Mac hmac = createHmacInstance();
                hmac.init(macKey);
                hmac.update(iv);
                hmac.update(cipherText);

                if (associatedData != null) {
                    hmac.update(associatedData);
                }

                return hmac.doFinal();
            } catch (InvalidKeyException e) {
                // due to key generation in createMacKey(byte[]) this actually can not happen
                throw new IllegalStateException("error during HMAC calculation");
            }
        }

        private SecretKey createMacKey(byte[] rawEncryptionKey) {
            byte[] derivedMacKey = HKDF.fromHmacSha256().expand(rawEncryptionKey, Bytes.from("macKey").array(), 32);
            return new SecretKeySpec(derivedMacKey, HMAC_ALGORITHM);
        }

        private synchronized Mac createHmacInstance() {
            Mac mac = macThreadLocal.get();
            if (mac == null) {
                try {
                    if (provider != null) {
                        mac = Mac.getInstance(HMAC_ALGORITHM, provider);
                    } else {
                        mac = Mac.getInstance(HMAC_ALGORITHM);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("could not get cipher instance", e);
                }
                macThreadLocal.set(mac);
                return macThreadLocal.get();
            } else {
                return mac;
            }
        }

        @Override
        protected String getCipherAlgorithm() {
            return ALGORITHM;
        }

        @Override
        protected byte engineId() {
            return ENGINE_ID;
        }
    }

    final class AesSivEngine implements IdMaskEngine {

        private final ThreadLocal<SivMode> sivModeThreadLocal = new ThreadLocal<>();
        private final HKDF hkdf;
        private final KeyManager keyManager;
        private final ByteToTextEncoding encoding;

        public AesSivEngine(KeyManager keyManager, ByteToTextEncoding encoding) {
            this.keyManager = keyManager;
            this.encoding = encoding;
            hkdf = HKDF.fromHmacSha512();
        }

        @Override
        public CharSequence mask(byte[] plainId) {
            Objects.requireNonNull(plainId, "id");

            byte[] keys = hkdf.expand(keyManager.getActiveKey().getKeyBytes(), Bytes.allocate(16).array(), 64);

            SivMode sivMode = getSiv();
            byte[] encrypted = sivMode.encrypt(
                    Bytes.from(keys, 0, 16).array(),
                    Bytes.from(keys, 16, 32).array(),
                    plainId);
            return encoding.encode(encrypted);
        }

        @Override
        public byte[] unmask(CharSequence maskedId) {
            byte[] keys = hkdf.expand(keyManager.getActiveKey().getKeyBytes(), Bytes.allocate(16).array(), 64);
            SivMode sivMode = getSiv();

            try {
                return sivMode.decrypt(
                        Bytes.from(keys, 0, 16).array(),
                        Bytes.from(keys, 16, 32).array(),
                        encoding.decode(maskedId));
            } catch (UnauthenticCiphertextException | IllegalBlockSizeException e) {
                throw new IdMaskSecurityException("could not decrypt", IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY, e);
            }
        }

        private synchronized SivMode getSiv() {
            if (sivModeThreadLocal.get() == null) {
                sivModeThreadLocal.set(new SivMode());
            }
            return sivModeThreadLocal.get();
        }
    }
}
