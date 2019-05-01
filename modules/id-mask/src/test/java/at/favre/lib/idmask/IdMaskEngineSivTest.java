package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import static org.junit.Assert.*;

public class IdMaskEngineSivTest {
    private IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), new ByteToTextEncoding.Base64Url());

    @Test
    public void testFixedIdsAndKey() {
        byte[] id = Bytes.from(397849238741629487L).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), new ByteToTextEncoding.Base64Url());
        for (int i = 0; i < 10; i++) {
            CharSequence maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

//    @Test
//    public void testRandomizedShouldBeLongerThanDeterministic() {
//        KeyManager key = KeyManager.Factory.withRandom();
//        byte[] id = Bytes.from(Bytes.from(9182746139874612986L)).array();
//
//        IdMaskEngine idMaskRandomized = new IdMaskEngine.AesSivEngine(key, null, new SecureRandom(), new ByteToTextEncoding.Base64Url(), true, false);
//        IdMaskEngine idMaskDeterministic = new IdMaskEngine.AesSivEngine(key, null, new SecureRandom(), new ByteToTextEncoding.Base64Url(), false, false);
//
//        CharSequence maskedId1 = idMaskRandomized.mask(id);
//        CharSequence maskedId2 = idMaskDeterministic.mask(id);
//
//        assertNotEquals(maskedId1, maskedId2);
//        assertTrue(maskedId1.length() > maskedId2.length());
//
//        System.out.println(maskedId1);
//        System.out.println(maskedId2);
//    }

//    @Test
//    public void testRandomizedIdsShouldNotReturnSameMaskedId() {
//        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(),
//                null, new SecureRandom(), new ByteToTextEncoding.Base64Url(), true, false);
//        byte[] id = Bytes.from(7239562391234L).array();
//
//        CharSequence maskedId1 = idMaskEngine.mask(id);
//        CharSequence maskedId2 = idMaskEngine.mask(id);
//        CharSequence maskedId3 = idMaskEngine.mask(id);
//        CharSequence maskedId4 = idMaskEngine.mask(id);
//
//        assertNotEquals(maskedId1, maskedId2);
//        assertNotEquals(maskedId1, maskedId3);
//        assertNotEquals(maskedId1, maskedId4);
//        assertNotEquals(maskedId2, maskedId3);
//        assertNotEquals(maskedId3, maskedId4);
//
//        assertArrayEquals(id, idMaskEngine.unmask(maskedId1));
//        assertArrayEquals(id, idMaskEngine.unmask(maskedId2));
//        assertArrayEquals(id, idMaskEngine.unmask(maskedId3));
//        assertArrayEquals(id, idMaskEngine.unmask(maskedId4));
//
//        System.out.println(maskedId1);
//        System.out.println(maskedId2);
//        System.out.println(maskedId3);
//        System.out.println(maskedId4);
//    }

    @Test
    public void testDeterministicIdsShouldReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), new ByteToTextEncoding.Base64Url());
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
    public void testWithRandomId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), new ByteToTextEncoding.Base64Url());
        for (int i = 0; i < 10; i++) {
            byte[] id = Bytes.random(8).array();
            CharSequence maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testVariousKeyIds() {
        byte[] id = Bytes.from(93875623985763L).array();
        for (int i = 0; i < 16; i++) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.with(i, Bytes.random(16).array()), new ByteToTextEncoding.Base64Url());
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

        IdMaskEngine engine1 = new IdMaskEngine.AesSivEngine(KeyManager.Factory.with(k1), new ByteToTextEncoding.Base64Url());
        IdMaskEngine engine2 = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withKeyAndLegacyKeys(k2, k1), new ByteToTextEncoding.Base64Url());
        IdMaskEngine engine3 = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withKeyAndLegacyKeys(k3, k2, k1), new ByteToTextEncoding.Base64Url());

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

//    @Test(expected = IllegalArgumentException.class)
//    public void testUnmaskEncodedTooShort() {
//        idMaskEngine.unmask("1234567");
//    }

//    @Test(expected = IllegalArgumentException.class)
//    public void testUnmaskEncodedTooLong() {
//        idMaskEngine.unmask(Bytes.allocate(IdMaskEngine.BaseEngine.MAX_MASKED_ID_ENCODED_LENGTH / 2).encodeHex());
//    }

    @Test
    public void testForgeryAttempt1() {
        byte[] id = Bytes.from(103876123987523049L).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), new ByteToTextEncoding.Base64Url());
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
        IdMaskEngine idMaskEngine = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), new ByteToTextEncoding.Base64Url());
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
            new IdMaskEngine.EightByteEncryptionEngine(KeyManager.Factory.with(16, Bytes.random(16).array()));
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testIncorrectKey() {
        KeyManager manager1 = KeyManager.Factory.with(0, Bytes.random(16).array());
        IdMaskEngine idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(manager1);
        byte[] id = Bytes.from(509186513786L).array();
        CharSequence masked = idMaskEngine.mask(id);

        KeyManager manager2 = KeyManager.Factory.with(0, Bytes.random(16).array());
        idMaskEngine = new IdMaskEngine.EightByteEncryptionEngine(manager2);

        try {
            idMaskEngine.unmask(masked);
            fail();
        } catch (IdMaskSecurityException e) {
            assertEquals(IdMaskSecurityException.Reason.AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY, e.getReason());
        }
    }

//    @Test
//    public void testWrongId() {
//        try {
//            idMaskEngine.unmask("MB8GIdO1rkNLN88yCLaxB_U");
//            fail();
//        } catch (IdMaskSecurityException e) {
//            assertEquals(IdMaskSecurityException.Reason.UNKNOWN_ENGINE_ID, e.getReason());
//        }
//    }

//    @Test
//    public void testAutoWipeMemory() {
//        byte[] id = Bytes.from(397849238741625487L).array();
//        IdMaskEngine idMaskRandomized = new IdMaskEngine.AesSivEngine(KeyManager.Factory.withRandom(), null, new SecureRandom(), new ByteToTextEncoding.Base64Url(), true, true);
//        for (int i = 0; i < 10; i++) {
//            CharSequence maskedId = idMaskRandomized.mask(id);
//            assertNotNull(maskedId);
//            assertArrayEquals(id, idMaskRandomized.unmask(maskedId));
//        }
//    }
}
