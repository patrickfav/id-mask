package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.*;

public class KeyManagerTest {

    @Test
    public void testSingleKeyManager() {
        KeyManager.IdSecretKey idSecretKey = new KeyManager.IdSecretKey(0, Bytes.random(16).array());
        KeyManager keyManager = KeyManager.Factory.with(idSecretKey);
        assertEquals(idSecretKey, keyManager.getActiveKey());
        assertEquals(idSecretKey, keyManager.getById(idSecretKey.getKeyId()));
        assertNull(keyManager.getById(2));
    }

    @Test
    public void testMultiKeyKeyManager() {
        KeyManager.IdSecretKey idSecretKey1 = new KeyManager.IdSecretKey(0, Bytes.random(16).array());
        KeyManager.IdSecretKey idSecretKey2 = new KeyManager.IdSecretKey(1, Bytes.random(16).array());
        KeyManager.IdSecretKey idSecretKey3 = new KeyManager.IdSecretKey(2, Bytes.random(16).array());
        KeyManager keyManager = KeyManager.Factory.withKeyAndLegacyKeys(idSecretKey1, idSecretKey2, idSecretKey3);
        assertEquals(idSecretKey1, keyManager.getActiveKey());
        assertEquals(idSecretKey1, keyManager.getById(idSecretKey1.getKeyId()));
        assertEquals(idSecretKey2, keyManager.getById(idSecretKey2.getKeyId()));
        assertEquals(idSecretKey3, keyManager.getById(idSecretKey3.getKeyId()));
        assertNull(keyManager.getById(3));
    }

    @Test
    public void testClear() {
        byte[] k1 = Bytes.random(16).array();
        byte[] k2 = Bytes.random(16).array();

        KeyManager.IdSecretKey idSecretKey1 = new KeyManager.IdSecretKey(0, Bytes.wrap(k1).copy().array());
        KeyManager.IdSecretKey idSecretKey2 = new KeyManager.IdSecretKey(1, Bytes.wrap(k2).copy().array());

        KeyManager keyManager = KeyManager.Factory.withKeyAndLegacyKeys(idSecretKey1, idSecretKey2);

        assertTrue(Bytes.wrap(k1).equals(idSecretKey1.getKeyBytes()));
        assertTrue(Bytes.wrap(k2).equals(idSecretKey2.getKeyBytes()));
        assertEquals(2, keyManager.size());

        keyManager.clear();

        assertFalse(Bytes.wrap(k1).equals(idSecretKey1.getKeyBytes()));
        assertFalse(Bytes.wrap(k2).equals(idSecretKey2.getKeyBytes()));
        assertEquals(0, keyManager.size());

        assertNull(keyManager.getActiveKey());
        assertEquals(0, keyManager.getActiveKeyId());
    }

    @Test
    public void testSimpleConstructor1() {
        KeyManager.IdSecretKey idSecretKey = new KeyManager.IdSecretKey(0, Bytes.random(16).array());
        KeyManager keyManager = KeyManager.Factory.with(idSecretKey.getKeyId(), idSecretKey.getKeyBytes());
        assertEquals(idSecretKey, keyManager.getActiveKey());
        assertEquals(idSecretKey, keyManager.getById(idSecretKey.getKeyId()));
        assertNull(keyManager.getById(2));
    }

    @Test
    public void testSimpleConstructor2() {
        KeyManager.IdSecretKey idSecretKey = new KeyManager.IdSecretKey(0, Bytes.random(16).array());

        KeyManager keyManager = KeyManager.Factory.with(idSecretKey.getKeyBytes());
        assertEquals(idSecretKey, keyManager.getActiveKey());
        assertEquals(idSecretKey, keyManager.getById(idSecretKey.getKeyId()));
        assertNull(keyManager.getById(2));
    }

    @Test
    public void testWithSecretKey() {
        byte[] key = Bytes.random(16).array();
        KeyManager.IdSecretKey idSecretKey = new KeyManager.IdSecretKey(0, key);

        KeyManager keyManager = KeyManager.Factory.with(new SecretKeySpec(key, "AES"));
        assertEquals(idSecretKey, keyManager.getActiveKey());
        assertEquals(idSecretKey, keyManager.getById(keyManager.getActiveKeyId()));
        assertNull(keyManager.getById(2));
    }

    @Test
    public void testKeyIdAlreadyAdded() {
        KeyManager.IdSecretKey idSecretKey1 = new KeyManager.IdSecretKey(0, Bytes.random(16).array());
        KeyManager.IdSecretKey idSecretKey2 = new KeyManager.IdSecretKey(0, Bytes.random(16).array());

        try {
            KeyManager.Factory.withKeyAndLegacyKeys(idSecretKey1, idSecretKey2);
            fail();
        } catch (IllegalArgumentException ignored) {

        }
    }

    @Test
    public void testCachedKdf() {
        KeyManager.IdSecretKey idSecretKey1 = new KeyManager.IdSecretKey(1, Bytes.random(16).array());
        KeyManager.IdSecretKey idSecretKey2 = new KeyManager.IdSecretKey(2, Bytes.random(16).array());

        KeyManager keyManager = new KeyManager.CachedKdfConverter(KeyManager.Factory.withKeyAndLegacyKeys(idSecretKey1, idSecretKey2),
                new KeyManager.CachedKdfConverter.KdfConverter() {
                    @Override
                    public byte[] convert(KeyManager.IdSecretKey idSecretKey) {
                        return Bytes.wrap(idSecretKey.getKeyBytes()).hashSha256().array();
                    }
                });

        assertEquals(2, keyManager.size());

        assertArrayEquals(Bytes.wrap(idSecretKey1.getKeyBytes()).hashSha256().array(), keyManager.getActiveKey().getKeyBytes());
        assertArrayEquals(Bytes.wrap(idSecretKey1.getKeyBytes()).hashSha256().array(), keyManager.getById(idSecretKey1.getKeyId()).getKeyBytes());
        assertArrayEquals(Bytes.wrap(idSecretKey2.getKeyBytes()).hashSha256().array(), keyManager.getById(idSecretKey2.getKeyId()).getKeyBytes());
        assertNull(keyManager.getById(3));

        keyManager.clear();
        assertEquals(0, keyManager.size());
    }

}
