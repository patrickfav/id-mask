package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.crypto.HKDF;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Objects;

public interface IdMaskEngine {

    String mask(byte[] id);

    byte[] unmask(String maskedId);

    int MAX_ENGINE_ID = 0x0F; //15
    int MAX_KEY_ID = 0x0F; //15

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
        final int supportedIdByteLength;

        BaseEngine(int supportedIdByteLength, KeyManager keyManager, Provider provider, SecureRandom secureRandom, ByteToTextEncoding encoding, boolean randomizeIds) {
            this.hkdf = HKDF.fromHmacSha512();
            this.provider = provider;
            this.secureRandom = secureRandom;
            this.encoding = Objects.requireNonNull(encoding, "encoding");
            this.keyManager = KeyManager.CachedValueConverter.wrap(keyManager, new KeyManager.CachedValueConverter.ValueConverter() {
                @Override
                public byte[] convert(KeyManager.IdKey original) {
                    return hkdf.extract(Bytes.from(original.getKeyId()).array(), original.getKeyBytes());
                }
            });
            this.randomizeIds = randomizeIds;
            this.supportedIdByteLength = supportedIdByteLength;
        }

        int getSupportedIdByteLength() {
            return supportedIdByteLength;
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

        void checkInput(String maskedId) {
            if (Objects.requireNonNull(maskedId, "maskedId").length() > MAX_MASKED_ID_ENCODED_LENGTH || maskedId.length() < MIN_MASKED_ID_ENCODED_LENGTH) {
                throw new IllegalArgumentException("encoded masked id too long or short, must be between " + MIN_MASKED_ID_ENCODED_LENGTH + " and " + MAX_MASKED_ID_ENCODED_LENGTH + " chars");
            }
        }

        protected abstract String getCipherAlgorithm();

        protected abstract byte engineId();

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
            KeyManager.IdKey key;
            if ((key = keyManager.getById(keyId)) == null) {
                return null;
            }
            return key.getKeyBytes();
        }

