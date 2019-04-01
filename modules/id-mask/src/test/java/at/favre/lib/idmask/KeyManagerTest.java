package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class KeyManagerTest {

    @Test
    public void testSingleKeyManager() {
        KeyManager.IdKey idKey = new KeyManager.IdKey(0, Bytes.random(16).array());
        KeyManager keyManager = KeyManager.Factory.withKey(idKey);
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
    public void testIdKey() {
        byte[] random = Bytes.random(16).array();
        byte[] randomRef = Bytes.wrap(random).copy().array();
        final int id = 2;
        KeyManager.IdKey idKey = new KeyManager.IdKey(id, random);
        assertArrayEquals(randomRef, idKey.getKeyBytes());
        assertEquals(id, idKey.getKeyId());

        assertEquals(idKey.hashCode(), new KeyManager.IdKey(id, random).hashCode());
        assertEquals(idKey, new KeyManager.IdKey(id, random));

        idKey.clear();

        assertFalse(Bytes.wrap(randomRef).equals(idKey.getKeyBytes()));
    }
}
