package at.favre.lib.idmask;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.Nullable;

import java.security.Provider;
import java.security.SecureRandom;

/**
 * Configuration to create a new {@link IdMask} instance.
 */
@AutoValue
public abstract class Config {

    /**
     * Used byte to text encoding.
     *
     * @return encoding
     */
    abstract ByteToTextEncoding encoding();

    /**
     * If the masking creates non-deterministic ids (different output for every call)
     *
     * @return if non-deterministic
     */
    abstract boolean randomizedIds();

    /**
     * If better security settings should be used sacrificing output size and / or performance
     *
     * @return if high security mode is on
     */
    abstract boolean highSecurityMode();

    /**
     * The key manager responsible for providing the secret keys for the cryptographic primitives
     *
     * @return key manager
     */
    abstract KeyManager keyManager();

    /**
     * Optional custom JCA security provider
     *
     * @return provider instance
     */
    @Nullable
    abstract Provider securityProvider();

    /**
     * The cryptographically secure pseudorandom number generator used for generating randoms during masking process
     *
     * @return random generator
     */
    abstract SecureRandom secureRandom();

    /**
     * Cache implementation used to cache if enabled.
     *
     * @return cache impl
     */
    abstract Cache cacheImpl();

    /**
     * If internal caching should be enabled
     *
     * @return if caching is enabled
     */
    abstract boolean enableCache();

    /**
     * Wipes memory of security relevant date (e.g. byte arrays)
     * in the expensive of making the process a bit slower.
     *
     * @return if auto wipe is enabled
     */
    abstract boolean autoWipeMemory();

    /**
     * Creates a new build with the following defaults:
     *
     * <ul>
     * <li>Base64 encoding</li>
     * <li>Default security provider</li>
     * <li>Using in-memory-lru cache and enables it</li>
     * <li>Default secure random</li>
     * <li>Deterministic ids &amp; high security mode disabled</li>
     * </ul>
     *
     * @param keyManager non-null key manager responsible for providing the secret keys for the cryptographic primitives. If only a single key is used:'KeyManager.Factory.with(secretKey);'
     * @return builder
     */
    public static Builder builder(KeyManager keyManager) {
        return new AutoValue_Config.Builder()
                .keyManager(keyManager)
                .encoding(new ByteToTextEncoding.Base64Url())
                .highSecurityMode(false)
                .randomizedIds(false)
                .securityProvider(null)
                .cacheImpl(new Cache.SimpleLruMemCache())
                .enableCache(true)
                .autoWipeMemory(false)
                .secureRandom(new SecureRandom());
    }

    /**
     * Create new builder. Shorthand for calling {@link #builder(KeyManager)} with single key key-manager.
     *
     * @param key to use as secret key.
     * @return builder
     */
    public static Builder builder(byte[] key) {
        return builder(KeyManager.Factory.with(key));
    }

    /**
     * Class for creating configurations for IdMas. See {@link #builder(KeyManager)}.
     */
    @AutoValue.Builder
    public abstract static class Builder {

        /**
         * The key manager responsible for providing the secret keys for the cryptographic primitives.
         * <p>
         * If only a single key is used:
         * <pre>
         *    KeyManager.Factory.with(secretKey);
         * </pre>
         *
         * @param keyManager to use (non-optional)
         * @return builder
         */
        abstract Builder keyManager(KeyManager keyManager);

        /**
         * Used byte to text encoding.
         * <p>
         * Implementations for Base64, Base32Rfc4648 and Hex are available. You may provide your own
         * however.
         * <p>
         * E.g.
         * <pre>
         *    new ByteToTextEncoding.Base32Rfc4648()
         * </pre>
         *
         *  The library internally converts everything to bytes, encrypts it and then requires an encoding schema to make
         *  the output printable. Per default the url-safe version of Base64 (RFC4648) is used. This is a well supported,
         *  fast and reasonable space efficient (needs ~25% more storage than the raw bytes) encoding. Note that the
         *  output size is constant using the same settings a type and does _not_ grow or shrink depending on e.g.
         *  how big the number is.
         *
         * @param encoding to use
         * @return builder
         */
        public abstract Builder encoding(ByteToTextEncoding encoding);


        /**
         * If better security settings should be used sacrificing output size and / or performance.
         *
         * Only applicable with 16 byte ids (e.g. <code>UUID</code>, <code>byte[]</code>, <code>BigInteger</code>, ...)
         * it is optionally possible to increase the security  strength of the masked id in expense for increased id lengths.
         * By default a 8-byte MAC is appended to the ID and, if randomization is enabled, a 8-byte random nonce is prepended.
         * In high security mode these  numbers double to 16 byte, therefore high security IDs are 16 bytes longer.
         * If you generate a massive amount of ids (more than 2^32) or don't mind the longer output length, high security mode is recommended.
         *
         * Issue with smaller MAC is increased chance of not recognizing a forgery and issue with smaller randomization nonce is higher
         * chance of finding duplicated randomization values and recognizing equal ids (chance of duplicate after 5,000,000,000 randomized ids
         * with 8 byte nonce is 50%). Increasing these numbers to 16 bytes make both those issue negligible.
         *
         * @param isHighSecurityMode true if enabled
         * @return builder
         */
        public abstract Builder highSecurityMode(boolean isHighSecurityMode);

        /**
         * If the masking should create non-deterministic ids (different output for every call).
         * If tolerable, this setting should always be activated because it drastically improves the
         * effectiveness of masking.
         * <p>
         * Use this randomized ids
         *
         * <ul>
         * <li>Shareable links</li>
         * <li>Single Use Tokens</li>
         * <li>Ids where the client must not compare equality (e.g. are those 2 models the same?)</li>
         * </ul>
         * <p>
         * If this is enabled, cache for encoding wil be disabled.
         *
         * @param isRandomized true if enabled
         * @return builder
         */
        public abstract Builder randomizedIds(boolean isRandomized);

        /**
         * Optionally provide your own security provider, e.g. for using BouncyCastle.
         * Use this only if you are know what you do, usually the default setting is fine.
         *
         * @param provider to use
         * @return builder
         */
        public abstract Builder securityProvider(Provider provider);

        /**
         * Optionally provide your own secure random implementation.
         * Use this only if you are know what you do, usually the default setting is fine.
         *
         * @param secureRandom to use
         * @return builder
         */
        public abstract Builder secureRandom(SecureRandom secureRandom);


        /**
         * Optionally provide your own cache implementation.
         *
         * @param cache to use
         * @return builder
         */
        public abstract Builder cacheImpl(Cache cache);

        /**
         * If masking/unmasking should be cached to speed up performance.
         *
         * <strong>Note:</strong>
         * <ul>
         * <li>Caching for masking will only work if randomizedIds are disabled</li>
         * <li>only makes sense if same ids will be masked from time to time</li>
         * <li>use slightly more memory (when using the default in-memory lru cache)</li>
         * <li>exposes the raw/masked ids mappings in memory which might be bad for Android</li>
         * </ul>
         *
         * @param shouldCache true if enabled
         * @return builder
         */
        public abstract Builder enableCache(boolean shouldCache);

        /**
         * Wipes memory of security relevant date (e.g. byte arrays) immediately after usage
         * in the expensive of making masking a bit slower. This is an advanced security feature.
         *
         * @param shouldAutoWipe true if enabled
         * @return builder
         */
        public abstract Builder autoWipeMemory(boolean shouldAutoWipe);

        /**
         * Create config
         *
         * @return new config instance
         */
        public abstract Config build();
    }
}
