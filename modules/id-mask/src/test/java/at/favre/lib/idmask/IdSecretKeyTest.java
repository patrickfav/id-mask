package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class IdSecretKeyTest {

    @Test
    public void testCreateValidKeys() {
        for (int i = 8; i < 65; i++) {
            for (int keyId = 0; keyId < 15; keyId++) {
                new KeyManager.IdSecretKey(keyId, Bytes.random(i).array());
            }
        }
    }

    @Test
    public void testIdKeyEqualsAndHashcode() {
        byte[] random = Bytes.random(16).array();
        byte[] randomRef = Bytes.wrap(random).copy().array();
        final int id = 2;
        KeyManager.IdSecretKey idSecretKey = new KeyManager.IdSecretKey(id, random);
        assertArrayEquals(randomRef, idSecretKey.getKeyBytes());
        assertEquals(id, idSecretKey.getKeyId());

        assertEquals(idSecretKey.hashCode(), new KeyManager.IdSecretKey(id, random).hashCode());
        assertEquals(idSecretKey, new KeyManager.IdSecretKey(id, random));

        idSecretKey.clear();

        assertFalse(Bytes.wrap(randomRef).equals(idSecretKey.getKeyBytes()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyBytesTooSmall() {
        new KeyManager.IdSecretKey(1, Bytes.random(7).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyIdTooSmall() {
        new KeyManager.IdSecretKey(-1, Bytes.random(16).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyBytesTooBig() {
        new KeyManager.IdSecretKey(1, Bytes.random(65).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeyIdTooBig() {
        new KeyManager.IdSecretKey(16, Bytes.random(16).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOnlyZeros() {
        new KeyManager.IdSecretKey(1, Bytes.allocate(8).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy() {
        new KeyManager.IdSecretKey(1, Bytes.from(4L).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy2() {
        new KeyManager.IdSecretKey(1, Bytes.from(4564L).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy3() {
        new KeyManager.IdSecretKey(1, Bytes.from(984654163L).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLowEntropy4() {
        new KeyManager.IdSecretKey(1, Bytes.from(15678654163L).array());
    }
}
