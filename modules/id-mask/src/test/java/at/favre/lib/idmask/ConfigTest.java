package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;
import org.junit.Test;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void testDefaults() {
        Config config = Config.builder(KeyManager.Factory.withRandom()).build();
        assertTrue(config.enableCache());
        assertFalse(config.randomizedIds());
        assertFalse(config.highSecurityMode());
        assertNull(config.securityProvider());
        assertEquals(ByteToTextEncoding.Base64Url.class.getName(), config.encoding().getClass().getName());
    }

    @Test
    public void testConfigs() {
        ByteToTextEncoding encoding = new ByteToTextEncoding.Base32Rfc4648();
        KeyManager keyManager = KeyManager.Factory.withRandom();
        Provider provider = Security.getProvider("BC");
        Cache cache = new Cache.SimpleLruMemCache();
        SecureRandom secureRandom = new SecureRandom();

        Config config = Config.builder(keyManager)
                .encoding(encoding)
                .enableCache(false)
                .randomizedIds(true)
                .securityProvider(provider)
                .cacheImpl(cache)
                .highSecurityMode(true)
                .secureRandom(secureRandom)
                .build();

        assertSame(keyManager, config.keyManager());
        assertSame(encoding, config.encoding());
        assertSame(cache, config.cacheImpl());
        assertSame(cache, config.cacheImpl());
        assertSame(provider, config.securityProvider());
        assertSame(secureRandom, config.secureRandom());
        assertFalse(config.enableCache());
        assertFalse(config.enableCache());
        assertTrue(config.randomizedIds());
        assertTrue(config.highSecurityMode());
    }

    @Test
    public void testUsingKeyShorthand() {
        byte[] key = Bytes.random(16).array();
        Config config = Config.builder(key).build();
        assertArrayEquals(key, config.keyManager().getActiveKey().getKeyBytes());
    }
}
