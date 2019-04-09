package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.util.UUID;

public class IdMaskUuidTest extends ABaseIdMaskTest {
    private IdMask<UUID> idMask = new IdMask.UuidMask(
            Config.builder()
                    .keyManager(KeyManager.Factory.with(Bytes.random(16).array()))
                    .enableCache(false)
                    .build());

    @Test
    public void testEncodeDecode() {
        for (int i = 0; i < 10; i++) {
            maskAndUnmask(idMask, UUID.randomUUID());
        }
    }

    @Test
    public void testAllByteToTextEncodings() {
        for (ByteToTextEncoding encoding : encodings) {
            maskAndUnmask(new IdMask.UuidMask(Config.builder().key(Bytes.random(16).array()).enableCache(false).encoding(encoding).build()), UUID.randomUUID());
            maskAndUnmask(new IdMask.UuidMask(Config.builder().key(Bytes.random(16).array()).randomizedIds(true).enableCache(false).encoding(encoding).build()), UUID.randomUUID());
        }
    }
}
