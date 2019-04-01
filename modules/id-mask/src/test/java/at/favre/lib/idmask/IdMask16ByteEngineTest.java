package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class IdMask16ByteEngineTest {
    private IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.random(16).array(), Mode.MEDIUM_SIZE_AND_SECURITY);

    @Test
    public void testAllModes() {
        byte[] id = Bytes.from(Bytes.from(UUID.randomUUID())).array();
        for (Mode mode : Mode.values()) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.from(192731092837120938L).array(), mode);
            for (int i = 0; i < 10; i++) {
                String maskedId = idMaskEngine.mask(id);
                assertNotNull(maskedId);
                assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                System.out.println(maskedId);
            }
        }
    }

    @Test
    public void testShouldNotReturnSameMaskedId() {
        IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.from(130984671309784536L).array(), Mode.LARGE_SIZE_AND_HIGH_SECURITY);
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
    public void testAllModesWithRandomId() {
        for (Mode mode : Mode.values()) {
            IdMaskEngine idMaskEngine = new IdMaskEngine.SixteenByteEngine(Bytes.random(16).array(), mode);
            for (int i = 0; i < 10; i++) {
                byte[] id = Bytes.from(UUID.randomUUID()).array();
                String maskedId = idMaskEngine.mask(id);
                assertNotNull(maskedId);
                assertArrayEquals(id, idMaskEngine.unmask(maskedId));
                System.out.println(maskedId);
            }
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
}
