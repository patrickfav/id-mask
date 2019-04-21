package at.favre.lib.idmask.ext;

import at.favre.lib.bytes.Bytes;

public final class TestKey {
    private TestKey() {
    }

    public static final byte[] KEY = Bytes.parseHex("a3a5a7a9a1a0abea7aa8aaa3afaaaaaa").array();
}
