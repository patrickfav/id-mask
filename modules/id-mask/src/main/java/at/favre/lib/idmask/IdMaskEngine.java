package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.bytes.BytesTransformer;
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

    abstract class BaseEngine {
        private ThreadLocal<Cipher> cipherWrapper = new ThreadLocal<>();
        final Provider provider;
        final SecureRandom secureRandom;
        final ByteToTextEncoding encoding;
        final HKDF hkdf;
        final byte[] internalKey;

        BaseEngine(byte[] key, Provider provider, SecureRandom secureRandom, ByteToTextEncoding encoding) {
            this.hkdf = HKDF.fromHmacSha512();
            this.provider = provider;
            this.secureRandom = secureRandom;
            this.encoding = Objects.requireNonNull(encoding, "encoding");
            this.internalKey = hkdf.extract(null, key);
        }

        byte[] getRandomBytes(int size) {
            byte[] rnd = new byte[size];
            secureRandom.nextBytes(rnd);
            return rnd;
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

        protected abstract String getCipherAlgorithm();

    }

    final class EightByteEncryptionEngine extends BaseEngine implements IdMaskEngine {
        private static final String ALGORITHM = "AES/ECB/NoPadding";
        private static final int LENGTH = 8;

        EightByteEncryptionEngine(byte[] key) {
            super(key, null, new SecureRandom(), new ByteToTextEncoding.Base64());
        }

        EightByteEncryptionEngine(byte[] key, Provider provider, SecureRandom secureRandom, ByteToTextEncoding encoding) {
            super(key, provider, secureRandom, encoding);
        }

        @Override
        public String mask(byte[] id) {
            if (id.length != LENGTH) {
                throw new IllegalArgumentException("input must be 8 byte long");
            }

            byte[] random = getRandomBytes(LENGTH);
            byte[] message = Bytes.wrap(random).append(id).array();
            SecretKey secretKey = new SecretKeySpec(Bytes.wrap(internalKey).resize(16, BytesTransformer.ResizeTransformer.Mode.RESIZE_KEEP_FROM_ZERO_INDEX).array(), "AES");
            try {

                Cipher c = getCipher();
                c.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] cipherText = c.doFinal(message);

                ByteBuffer bb = ByteBuffer.allocate(LENGTH * 3);
                bb.put(random);
                bb.put(cipherText);

                return encoding.encode(bb.array());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public byte[] unmask(String maskedId) {
            Objects.requireNonNull(maskedId, "maskedId");

            ByteBuffer bb = ByteBuffer.wrap(encoding.decode(maskedId));

            if (bb.remaining() != 3 * LENGTH) {
                throw new IllegalArgumentException("invalid message length");
            }

            byte[] random = new byte[LENGTH];
            bb.get(random);
            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);
            try {
                SecretKey secretKey = new SecretKeySpec(Bytes.from(internalKey, 0, 16).array(), "AES");
                Cipher c = getCipher();
                c.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] message = c.doFinal(cipherText);

                if (!Bytes.from(message, 0, LENGTH).equalsConstantTime(random)) {
                    throw new SecurityException("internal random does not match, probably forgery attempt");
                }

                return Bytes.from(message, 8, LENGTH).array();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        protected String getCipherAlgorithm() {
            return ALGORITHM;
        }
    }

    final class SixteenByteEngine extends BaseEngine implements IdMaskEngine {
        private static final String ALGORITHM = "AES/CBC/NoPadding";
        private static final String HMAC_ALGORITHM = "HmacSHA256";
        private static final int MAX_ID_LENGTH = 16;

        //private final Mode mode;

        private Mac hmac;

        public SixteenByteEngine(byte[] key, Mode mode) {
            this(key, mode, new ByteToTextEncoding.Base64(), new SecureRandom(), null);
        }

        public SixteenByteEngine(byte[] key, Mode mode, ByteToTextEncoding encoding, SecureRandom secureRandom, Provider provider) {
            super(key, provider, secureRandom, encoding);
            //this.mode = Objects.requireNonNull(mode, "mode");
        }

        @Override
        public String mask(byte[] id) {
            Objects.requireNonNull(id, "id");

            if (id.length != MAX_ID_LENGTH) {
                throw new IllegalArgumentException(String.format("id length must be between 1 and %d bytes", MAX_ID_LENGTH));
            }

            try {
                byte[] entropy = getRandomBytes(16);
                byte[] keys = hkdf.expand(internalKey, entropy, 64);

                byte[] currentKey = Bytes.from(keys, 0, 16).array();
                byte[] iv = Bytes.from(keys, 16, 16).array();
                byte[] macKey = Bytes.from(keys, 32, 32).array();

                Cipher cipher = getCipher();
                cipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(currentKey, "AES"),
                        new IvParameterSpec(iv));
                byte[] encryptedId = cipher.doFinal(Bytes.from(id).xor(entropy).array());
                byte version = (byte) 0x01;
                byte[] mac = Bytes.from(macCipherText(macKey, encryptedId, iv, new byte[]{version}), 0, 16).array();

                ByteBuffer bb = ByteBuffer.allocate(1 + encryptedId.length + mac.length + entropy.length);
                bb.put(obfuscateVersion((byte) 0x01, Bytes.from(keys, 16, 1).array()));
                bb.put(entropy);
                bb.put(encryptedId);
                bb.put(mac);

                return encoding.encode(bb.array());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        private byte obfuscateVersion(byte b, byte[] entropy) {
            return (byte) (b ^ entropy[0]);
        }

        @Override
        public byte[] unmask(String maskedId) {
            Objects.requireNonNull(maskedId, "maskedId");

            ByteBuffer bb = ByteBuffer.wrap(encoding.decode(maskedId));
            byte version = bb.get();
            byte[] entropy = new byte[16];
            bb.get(entropy);
            byte[] payload = new byte[16];
            bb.get(payload);
            byte[] mac = new byte[16];
            bb.get(mac);

            byte[] keys = hkdf.expand(internalKey, entropy, 64);

            byte[] currentKey = Bytes.from(keys, 0, 16).array();
            byte[] iv = Bytes.from(keys, 16, 16).array();
            byte[] macKey = Bytes.from(keys, 32, 32).array();

            version = obfuscateVersion(version, Bytes.from(keys, 16, 1).array());
            byte[] refMac = Bytes.from(macCipherText(macKey, payload, iv, new byte[]{version}), 0, 16).array();

            if (!Bytes.wrap(mac).equalsConstantTime(refMac)) {
                throw new SecurityException("mac does not match");
            }
            try {
                Cipher cipher = getCipher();
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(currentKey, "AES"), new IvParameterSpec(iv));
                return Bytes.wrap(cipher.doFinal(payload)).xor(entropy).array();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
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

    }
}
