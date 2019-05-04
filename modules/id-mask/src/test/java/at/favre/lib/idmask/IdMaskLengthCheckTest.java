package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class IdMaskLengthCheckTest {

    private final List<ByteToTextEncoding> encodingList;

    public IdMaskLengthCheckTest() {
        encodingList = new ArrayList<>();
        encodingList.add(new ByteToTextEncoding.Base16());
        encodingList.add(new ByteToTextEncoding.Base32Rfc4648());
        encodingList.add(new ByteToTextEncoding.Base64Url());
    }

    @Test
    public void testAllConfigsAndOutputLength() {
        KeyManager keyManager = KeyManager.Factory.withRandom();
        for (ByteToTextEncoding encoding : encodingList) {
            System.out.println("\n----------------------------------------");
            System.out.println("Encoding: " + encoding.getClass().getSimpleName() + "\n");
            boolean randomized = true;
            for (int i = 0; i < 2; i++) {
                randomized = !randomized;
                System.out.println("Randomized: " + randomized);
                for (IdMaskEngine.AesSivEngine.IdEncConfig config : IdMaskEngine.AesSivEngine.IdEncConfig.values()) {
                    IdMaskEngine engine = new IdMaskEngine.AesSivEngine(keyManager, config, Bytes.allocate(8).array(), encoding, randomized, new SecureRandom(), null);
                    byte[] id = Bytes.random(config.valueLengthBytes).array();
                    String masked = engine.mask(id).toString();
                    System.out.println(String.format("Config: %15s - Length: %2d - %s", config, masked.length(), masked));
                }
                System.out.println();
            }

        }
    }
}
