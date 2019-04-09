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

        IdMask<Long> idMask = IdMasks.forLongIds(Config.builder(key).build());

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

        IdMask<UUID> idMask = IdMasks.forUuids(Config.builder(key).build());

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
                Config.builder(KeyManager.Factory.with(key))
                        .randomizedIds(true) //non-deterministic output
                        .enableCache(true)
                        .cacheImpl(new Cache.SimpleLruMemCache(64))
                        .encoding(new ByteToTextEncoding.Base32Rfc4648())
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

    @Test
    public void upgradeKey() {
        long id = 123456789L;
        byte[] key1 = Bytes.random(16).array();

        // create new instance with your key
        IdMask<Long> idMask1 = IdMasks.forLongIds(Config.builder(key1).build());
        String maskKey1 = idMask1.mask(id);
        // e.g.: kpKOdqrNdmyx34-VxjTg6B4
        System.out.println(maskKey1);

        // if key1 is compromised create a new key
        byte[] key2 = Bytes.random(16).array();

        // set the new key as active key and add the old key as legacy key - us the DEFAULT_KEY_ID, is it is used if no key id is set
        IdMask<Long> idMask2 = IdMasks.forLongIds(
                Config.builder(KeyManager.Factory.withKeyAndLegacyKeys(
                        new KeyManager.IdSecretKey(1, key2), //new key with a new key id
                        new KeyManager.IdSecretKey(KeyManager.Factory.DEFAULT_KEY_ID, key1))) //old key with the DEFAULT_KEY_ID
                        .build());

        // same id will create different output
        String maskKey2 = idMask2.mask(id);
        // e.g.: 3c1UMVvVK5SvNiOaT4btpiQ
        System.out.println(maskKey2);

        // the new instance can unmask the old an new key
        assert idMask2.unmask(maskKey1).equals(idMask2.unmask(maskKey2));
    }
}
