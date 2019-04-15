package at.favre.lib.idmask.ext;

import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMasks;

public final class MyIdMaskLongSerializers {
    private MyIdMaskLongSerializers() {
    }

    public static final class Serializer extends IdMaskJackson.LongSerializer {
        public Serializer() {
            super(IdMasks.forLongIds(Config.builder(TestKey.KEY).build()));
        }
    }

    public static final class Deserializer extends IdMaskJackson.LongDeserializer {
        public Deserializer() {
            super(IdMasks.forLongIds(Config.builder(TestKey.KEY).build()));
        }
    }
}
