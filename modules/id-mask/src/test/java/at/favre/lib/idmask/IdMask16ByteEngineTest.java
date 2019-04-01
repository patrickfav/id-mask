package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.UUID;

import static org.junit.Assert.*;

public class IdMask16ByteEngineTest {
    private IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.random(16).array());

    @Test
    public void testMediumSecurityMode() {
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.from(192731092837120938L).array(), false,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, true);

        for (int i = 0; i < 10; i++) {
            String maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testHighSecurityMode() {
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.from(192731092837120938L).array(), true,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, true);

        for (int i = 0; i < 10; i++) {
            String maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
        }
    }

    @Test
    public void testHighSecurityModeShouldBeLongerThanMediumSecurity() {
        byte[] key = Bytes.from(192731092837120938L).array();
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();

        IdMaskEngine idMaskEngineHighSecurity = new IdMaskEngine.SixteenByteEngine(key, true,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, false);

        IdMaskEngine idMaskEngineMediumSecurity = new IdMaskEngine.SixteenByteEngine(key, false,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, false);

        String maskedId1 = idMaskEngineHighSecurity.mask(id);
        String maskedId2 = idMaskEngineMediumSecurity.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertTrue(maskedId1.length() > maskedId2.length());

        System.out.println(maskedId1);
        System.out.println(maskedId2);
    }

    @Test
    public void testRandomizedShouldBeLongerThanDeterministic() {
        byte[] key = Bytes.from(245789624908756240L).array();
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();

        IdMaskEngine idMaskRandomized = new IdMaskEngine.SixteenByteEngine(key, false,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, true);

        IdMaskEngine idMaskDeterministic = new IdMaskEngine.SixteenByteEngine(key, false,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, false);

        String maskedId1 = idMaskRandomized.mask(id);
        String maskedId2 = idMaskDeterministic.mask(id);

        assertNotEquals(maskedId1, maskedId2);
        assertTrue(maskedId1.length() > maskedId2.length());

        System.out.println(maskedId1);
        System.out.println(maskedId2);
    }

    @Test
    public void testRandomizedShouldNotReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.from(130984671309784536L).array(), true,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, true);
        byte[] id = Bytes.from(UUID.randomUUID()).array();

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
    }

    @Test
    public void testDeterministicShouldReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.from(130984671309784536L).array(), true,
                new ByteToTextEncoding.Base64(), new SecureRandom(), null, false);
        byte[] id = Bytes.from(UUID.randomUUID()).array();

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
    }

    @Test
    public void testWithRandomId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.random(16).array());
        for (int i = 0; i < 10; i++) {
            byte[] id = Bytes.from(UUID.randomUUID()).array();
            String maskedId = idMaskEngine.mask(id);
            assertNotNull(maskedId);
            assertArrayEquals(id, idMaskEngine.unmask(maskedId));
            System.out.println(maskedId);
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