        byte[] checkAndGetCurrentKey(byte version, byte[] cipherText) {
            if (getEngineIdFromVersion(version, cipherText) != engineId()) {
                throw new SecurityException("wrong id-engine used according to version byte");
            }

            byte[] currentSecretKey = getKeyForId(getKeyIdFromVersion(version, cipherText));

            if (currentSecretKey == null) {
                throw new IllegalStateException("unknown key id");
            }
            return currentSecretKey;
        }
    }

    final class EightByteEncryptionEngine extends BaseEngine implements IdMaskEngine {
        private static final String ALGORITHM = "AES/ECB/NoPadding";
        private static final int ENGINE_ID = 0;

        EightByteEncryptionEngine(KeyManager keyManager) {
            this(keyManager, null, new SecureRandom(), new ByteToTextEncoding.Base64(), false);
        }

        public EightByteEncryptionEngine(KeyManager keyManager, Provider provider, SecureRandom secureRandom, ByteToTextEncoding encoding, boolean randomizeIds) {
            super(8, keyManager, provider, secureRandom, encoding, randomizeIds);
        }

        @Override
        public String mask(byte[] id) {
            if (id.length != getSupportedIdByteLength()) {
                throw new IllegalArgumentException("input must be 8 byte long");
            }

            byte[] random = getEntropyBytes(getSupportedIdByteLength());
            byte[] message = Bytes.wrap(random).append(id).array();
            SecretKey secretKey = new SecretKeySpec(Bytes.from(getCurrentIdKey(), 0, 16).array(), "AES");

            try {
                Cipher c = getCipher();
                c.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] cipherText = c.doFinal(message);

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

                return encoding.encode(bb.array());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public byte[] unmask(String maskedId) {
            checkInput(maskedId);

            ByteBuffer bb = ByteBuffer.wrap(encoding.decode(maskedId));

            if (bb.remaining() != 1 + (randomizeIds ? 3 : 2) * getSupportedIdByteLength()) {
                throw new IllegalArgumentException("unexpected message id length " + bb.remaining());
            }

            byte version = bb.get();

            final byte[] entropyData = getEntropyBytes(getSupportedIdByteLength());
            if (randomizeIds) {
                bb.get(entropyData);
            }

            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);

            byte[] currentSecretKey = checkAndGetCurrentKey(version, cipherText);

            try {
                SecretKey secretKey = new SecretKeySpec(Bytes.from(currentSecretKey, 0, 16).array(), "AES");
                Cipher c = getCipher();
                c.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] message = c.doFinal(cipherText);

                if (!Bytes.from(message, 0, getSupportedIdByteLength()).equalsConstantTime(entropyData)) {
                    throw new SecurityException("internal reference entropy does not match, probably forgery attempt");
                }

                return Bytes.from(message, 8, getSupportedIdByteLength()).array();
            } catch (Exception e) {
                throw new IllegalStateException(e);
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

    final class SixteenByteEngine extends BaseEngine implements IdMaskEngine {
        private static final String ALGORITHM = "AES/CBC/NoPadding";
        private static final String HMAC_ALGORITHM = "HmacSHA256";
        private static final int MAC_LENGTH_SHORT = 8;
        private static final int MAC_LENGTH_LONG = 16;
        private static final int ENGINE_ID = 1;

        private final boolean highSecurityMode;

        private Mac hmac;

        SixteenByteEngine(KeyManager keyManager) {
            this(keyManager, false, new ByteToTextEncoding.Base64(), new SecureRandom(), null, false);
        }

        public SixteenByteEngine(KeyManager keyManager, boolean highSecurityMode, ByteToTextEncoding encoding, SecureRandom secureRandom, Provider provider, boolean randomizeIds) {
            super(16, keyManager, provider, secureRandom, encoding, randomizeIds);
            this.highSecurityMode = highSecurityMode;
        }

        @Override
        public String mask(byte[] id) {
            Objects.requireNonNull(id, "id");

            if (id.length != getSupportedIdByteLength()) {
                throw new IllegalArgumentException(String.format("id length must be between 1 and %d bytes", getSupportedIdByteLength()));
            }

            try {
                byte[] entropy = getEntropyBytes(getSupportedIdByteLength());
                byte[] keys = hkdf.expand(getCurrentIdKey(), entropy, 64);

                byte[] currentKey = Bytes.from(keys, 0, 16).array();
                byte[] iv = Bytes.from(keys, 16, 16).array();
                byte[] macKey = Bytes.from(keys, 32, 32).array();

                Cipher cipher = getCipher();
                cipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(currentKey, "AES"),
                        new IvParameterSpec(iv));
                byte[] encryptedId = cipher.doFinal(Bytes.from(id).xor(entropy).array());
                byte version = createVersionByte((byte) keyManager.getActiveKeyId(), encryptedId);
                byte[] mac = Bytes.from(macCipherText(macKey, encryptedId, iv, new byte[]{version}), 0, getMacLength()).array();

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
            }
        }

        @Override
        public byte[] unmask(String maskedId) {
            checkInput(maskedId);

            ByteBuffer bb = ByteBuffer.wrap(encoding.decode(maskedId));

            checkDecodedLength(bb.remaining());

            byte version = bb.get();
            byte[] entropy = getEntropyBytes(getSupportedIdByteLength());
            if (randomizeIds) {
                bb.get(entropy);
            }
            byte[] cipherText = new byte[getSupportedIdByteLength()];
            bb.get(cipherText);

            byte[] mac = new byte[getMacLength()];
            bb.get(mac);

            byte[] currentSecretKey = checkAndGetCurrentKey(version, cipherText);

            byte[] keys = hkdf.expand(currentSecretKey, entropy, 64);

            byte[] currentKey = Bytes.from(keys, 0, 16).array();
            byte[] iv = Bytes.from(keys, 16, 16).array();
            byte[] macKey = Bytes.from(keys, 32, 32).array();

            byte[] refMac = Bytes.from(macCipherText(macKey, cipherText, iv, new byte[]{version}), 0, getMacLength()).array();
            if (!Bytes.wrap(mac).equalsConstantTime(refMac)) {
                throw new SecurityException("mac does not match");
            }
            try {
                Cipher cipher = getCipher();
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(currentKey, "AES"), new IvParameterSpec(iv));
                return Bytes.wrap(cipher.doFinal(cipherText)).xor(entropy).array();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private void checkDecodedLength(int size) {
            int expectedLength = 1 + getSupportedIdByteLength() + (randomizeIds ? getSupportedIdByteLength() : 0) + getMacLength();
            if (size != expectedLength) {
                throw new IllegalArgumentException("unexpected message id length " + size);
            }
        }

        private int getMacLength() {
            return highSecurityMode ? MAC_LENGTH_LONG : MAC_LENGTH_SHORT;
        }

        private byte[] macCipherText(byte[] rawEncryptionKey, byte[] cipherText, byte[] iv, byte[] associatedData) {
            SecretKey macKey = createMacKey(rawEncryptionKey);

            try {
                createHmacInstance();
                hmac.init(macKey);
                hmac.update(iv);
                hmac.update(cipherText);
            } catch (InvalidKeyException e) {
                // due to key generation in createMacKey(byte[]) this actually can not happen
                throw new IllegalStateException("error during HMAC calculation");
            }

            if (associatedData != null) {
                hmac.update(associatedData);
            }

            return hmac.doFinal();
        }

        private SecretKey createMacKey(byte[] rawEncryptionKey) {
            byte[] derivedMacKey = HKDF.fromHmacSha256().expand(rawEncryptionKey, Bytes.from("macKey").array(), 32);
            return new SecretKeySpec(derivedMacKey, HMAC_ALGORITHM);
        }

        private synchronized void createHmacInstance() {
            if (hmac == null) {
                try {
                    if (provider != null) {
                        hmac = Mac.getInstance(HMAC_ALGORITHM, provider);
                    } else {
                        hmac = Mac.getInstance(HMAC_ALGORITHM);
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("could not get cipher instance", e);
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
}
