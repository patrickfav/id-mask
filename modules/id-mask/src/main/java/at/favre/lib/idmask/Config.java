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
     * @return if non-deterministic
     */
    abstract boolean randomizedIds();

    /**
     * If better security settings should be used sacrificing output size and / or performance
     * @return if high security mode is on
     */
    abstract boolean highSecurityMode();

    /**
     * The key manager responsible for providing the secret keys for the cryptographic primitives
     * @return key manager
     */
    abstract KeyManager keyManager();

    /**
     * Optional custom JCA security provider
     * @return provider instance
     */
    @Nullable
    abstract Provider securityProvider();

    /**
     * The cryptographically secure pseudorandom number generator used for generating randoms during masking process
     * @return random generator
     */
    abstract SecureRandom secureRandom();

    /**
     * Cache implementation used to cache if enabled.
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
     * Creates a new build with the following defaults:
     *
     * <ul>
     *     <li>Base64 encoding</li>
     *     <li>Default security provider</li>
     *     <li>Using in-memory-lru cache and enables it</li>
     *     <li>Default secure random</li>
     *     <li>Deterministic ids & high security mode disabled</li>
     * </ul>
     *
     * @return builder
     */
    public static Builder builder() {
        return new AutoValue_Config.Builder()
                .encoding(new ByteToTextEncoding.Base64Url())
                .highSecurityMode(false)
                .randomizedIds(false)
                .securityProvider(null)
                .cacheImpl(new Cache.SimpleLruMemCache())
                .enableCache(true)
                .secureRandom(new SecureRandom());
    }

    @AutoValue.Builder
    public abstract static class Builder {

        /**
         * The key manager responsible for providing the secret keys for the cryptographic primitives.
         *
         * If only a single key is used:
         * <pre>
         *    KeyManager.Factory.with(secretKey);
         * </pre>
         * @param keyManager to use (non-optional)
         * @return builder
         */
        public abstract Builder keyManager(KeyManager keyManager);

        /**
         * Used byte to text encoding.
         *
         * Implementations for Base64, Base32 and Hex are available. You may provide your own
         * however.
         *
         * E.g.
         * <pre>
         *    new ByteToTextEncoding.Base32()
         * </pre>
         *
         * @param encoding to use
         * @return builder
         */
        public abstract Builder encoding(ByteToTextEncoding encoding);


        /**
         * If better security settings should be used sacrificing output size and / or performance.
         *
         * @param isHighSecurityMode true if enabled
         * @return builder
         */
        public abstract Builder highSecurityMode(boolean isHighSecurityMode);

        /**
         * If the masking should create non-deterministic ids (different output for every call).
         * If tolerable, this setting should always be activated because it drastically improves the
         * effectiveness of masking.
         *
         * Use this randomized ids
         *
         * <ul>
         *     <li>Shareable links</li>
         *     <li>Single Use Tokens</li>
         *     <li>Ids where the client must not compare equality (e.g. are those 2 models the same?)</li>
         * </ul>
         *
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
         * If masking/unmasking should be cached to speed up performance. Mind that this will
         * use slightly more memory (when using the default in-memory lru cache).
         *
         * @param shouldCache true if enabled
         * @return builder
         */
        public abstract Builder enableCache(boolean shouldCache);

        /**
         * Create config
         * @return new config instance
         */
        public abstract Config build();
    }
}
