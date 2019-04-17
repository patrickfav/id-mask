package at.favre.lib.idmask.ext;

import at.favre.lib.idmask.Config;
import at.favre.lib.idmask.IdMasks;

public final class MyIdMaskUuidSerializers {
    private MyIdMaskUuidSerializers() {
    }

    public static final class Serializer extends IdMaskJackson.UuidSerializer {
        public Serializer() {
            super(IdMasks.forUuids(Config.builder(TestKey.KEY).build()));
        }
    }

    public static final class Deserializer extends IdMaskJackson.UuidDeserializer {
        public Deserializer() {
            super(IdMasks.forUuids(Config.builder(TestKey.KEY).build()));
        }
    }
}
