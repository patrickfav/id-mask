package at.favre.lib.idmask.ext;

import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMasks;

public final class MyIdMaskBigIntegerSerializers {
    private MyIdMaskBigIntegerSerializers() {
    }

    public static final class Serializer extends IdMaskJackson.BigIntegerSerializer {
        public Serializer() {
            super(IdMasks.forBigInteger(Config.builder(TestKey.KEY).build()));
        }
    }

    public static final class Deserializer extends IdMaskJackson.BigIntegerDeserializer {
        public Deserializer() {
            super(IdMasks.forBigInteger(Config.builder(TestKey.KEY).build()));
        }
    }
}
