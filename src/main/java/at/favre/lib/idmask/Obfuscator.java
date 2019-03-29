package at.favre.lib.idmask;

public interface Obfuscator {

    byte[] obfuscate(byte[] raw);

    byte[] deofuscate(byte[] obfuscated);

}
