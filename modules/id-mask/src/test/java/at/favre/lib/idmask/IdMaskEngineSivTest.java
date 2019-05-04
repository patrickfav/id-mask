package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.bytes.BytesTransformer;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.*;

public class IdMaskEngineSivTest {
    private IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);

    @Test
    public void testFixedIdsAndKey() {
        byte[] id = Bytes.from(397849238741629487L).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);

        CharSequence maskedId = idMaskEngine.mask(id);
        assertNotNull(maskedId);
        assertArrayEquals(id, idMaskEngine.unmask(maskedId));
        System.out.println(maskedId);
    }

    @Test
    public void testAllIdLengthConfigsWithRandomId() {
        for (IdMaskEngine.AesSivEngine.IdEncConfig config : IdMaskEngine.AesSivEngine.IdEncConfig.values()) {
            for (int i = 0; i < 3; i++) {
                byte[] id = Bytes.random(config.valueLengthBytes).array();
                IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), config);
                CharSequence maskedId = idMaskEngine.mask(id);
                assertNotNull(maskedId);
                assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                System.out.println(maskedId);
            }
        }
    }

    @Test
    public void testIncrementingIds() {
        for (IdMaskEngine.AesSivEngine.IdEncConfig config : IdMaskEngine.AesSivEngine.IdEncConfig.values()) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), config);
            CharSequence maskedIdBefore = null;
            for (int i = 0; i < 16; i++) {
                byte[] id = Bytes.from(i).resize(config.valueLengthBytes, BytesTransformer.ResizeTransformer.Mode.RESIZE_KEEP_FROM_MAX_LENGTH).array();
                CharSequence maskedId = idMaskEngine.mask(id);
                assertNotNull(maskedId);
                assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                assertNotEquals(maskedIdBefore, maskedId);
                maskedIdBefore = maskedId;
                System.out.println(String.format("%2d", i) + ": " + maskedId);
            }
        }
    }

    @Test
    public void testRandomizedShouldBeLongerThanDeterministic() {
        KeyManager key = KeyManager.Factory.withRandom();
        byte[] id = Bytes.from(Bytes.from(9182746139874612986L)).array();

        IdMaskEngine idMaskRandomized = new IdMaskEngine.AesSivEngine(key, IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE, Bytes.allocate(8).array(), new ByteToTextEncoding.Base64Url(), true, new SecureRandom(), null);
        IdMaskEngine idMaskDeterministic = new IdMaskEngine.AesSivEngine(key, IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE, Bytes.allocate(8).array(), new ByteToTextEncoding.Base64Url(), false, new SecureRandom(), null);

        CharSequence maskedId1 = idMaskRandomized.mask(id);
        CharSequence maskedId2 = idMaskDeterministic.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertTrue(maskedId1.length() > maskedId2.length());

        System.out.println(maskedId1);
        System.out.println(maskedId2);
    }

    @Test
    public void testRandomizedIdsShouldNotReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(),
                IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE, Bytes.allocate(8).array(), new ByteToTextEncoding.Base64Url(), true, new SecureRandom(), null);
        byte[] id = Bytes.from(7239562391234L).array();

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

        System.out.println(maskedId1);
        System.out.println(maskedId2);
        System.out.println(maskedId3);
        System.out.println(maskedId4);
    }

    @Test
    public void testDeterministicIdsShouldReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
        byte[] id = Bytes.from(7239562391234L).array();

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

        System.out.println(maskedId1);
        System.out.println(maskedId2);
        System.out.println(maskedId3);
        System.out.println(maskedId4);
    }

    @Test
    public void testVariousKeyIds() {
        byte[] id = Bytes.from(93875623985763L).array();
        for (int i = 0; i < 16; i++) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.with(i, Bytes.random(16).array()), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
            CharSequence maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testMultiKeySupport() {
        byte[] id = Bytes.from(1L).array();

        KeyManager.IdSecretKey k1 = new KeyManager.IdSecretKey(0, Bytes.random(16).array());
        KeyManager.IdSecretKey k2 = new KeyManager.IdSecretKey(1, Bytes.random(16).array());
        KeyManager.IdSecretKey k3 = new KeyManager.IdSecretKey(2, Bytes.random(16).array());

        IdMaskEngine engine1 = new IdMaskEngine.AesSivEngine(KeyManager.Factory.with(k1), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
        IdMaskEngine engine2 = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withKeyAndLegacyKeys(k2, k1), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
        IdMaskEngine engine3 = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withKeyAndLegacyKeys(k3, k2, k1), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);

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
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.UNKNOWN_KEY_ID, e.getReason());
        }
        assertArrayEquals(id, engine2.unmask(maskedId2));
        assertArrayEquals(id, engine3.unmask(maskedId2));

        // raw must be the same for all engines supporting key3
        try {
            assertArrayEquals(id, engine1.unmask(maskedId3));
            fail();
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.UNKNOWN_KEY_ID, e.getReason());
        }
        try {
            assertArrayEquals(id, engine2.unmask(maskedId3));
            fail();
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.UNKNOWN_KEY_ID, e.getReason());
        }
        assertArrayEquals(id, engine3.unmask(maskedId3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLongId1() {
        new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_16_BYTE).mask(Bytes.allocate(5).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLongId2() {
        idMaskEngine.mask(Bytes.allocate(7).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLongId3() {
        new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_16_BYTE).mask(Bytes.allocate(17).array());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLongId4() {
        new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_32_BYTE).mask(Bytes.allocate(33).array());
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
        idMaskEngine.unmask(Bytes.allocate(IdMaskEngine.AesSivEngine.MAX_MASKED_ID_ENCODED_LENGTH / 2 + 1).encodeHex());
    }

    @Test
    public void testForgeryAttempt1() {
        byte[] id = Bytes.from(103876123987523049L).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
        CharSequence maskedId = idMaskEngine.mask(id);

        byte[] raw = new ByteToTextEncoding.Base64Url().decode(maskedId);

        raw[2] = (byte) (raw[2] ^ 0xe2);

        CharSequence forged = new ByteToTextEncoding.Base64Url().encode(raw);
        try {
            idMaskEngine.unmask(forged);
            fail();
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY, e.getReason());
        }
    }

    @Test
    public void testForgeryAttempt2() {
        byte[] id = Bytes.from(70366123987523049L).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
        CharSequence maskedId = idMaskEngine.mask(id);

        byte[] raw = new ByteToTextEncoding.Base64Url().decode(maskedId);

        raw[14] = (byte) (raw[14] ^ 0xe2);

        CharSequence forged = new ByteToTextEncoding.Base64Url().encode(raw);
        try {
            idMaskEngine.unmask(forged);
            fail();
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY, e.getReason());
        }
    }

    @Test
    public void testKeyIdTooBig() {
        try {
            new IdMaskEngine.AesSivEngine(KeyManager.Factory.with(16, Bytes.random(16).array()), IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testIncorrectKey() {
        KeyManager manager1 = KeyManager.Factory.with(0, Bytes.random(16).array());
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(manager1, IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);
        byte[] id = Bytes.from(509186513786L).array();
        CharSequence masked = idMaskEngine.mask(id);

        KeyManager manager2 = KeyManager.Factory.with(0, Bytes.random(16).array());
        idMaskEngine = new IdMaskEngine.AesSivEngine(manager2, IdMaskEngine.AesSivEngine.IdEncConfig.INTEGER_8_BYTE);

        try {
            idMaskEngine.unmask(masked);
            fail();
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY, e.getReason());
        }
    }

    @Test
    public void testWrongId() {
        try {
            idMaskEngine.unmask("HyFTDJKjEQHz75GQ4F-SUMXtMnAbfPVI3Q");
            fail();
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.UNKNOWN_ENGINE_ID, e.getReason());
        }
    }

//    @Test
//    public void testAutoWipeMemory() {
//        byte[] id = Bytes.from(397849238741625487L).array();
//        IdMaskEngine idMaskRandomized = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), new ByteToTextEncoding.Base64Url(), true, new SecureRandom(), null);
//        for (int i = 0; i < 10; i++) {
//            CharSequence maskedId = idMaskRandomized.mask(id);
//            assertNotNull(maskedId);
//            assertArrayEquals(id, idMaskRandomized.unmask(maskedId));
//        }
//    }
}
