package at.favre.lib.idmask;

import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class IdMaskFactory {

    private IdMaskFactory() {
    }

    public static IdMask<Long> createForLongIds(Config config) {
        return new IdMask.LongIdMask(config);
    }

    public static IdMask<LongTuple> createForLongTuples(Config config) {
        return new IdMask.LongIdTupleMask(config);
    }

    public static IdMask<UUID> createForUuids(Config config) {
        return new IdMask.UuidMask(config);
    }

}
