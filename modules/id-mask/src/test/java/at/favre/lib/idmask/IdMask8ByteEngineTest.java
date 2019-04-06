package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.*;

public class IdMask8ByteEngineTest {
    private IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.withRandom());

    @Test
    public void testFixedIdsAndKey() {
        byte[] id = Bytes.from(397849238741629487L).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.with(192731092837120938L));
        for (int i = 0; i < 10; i++) {
            String maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testRandomizedShouldBeLongerThanDeterministic() {
        KeyManager key = KeyManager.Factory.with(87587659785921233L);
        byte[] id = Bytes.from(Bytes.from(9182746139874612986L)).array();

        IdMaskEngine idMaskRandomized = new IdMaskEngine.EightByteEncryptionEngine(key, null, new SecureRandom(), new ByteToTextEncoding.Base64(), true);
        IdMaskEngine idMaskDeterministic = new IdMaskEngine.EightByteEncryptionEngine(key, null, new SecureRandom(), new ByteToTextEncoding.Base64(), false);

        String maskedId1 = idMaskRandomized.mask(id);
        String maskedId2 = idMaskDeterministic.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertTrue(maskedId1.length() > maskedId2.length());

        System.out.println(maskedId1);
        System.out.println(maskedId2);
    }

    @Test
    public void testRandomizedIdsShouldNotReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.with(130984671309784536L),
                null, new SecureRandom(), new ByteToTextEncoding.Base64(), true);
        byte[] id = Bytes.from(7239562391234L).array();

        String maskedId1 = idMaskEngine.mask(id);
        String maskedId2 = idMaskEngine.mask(id);
        String maskedId3 = idMaskEngine.mask(id);
        String maskedId4 = idMaskEngine.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertNotEquals(maskedId1, maskedId3);
        assertNotEquals(maskedId1, maskedId4);
        assertNotEquals(maskedId2, maskedId3);
        assertNotEquals(maskedId3, maskedId4);

        assertArrayEquals(id, idMaskEngine.unmask(maskedId1));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId2));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId3));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId4));

        System.out.println(maskedId1);
        System.out.println(maskedId2);
        System.out.println(maskedId3);
        System.out.println(maskedId4);
    }

    @Test
    public void testDeterministicIdsShouldReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.with(130984671309784536L),
                null, new SecureRandom(), new ByteToTextEncoding.Base64(), false);
        byte[] id = Bytes.from(7239562391234L).array();

        String maskedId1 = idMaskEngine.mask(id);
        String maskedId2 = idMaskEngine.mask(id);
        String maskedId3 = idMaskEngine.mask(id);
        String maskedId4 = idMaskEngine.mask(id);

        assertEquals(maskedId1, maskedId2);
        assertEquals(maskedId1, maskedId3);
        assertEquals(maskedId1, maskedId4);
        assertEquals(maskedId2, maskedId3);
        assertEquals(maskedId3, maskedId4);

        assertArrayEquals(id, idMaskEngine.unmask(maskedId1));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId2));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId3));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId4));

        System.out.println(maskedId1);
        System.out.println(maskedId2);
        System.out.println(maskedId3);
        System.out.println(maskedId4);
    }

    @Test
    public void testWithRandomId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.withRandom());
        for (int i = 0; i < 10; i++) {
            byte[] id = Bytes.random(8).array();
            String maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testVariousKeyIds() {
        byte[] id = Bytes.from(93875623985763L).array();
        for (int i = 0; i < 16; i++) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.with(i, Bytes.random(16).array()));
            String maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testMultiKeySupport() {
        byte[] id = Bytes.from(1L).array();

        KeyManager.IdKey k1 = new KeyManager.IdKey(0, Bytes.random(16).array());
        KeyManager.IdKey k2 = new KeyManager.IdKey(1, Bytes.random(16).array());
        KeyManager.IdKey k3 = new KeyManager.IdKey(2, Bytes.random(16).array());

        IdMaskEngine engine1 = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.with(k1));
        IdMaskEngine engine2 = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.withKeyAndLegacyKeys(k2, k1));
        IdMaskEngine engine3 = new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.withKeyAndLegacyKeys(k3, k2, k1));

        // encrypt with 3 different keys, having backwards compatibility
        String maskedId1 = engine1.mask(id);
        String maskedId2 = engine2.mask(id);
        String maskedId3 = engine3.mask(id);

        // encryption with different key must be different
        assertNotEquals(maskedId1, maskedId2);
        assertNotEquals(maskedId2, maskedId3);

        // raw id must be same because all engines support key1
        assertArrayEquals(id, engine1.unmask(maskedId1));
        assertArrayEquals(id, engine2.unmask(maskedId1));
        assertArrayEquals(id, engine3.unmask(maskedId1));

        // raw must be the same for all engines supporting key2
        try {
            assertArrayEquals(id, engine1.unmask(maskedId2));
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertArrayEquals(id, engine2.unmask(maskedId2));
        assertArrayEquals(id, engine3.unmask(maskedId2));

        // raw must be the same for all engines supporting key3
        try {
            assertArrayEquals(id, engine1.unmask(maskedId3));
            fail();
        } catch (IllegalStateException ignored) {
        }
        try {
            assertArrayEquals(id, engine2.unmask(maskedId3));
            fail();
        } catch (IllegalStateException ignored) {
        }
        assertArrayEquals(id, engine3.unmask(maskedId3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLongId() {
        idMaskEngine.mask(Bytes.allocate(17).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testZeroLengthId() {
        idMaskEngine.mask(Bytes.empty().array());
    }

    @Test(expected = NullPointerException.class)
    public void testMaskNullInput() {
        idMaskEngine.mask(null);
    }

    @Test(expected = NullPointerException.class)
    public void testUnmaskNullInput() {
        idMaskEngine.unmask(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmaskEncodedTooShort() {
        idMaskEngine.unmask("1234567");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnmaskEncodedTooLong() {
        idMaskEngine.unmask(Bytes.allocate(IdMaskEngine.BaseEngine.MAX_MASKED_ID_ENCODED_LENGTH / 2).encodeHex());
    }
}
