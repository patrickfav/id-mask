package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class IdKeyTest {

    @Test
    public void testCreateValidKeys() {
        for (int i = 8; i < 65; i++) {
            for (int keyId = 0; keyId < 15; keyId++) {
                new KeyManager.IdKey(keyId, Bytes.random(i).array());
            }
        }
    }

    @Test
    public void testIdKeyEqualsAndHashcode() {
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

    @Test(expected = IllegalArgumentException.class)
    public void testKeyBytesTooSmall() {
        new KeyManager.IdKey(1, Bytes.random(7).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyIdTooSmall() {
        new KeyManager.IdKey(-1, Bytes.random(16).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyBytesTooBig() {
        new KeyManager.IdKey(1, Bytes.random(65).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyIdTooBig() {
        new KeyManager.IdKey(16, Bytes.random(16).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnlyZeros() {
        new KeyManager.IdKey(1, Bytes.allocate(8).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy() {
        new KeyManager.IdKey(1, Bytes.from(4L).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy2() {
        new KeyManager.IdKey(1, Bytes.from(4564L).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy3() {
        new KeyManager.IdKey(1, Bytes.from(984654163L).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy4() {
        new KeyManager.IdKey(1, Bytes.from(15678654163L).array());
    }
}
