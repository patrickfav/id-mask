package at.favre.lib.idmask;

public enum ContentType {
    SINGLE_LONG((byte) 0x01),
    DOUBLE_LONG((byte) 0x02),
    UUID((byte) 0x03),
    BYTE_ARRAY((byte) 0x04);

    private final byte id;

    ContentType(byte id) {
        this.id = id;
    }

    public byte getId() {
        return id;
    }
}
