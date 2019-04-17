package at.favre.lib.idmask.ext;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMask;
import at.favre.lib.idmask.IdMasks;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.ext.ParamConverter;
import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.*;

public class IdMaskParamConvertersTest {
    private IdMask<Long> longIdMask;
    private IdMask<UUID> uuidIdMask;
    private IdMask<BigInteger> bigIntegerIdMask;

    @Before
    public void setup() {
        longIdMask = IdMasks.forLongIds(Config.builder(Bytes.random(16).array()).build());
        uuidIdMask = IdMasks.forUuids(Config.builder(Bytes.random(16).array()).build());
        bigIntegerIdMask = IdMasks.forBigInteger(Config.builder(Bytes.random(16).array()).build());
    }

    @Test
    public void testMaskedLongIdParamConverter() {
        testParamConverter(new IdMaskParamConverters.IdMaskLongIdParamConverter(longIdMask), Bytes.random(8).toLong());
    }

    @Test
    public void testLongIdParamConverter() {
        testParamConverter(new IdMaskParamConverters.IdMaskLongIdParamConverter(longIdMask), Bytes.random(8).toLong());
    }

    @Test
    public void testUuidParamConverter() {
        testParamConverter(new IdMaskParamConverters.IdMaskUuidParamConverter(uuidIdMask), UUID.randomUUID());
    }

    @Test
    public void testBigIntegerParamConverter() {
        testParamConverter(new IdMaskParamConverters.IdMaskBigIntegerParamConverter(bigIntegerIdMask), Bytes.random(3).toBigInteger());
        testParamConverter(new IdMaskParamConverters.IdMaskBigIntegerParamConverter(bigIntegerIdMask), Bytes.random(7).toBigInteger());
        testParamConverter(new IdMaskParamConverters.IdMaskBigIntegerParamConverter(bigIntegerIdMask), Bytes.random(12).toBigInteger());
    }

    private <T> void testParamConverter(ParamConverter<T> paramConverter, T id) {
        String maskedId = paramConverter.toString(id);
        assertEquals(id, paramConverter.fromString(maskedId));
    }

    @Test
    public void testNullHandling() {
        _testNullHandling(new IdMaskParamConverters.IdMaskLongIdParamConverter(longIdMask));
        _testNullHandling(new IdMaskParamConverters.IdMaskMaskedLongIdParamConverter(longIdMask));
        _testNullHandling(new IdMaskParamConverters.IdMaskUuidParamConverter(uuidIdMask));
        _testNullHandling(new IdMaskParamConverters.IdMaskBigIntegerParamConverter(bigIntegerIdMask));
    }

    private <T> void _testNullHandling(ParamConverter<T> paramConverter) {
        assertNull(paramConverter.fromString(null));
        assertNull(paramConverter.toString(null));
    }

    @Test
    public void testLongWrapper() {
        IdMaskParamConverters.MaskedLongId id1 = new IdMaskParamConverters.MaskedLongId(1L);
        IdMaskParamConverters.MaskedLongId id1a = new IdMaskParamConverters.MaskedLongId(1L);
        IdMaskParamConverters.MaskedLongId id2 = new IdMaskParamConverters.MaskedLongId(2L);

        assertNotEquals(id1, id2);
        assertNotEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1.getId(), id2.getId());
        assertNotEquals(id1.toString(), id2.toString());

        assertEquals(id1, id1a);
        assertEquals(id1.hashCode(), id1a.hashCode());
        assertEquals(id1.getId(), id1a.getId());
        assertEquals(id1.toString(), id1a.toString());
    }
}
