package at.favre.lib.idmask;

import java.util.UUID;

public class IdMaskFactory {

    IdMask<Long> createForLongIds(Config config) {
        return new IdMask.LongIdMask(config);
    }

    IdMask<LongTuple> createForLongTuples(Config config) {
        return new IdMask.LongIdTupleMask(config);
    }

    IdMask<UUID> createForUuids(Config config) {
        return new IdMask.UuidMask(config);
    }

}
