package at.favre.lib.idmask.ref;

import at.favre.lib.bytes.Bytes;
import at.favre.lib.idmask.ByteToTextEncoding;
import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMask;
import at.favre.lib.idmask.IdMasks;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public final class IdMaskRefConfigs {
    private static final Random rnd = new Random(19827362L);

    static final IdMask<Long> idMaskLongRefConfig1 = IdMasks.forLongIds(
            Config.builder(Bytes.parseHex("2a5a967e4669510560b73ce5c026d5f5").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .build());

    static final IdMask<Long> idMaskLongRefConfig2 = IdMasks.forLongIds(
            Config.builder(Bytes.parseHex("dbcaf04d9b58d32d2360727e").array())
                    .encoding(new ByteToTextEncoding.Base32Rfc4648())
                    .build());

    static final IdMask<Long> idMaskLongRefConfig3 = IdMasks.forLongIds(
            Config.builder(Bytes.parseHex("00112233445566778899aabbccddeeff").array())
                    .encoding(new ByteToTextEncoding.Base16())
                    .build());

    static final IdMask<Long> idMaskLongRefConfig4Random = IdMasks.forLongIds(
            Config.builder(Bytes.parseHex("b4015f53d4ae626aa66e40b271f603e3").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .randomizedIds(true)
                    .build());

    static final IdMask<UUID> idMaskUuidRefConfig1 = IdMasks.forUuids(
            Config.builder(Bytes.parseHex("2a5a967e4669510560b73ce5c026d5f5").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .build());
    static final IdMask<UUID> idMaskUuidRefConfig2 = IdMasks.forUuids(
            Config.builder(Bytes.parseHex("dbcaf04d9b58d32d2360727e").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .build());
    static final IdMask<UUID> idMaskUuidRefConfig3 = IdMasks.forUuids(
            Config.builder(Bytes.parseHex("00112233445566778899aabbccddeeff").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .build());
    static final IdMask<UUID> idMaskUuidRefConfig4Random = IdMasks.forUuids(
            Config.builder(Bytes.parseHex("c1a85ba166254220a6bcc110fca932b9").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .randomizedIds(true)
                    .build());
    static final IdMask<UUID> idMaskUuidRefConfig5HighSecurity = IdMasks.forUuids(
            Config.builder(Bytes.parseHex("62ce5d63824b697e17bbd3d778467af9").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .highSecurityMode(true)
                    .build());

    static final IdMask<BigInteger> idMaskBigIntegerRefConfig = IdMasks.forBigInteger(
            Config.builder(Bytes.parseHex("3d6c822e98047306cbee02b7b6a9a0e9").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .build());

    static final IdMask<byte[]> idMaskByteArrayRefConfig = IdMasks.for128bitNumbers(
            Config.builder(Bytes.parseHex("2dabb10b91aa663b852f5b3a0f4fbf0d").array())
                    .encoding(new ByteToTextEncoding.Base64Url())
                    .build());

    private static final List<IdMask<Long>> allLongConfigs = Arrays.asList(idMaskLongRefConfig1, idMaskLongRefConfig2, idMaskLongRefConfig3);
    private static final List<IdMask<UUID>> allUuidConfigs = Arrays.asList(idMaskUuidRefConfig1, idMaskUuidRefConfig2, idMaskUuidRefConfig3, idMaskUuidRefConfig5HighSecurity);

    @Test
    public void printLongReferenceTests() {
        for (IdMask<Long> config : allLongConfigs) {
            System.out.println();
            for (int i = -2; i < 16; i++) {
                Long id = i > 2 ? Bytes.random(8).toLong() : i;
                printLongReg(config, id);
            }
        }
    }

    private void printLongReg(IdMask<Long> config, Long id) {
        String masked = config.mask(id);
        String sb = "new Ref<>(" +
                id + "L, \"" + masked + "\"" +
                "),";
        System.out.println(sb);
    }

    @Test
    public void printUUIDReferenceTests() {
        for (IdMask<UUID> config : allUuidConfigs) {
            System.out.println();
            for (int i = 0; i < 16; i++) {
                UUID id = UUID.randomUUID();
                printUuidRef(config, id);
            }
        }
    }

    private void printUuidRef(IdMask<UUID> config, UUID id) {
        String masked = config.mask(id);
        String sb = "new Ref<>(" +
                "UUID.fromString(\"" + id + "\"), \"" + masked + "\"" +
                "),";
        System.out.println(sb);
    }

    @Test
    public void printRandomizedRef() {
        for (int i = -2; i < 3; i++) {
            printLongReg(idMaskLongRefConfig4Random, (long) i);
        }
        for (int i = 0; i < 4; i++) {
            printUuidRef(idMaskUuidRefConfig4Random, UUID.randomUUID());
        }
    }

    @Test
    public void printBigIntegerReferenceTests() {
        System.out.println();
        for (int i = 0; i < 8; i++) {
            BigInteger id = new BigInteger(Bytes.random(rnd.nextInt(14) + 1).array());
            printBigInteger(idMaskBigIntegerRefConfig, id);
        }
    }

    private void printBigInteger(IdMask<BigInteger> config, BigInteger id) {
        String masked = config.mask(id);
        String sb = "new Ref<>(" +
                "new BigInteger(\"" + id.toString() + "\"), \"" + masked + "\"" +
                "),";
        System.out.println(sb);
    }

    @Test
    public void printByteArrayReferenceTests() {
        System.out.println();
        for (int i = 0; i < 8; i++) {
            byte[] id = Bytes.random(16).array();
            printByteArrayRef(idMaskByteArrayRefConfig, id);
        }
    }

    private void printByteArrayRef(IdMask<byte[]> config, byte[] id) {
        String masked = config.mask(id);
        String sb = "new Ref<>(" +
                "Bytes.parseHex(\"" + Bytes.wrap(id).encodeHex() + "\").array(), \"" + masked + "\"" +
                "),";
        System.out.println(sb);
    }
}
