package at.favre.lib.idmask;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class IdMaskSecurityExceptionTest {

    @Test
    public void getReason() {
        for (IdMaskSecurityException.Reason reason : IdMaskSecurityException.Reason.values()) {
            assertEquals(reason, new IdMaskSecurityException("test", reason).getReason());

        }
    }

    @Test
    public void getMessage() {
        for (IdMaskSecurityException.Reason reason : IdMaskSecurityException.Reason.values()) {
            assertNotNull(new IdMaskSecurityException("test message", reason).getMessage());
        }
    }

    @Test(expected = IdMaskSecurityException.class)
    public void testConstructors1() {
        throw new IdMaskSecurityException("test", IdMaskSecurityException.Reason.UNKNOWN_KEY_ID);
    }

    @Test(expected = IdMaskSecurityException.class)
    public void testConstructors2() {
        throw new IdMaskSecurityException("test", IdMaskSecurityException.Reason.UNKNOWN_KEY_ID, new IllegalStateException());
    }
}
