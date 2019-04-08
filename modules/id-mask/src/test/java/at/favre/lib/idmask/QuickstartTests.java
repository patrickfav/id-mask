package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.security.SecureRandom;
import java.security.Security;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class QuickstartTests {

    @Test
    public void quickstartLong() {
        byte[] key = Bytes.random(16).array();
        long id = new Random().nextLong();

        IdMask<Long> idMask = IdMasks.forLongIds(Config.builder().key(key).build());

        String maskedId = idMask.mask(id);
        //example: wMDHT8QN6Ljyko3ma5QLEIE
        long originalId = idMask.unmask(maskedId);

        assertEquals(id, originalId);
        System.out.println(maskedId);
    }

    @Test
    public void quickstartUUID() {
        byte[] key = Bytes.random(16).array();
        UUID id = UUID.fromString("eb1c6999-5fc1-4d5f-b98a-792949c38c45");

        IdMask<UUID> idMask = IdMasks.forUuids(Config.builder().key(key).build());

        String maskedId = idMask.mask(id);
        //example: rK0wpnG1lwvG0xiZn5swxOYmAvxhA4A7yg
        UUID originalId = idMask.unmask(maskedId);

        assertEquals(id, originalId);
        System.out.println(maskedId);
    }

    @Test
    public void fullExample() {
        byte[] key = Bytes.random(16).array();
        byte[] id128bit = Bytes.random(16).array();

        IdMask<byte[]> idMask = IdMasks.for128bitNumbers(
                Config.builder()
                        .keyManager(KeyManager.Factory.with(key))
                        .randomizedIds(true) //non-deterministic output
                        .enableCache(true)
                        .cacheImpl(new Cache.SimpleLruMemCache(64))
                        .encoding(new ByteToTextEncoding.Base32())
                        .secureRandom(new SecureRandom())
                        .securityProvider(Security.getProvider("BC"))
                        .build());

        String maskedId = idMask.mask(id128bit);
        //example: RAKESQ32V37ORV5JX7FAWCFVV2PITRN5KMOKYBJBLNS42WCEA3FI2FIBXLKJGMTSZY
        byte[] originalId = idMask.unmask(maskedId);

        assertArrayEquals(id128bit, originalId);
        System.out.println(maskedId);
        System.out.println(Bytes.wrap(originalId).encodeHex());
    }
}
