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

    byte[] unmask(String maskedId);

    String mask(byte[] id);

    final class Default implements IdMaskEngine {
        private static final String ALGORITHM = "AES/CTR/NoPadding";
        private static final String HMAC_ALGORITHM = "HmacSHA256";
        private static final int MAX_ID_LENGTH = 16;

        private final Provider provider;
        private final SecureRandom secureRandom;
        private final HKDF hkdf;
        private final ByteToTextEncoding encoding;

        private final Mode mode;
        private final byte[] internalKey;

        private Mac hmac;
        private ThreadLocal<Cipher> cipherWrapper = new ThreadLocal<>();

        public Default(byte[] key, Mode mode) {
            this(key, mode, new ByteToTextEncoding.Base64());
        }

        public Default(byte[] key, Mode mode, ByteToTextEncoding encoding) {
            this.secureRandom = new SecureRandom();
            this.provider = null;
            this.encoding = Objects.requireNonNull(encoding, "encoding");
            this.mode = Objects.requireNonNull(mode, "mode");
            this.hkdf = HKDF.fromHmacSha512();
            this.internalKey = hkdf.extract(null, key);
        }

        @Override
        public String mask(byte[] id) {
            Objects.requireNonNull(id, "id");

            if (id.length > MAX_ID_LENGTH) {
                throw new IllegalArgumentException(String.format("id must be longer than %d bytes", MAX_ID_LENGTH));
            }

            try {
                byte[] entropy = getRandomBytes(mode.getEntropyByteLength());
                byte[] keys = hkdf.expand(internalKey, entropy, 64);

                byte[] currentKey = Bytes.from(keys, 0, 16).array();
                byte[] iv = Bytes.from(keys, 16, 16).array();
                byte[] macKey = Bytes.from(keys, 32, 32).array();

                Cipher cipher = getCipher();
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(currentKey, "AES"), new IvParameterSpec(iv));
                byte[] encryptedId = cipher.doFinal(Bytes.from(id).array());
                byte version = (byte) 0x01;
                byte[] mac = Bytes.from(macCipherText(macKey, encryptedId, iv, new byte[]{version}), 0, mode.getMacByteLength()).array();

                ByteBuffer bb = ByteBuffer.allocate(1 + id.length + mode.getMacByteLength() + mode.getEntropyByteLength());
                bb.put(obfuscateVersion((byte) 0x01, entropy));
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

        private byte[] getRandomBytes(int size) {
            byte[] rnd = new byte[size];
            secureRandom.nextBytes(rnd);
            return rnd;
        }

        @Override
        public byte[] unmask(String maskedId) {
            Objects.requireNonNull(maskedId, "maskedId");

            ByteBuffer bb = ByteBuffer.wrap(encoding.decode(maskedId));
            byte version = bb.get();
            byte[] entropy = new byte[mode.getEntropyByteLength()];
            byte[] mac = new byte[mode.getMacByteLength()];
            byte[] payload = new byte[bb.remaining() - (mode.getMacByteLength() + mode.getEntropyByteLength())];
            bb.get(entropy);
            bb.get(payload);
            bb.get(mac);

            version = obfuscateVersion(version, entropy);

            byte[] keys = hkdf.expand(internalKey, entropy, 64);

            byte[] currentKey = Bytes.from(keys, 0, 16).array();
            byte[] iv = Bytes.from(keys, 16, 16).array();
            byte[] macKey = Bytes.from(keys, 32, 32).array();

            byte[] refMac = Bytes.from(macCipherText(macKey, payload, iv, new byte[]{version}), 0, mode.getMacByteLength()).array();

            if (!Bytes.wrap(mac).equalsConstantTime(refMac)) {
                throw new SecurityException("mac does not match");
            }
            try {
                Cipher cipher = getCipher();
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(currentKey, "AES"), new IvParameterSpec(iv));
                return cipher.doFinal(payload);
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

        private synchronized Cipher getCipher() {
            Cipher cipher = cipherWrapper.get();
            if (cipher == null) {
                try {
                    if (provider != null) {
                        cipher = Cipher.getInstance(ALGORITHM, provider);
                    } else {
                        cipher = Cipher.getInstance(ALGORITHM);
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

    }
}
