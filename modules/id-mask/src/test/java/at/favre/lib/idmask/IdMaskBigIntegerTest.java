package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IdMaskBigIntegerTest extends ABaseIdMaskTest {
    private IdMask<BigInteger> idMask = new IdMask.BigIntegerIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(false).build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            maskAndUnmask(idMask, BigInteger.valueOf(Bytes.random(8).toLong()));
        }
    }

    @Test
    public void testEncodeTooLongBigInteger() {
        BigInteger id = Bytes.random(16).toBigInteger();
        try {
            idMask.mask(id);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testWithCache() {
        IdMask<BigInteger> idMask = new IdMask.BigIntegerIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(true).build());
        BigInteger id = BigInteger.valueOf(Bytes.random(8).toLong());
        String encoded = idMask.mask(id);

        for (int i = 0; i < 10000; i++) {
            BigInteger refId = idMask.unmask(encoded);
            assertEquals(id, refId);
        }
    }

    @Test
    public void testWithoutCache() {
        IdMask<BigInteger> idMask = new IdMask.BigIntegerIdMask(Config.builder().keyManager(KeyManager.Factory.with(Bytes.random(16).array())).enableCache(false).build());
        BigInteger id = BigInteger.valueOf(Bytes.random(8).toLong());
        String encoded = idMask.mask(id);

        for (int i = 0; i < 100; i++) {
            BigInteger refId = idMask.unmask(encoded);
            assertEquals(id, refId);
        }
    }

    @Test
    public void testEncodeDecodeSimpleBigInteger() {
        maskAndUnmask(idMask, BigInteger.ZERO);
        maskAndUnmask(idMask, BigInteger.ONE);
        maskAndUnmask(idMask, BigInteger.TEN);
    }

    @Test
    public void testEncodeDecodeNegativeBigInteger() {
        maskAndUnmask(idMask, BigInteger.valueOf(-208495723905824L));
    }

    @Test
    public void testEncodeDecodeMaxLengthBigInteger() {
        maskAndUnmask(idMask, Bytes.random(15).toBigInteger());
    }
}
