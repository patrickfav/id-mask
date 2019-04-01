package at.favre.lib.idmask;

import com.google.auto.value.AutoValue;
import org.jetbrains.annotations.Nullable;

import java.security.Provider;
import java.security.SecureRandom;

@AutoValue
public abstract class Config {

    abstract byte[] key();

    abstract ByteToTextEncoding encoding();

    abstract boolean randomizedIds();

    abstract boolean highSecurityMode();

    @Nullable
    abstract Provider securityProvider();

    abstract SecureRandom secureRandom();

    abstract Cache cacheImpl();

    abstract boolean cacheDecode();

    abstract boolean cacheEncode();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_Config.Builder()
                .encoding(new ByteToTextEncoding.Base64())
                .highSecurityMode(false)
                .randomizedIds(false)
                .securityProvider(null)
                .cacheImpl(new Cache.SimpleLruCache())
                .cacheDecode(true)
                .cacheEncode(false)
                .secureRandom(new SecureRandom());
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder key(byte[] key);

        public abstract Builder encoding(ByteToTextEncoding encoding);

        public abstract Builder highSecurityMode(boolean isHighSecurityMode);

        public abstract Builder randomizedIds(boolean isRandomized);

        public abstract Builder securityProvider(Provider provider);

        public abstract Builder secureRandom(SecureRandom secureRandom);

        abstract Builder cacheImpl(Cache cache);

        public abstract Builder cacheDecode(boolean shouldCache);

        public abstract Builder cacheEncode(boolean shouldCache);

        public abstract Config build();
    }
}
