package at.favre.lib.idmask;

public enum Mode {
    SMALL_SIZE_WEAK_SECURITY((byte) 0x1, 4, 4),
    MEDIUM_SIZE_AND_SECURITY((byte) 0x2, 8, 8),
    LARGE_SIZE_AND_HIGH_SECURITY((byte) 0x3, 12, 12),
    LARGER_SIZE_AND_VERY_HIGH_SECURITY((byte) 0x4, 16, 16);

    private final byte id;
    private final int ivSrcLength;
    private final int macByteLength;

    Mode(byte id, int ivSrcLength, int macByteLength) {
        this.id = id;
        this.ivSrcLength = ivSrcLength;
        this.macByteLength = macByteLength;
    }

    public byte getId() {
        return id;
    }

    public int getEntropyByteLength() {
        return ivSrcLength;
    }

    public int getMacByteLength() {
        return macByteLength;
    }
}
