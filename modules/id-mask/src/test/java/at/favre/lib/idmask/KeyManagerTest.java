package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
}
