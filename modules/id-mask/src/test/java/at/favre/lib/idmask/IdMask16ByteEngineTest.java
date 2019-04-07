package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.UUID;

import static org.junit.Assert.*;

public class IdMask16ByteEngineTest {
    private IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.withRandom());

    @Test
    public void testMediumSecurityMode() {
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(192731092837120938L),
                false, new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, true);

        for (int i = 0; i < 10; i++) {
            CharSequence maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testHighSecurityMode() {
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(192731092837120938L),
                true, new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, true);

        for (int i = 0; i < 10; i++) {
            CharSequence maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testHighSecurityModeShouldBeLongerThanMediumSecurity() {
        KeyManager keyManager = KeyManager.Factory.with(Bytes.from(192731092837120938L).array());
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();

        IdMaskEngine idMaskEngineHighSecurity = new IdMaskEngine.SixteenByteEngine(keyManager, true,
                new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, false);

        IdMaskEngine idMaskEngineMediumSecurity = new IdMaskEngine.SixteenByteEngine(keyManager, false,
                new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, false);

        CharSequence maskedId1 = idMaskEngineHighSecurity.mask(id);
        CharSequence maskedId2 = idMaskEngineMediumSecurity.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertTrue(maskedId1.length() > maskedId2.length());

        System.out.println(maskedId1);
        System.out.println(maskedId2);
    }

    @Test
    public void testRandomizedShouldBeLongerThanDeterministic() {
        KeyManager keyManager = KeyManager.Factory.with(Bytes.from(245789624908756240L).array());
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();

        IdMaskEngine idMaskRandomized = new IdMaskEngine.SixteenByteEngine(keyManager, false,
                new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, true);

        IdMaskEngine idMaskDeterministic = new IdMaskEngine.SixteenByteEngine(keyManager, false,
                new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, false);

        CharSequence maskedId1 = idMaskRandomized.mask(id);
        CharSequence maskedId2 = idMaskDeterministic.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertTrue(maskedId1.length() > maskedId2.length());

        System.out.println(maskedId1);
        System.out.println(maskedId2);
    }

    @Test
    public void testRandomizedShouldNotReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(130984671309784536L),
                true, new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, true);
        byte[] id = Bytes.from(UUID.randomUUID()).array();

        CharSequence maskedId1 = idMaskEngine.mask(id);
        CharSequence maskedId2 = idMaskEngine.mask(id);
        CharSequence maskedId3 = idMaskEngine.mask(id);
        CharSequence maskedId4 = idMaskEngine.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertNotEquals(maskedId1, maskedId3);
        assertNotEquals(maskedId1, maskedId4);
        assertNotEquals(maskedId2, maskedId3);
        assertNotEquals(maskedId3, maskedId4);

        assertArrayEquals(id, idMaskEngine.unmask(maskedId1));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId2));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId3));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId4));
    }

    @Test
    public void testDeterministicShouldReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(130984671309784536L),
                true, new ByteToTextEncoding.Base64Url(), new SecureRandom(), null, false);
        byte[] id = Bytes.from(UUID.randomUUID()).array();

        CharSequence maskedId1 = idMaskEngine.mask(id);
        CharSequence maskedId2 = idMaskEngine.mask(id);
        CharSequence maskedId3 = idMaskEngine.mask(id);
        CharSequence maskedId4 = idMaskEngine.mask(id);

        assertEquals(maskedId1, maskedId2);
        assertEquals(maskedId1, maskedId3);
        assertEquals(maskedId1, maskedId4);
        assertEquals(maskedId2, maskedId3);
        assertEquals(maskedId3, maskedId4);

        assertArrayEquals(id, idMaskEngine.unmask(maskedId1));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId2));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId3));
        assertArrayEquals(id, idMaskEngine.unmask(maskedId4));
    }

    @Test
    public void testWithRandomId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.withRandom());
        for (int i = 0; i < 10; i++) {
            byte[] id = Bytes.from(UUID.randomUUID()).array();
            CharSequence maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testVariousKeyIds() {
        byte[] id = Bytes.random(16).array();
        for (int i = 0; i < 16; i++) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(i, Bytes.random(16).array()));
            CharSequence maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testMultiKeySupport() {
        byte[] id = Bytes.random(16).array();

        KeyManager.IdSecretKey k1 = new KeyManager.IdSecretKey(0, Bytes.random(16).array());
        KeyManager.IdSecretKey k2 = new KeyManager.IdSecretKey(1, Bytes.random(16).array());
        KeyManager.IdSecretKey k3 = new KeyManager.IdSecretKey(2, Bytes.random(16).array());

        IdMaskEngine engine1 = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(k1));
        IdMaskEngine engine2 = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.withKeyAndLegacyKeys(k2, k1));
        IdMaskEngine engine3 = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.withKeyAndLegacyKeys(k3, k2, k1));

        // encrypt with 3 different keys, having backwards compatibility
        CharSequence maskedId1 = engine1.mask(id);
        CharSequence maskedId2 = engine2.mask(id);
        CharSequence maskedId3 = engine3.mask(id);

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

    @Test
    public void testForgeryAttemptWithIncorrectMac() {
        byte[] id = Bytes.random(16).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(KeyManager.Factory.with(83785623198457L));
        CharSequence maskedId = idMaskEngine.mask(id);

        byte[] raw = new ByteToTextEncoding.Base64Url().decode(maskedId);

        raw[20] = (byte) (raw[20] ^ 0xe2);

        CharSequence forged = new ByteToTextEncoding.Base64Url().encode(raw);
        try {
            idMaskEngine.unmask(forged);
            fail();
        } catch (SecurityException ignored) {
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLongId() {
        idMaskEngine.mask(Bytes.allocate(17).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooShortId() {
        idMaskEngine.mask(Bytes.allocate(15).array());
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
