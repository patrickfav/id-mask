package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class KeyManagerTest {

    @Test
    public void testSingleKeyManager() {
        KeyManager.IdKey idKey = new KeyManager.IdKey(0, Bytes.random(16).array());
        KeyManager keyManager = KeyManager.Factory.with(idKey);
        assertEquals(idKey, keyManager.getActiveKey());
        assertEquals(idKey, keyManager.getById(idKey.getKeyId()));
        assertNull(keyManager.getById(2));
    }

    @Test
    public void testMultiKeyKeyManager() {
        KeyManager.IdKey idKey1 = new KeyManager.IdKey(0, Bytes.random(16).array());
        KeyManager.IdKey idKey2 = new KeyManager.IdKey(1, Bytes.random(16).array());
        KeyManager.IdKey idKey3 = new KeyManager.IdKey(2, Bytes.random(16).array());
        KeyManager keyManager = KeyManager.Factory.withKeyAndLegacyKeys(idKey1, idKey2, idKey3);
        assertEquals(idKey1, keyManager.getActiveKey());
        assertEquals(idKey1, keyManager.getById(idKey1.getKeyId()));
        assertEquals(idKey2, keyManager.getById(idKey2.getKeyId()));
        assertEquals(idKey3, keyManager.getById(idKey3.getKeyId()));
        assertNull(keyManager.getById(3));
    }

    @Test
    public void testSimpleConstructor1() {
        KeyManager.IdKey idKey = new KeyManager.IdKey(0, Bytes.random(16).array());
        KeyManager keyManager = KeyManager.Factory.with(idKey.getKeyId(), idKey.getKeyBytes());
        assertEquals(idKey, keyManager.getActiveKey());
        assertEquals(idKey, keyManager.getById(idKey.getKeyId()));
        assertNull(keyManager.getById(2));
    }

    @Test
    public void testSimpleConstructor2() {
        KeyManager.IdKey idKey = new KeyManager.IdKey(0, Bytes.random(16).array());

        KeyManager keyManager = KeyManager.Factory.with(idKey.getKeyBytes());
        assertEquals(idKey, keyManager.getActiveKey());
        assertEquals(idKey, keyManager.getById(idKey.getKeyId()));
        assertNull(keyManager.getById(2));
    }

    @Test
    public void testWithSecretKey() {
        byte[] key = Bytes.random(16).array();
        KeyManager.IdKey idKey = new KeyManager.IdKey(0, key);

        KeyManager keyManager = KeyManager.Factory.with(new SecretKeySpec(key, "AES"));
        assertEquals(idKey, keyManager.getActiveKey());
        assertEquals(idKey, keyManager.getById(keyManager.getActiveKeyId()));
        assertNull(keyManager.getById(2));
    }
}
