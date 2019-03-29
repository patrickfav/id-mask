package at.favre.lib.idmask;

import at.favre.lib.bytes.Bytes;

public class IdMaskWrapper {
    private final byte[] payload;

    IdMaskWrapper(byte[] payload) {
        this.payload = payload;
    }

    long asLong() {
        return Bytes.wrap(payload).toLong();
    }

    long[] asLongArray() {
        return null;
    }

    byte[] asBytes() {
        return Bytes.from(payload).array();
    }
}
