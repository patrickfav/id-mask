package at.favre.lib.idmask;

/**
 * Used as exception for probable security relevant issues with given id.
 * <p>
 * DO NOT pass details of this exception to the client as it may reveal security relevant information.
 */
public class IdMaskSecurityException extends SecurityException {

    /**
     * Technical reason of the exception
     */
    public enum Reason {

        /**
         * The internal authentication tag (e.g. MAC) does not match. This could mean a forgery attempt hash happened or
         * if an incorrect secret key was used (often not possible to discern).
         */
        AUTH_TAG_DOES_NOT_MATCH_OR_INVALID_KEY,

        /**
         * The encoded engine id in the version byte is unknown.
         */
        UNKNOWN_ENGINE_ID,

        /**
         * The encoded key id in the version byte is unknown.
         */
        UNKNOWN_KEY_ID,
    }

    /**
     * Technical reason for the exception
     */
    private final Reason reason;

    public IdMaskSecurityException(String s, Reason reason) {
        super(s);
        this.reason = reason;
    }

    public IdMaskSecurityException(String message, Reason reason, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    /**
     * Reason type of why the exception was thrown
     *
     * @return reason
     */
    public Reason getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        return "[" + reason + "] " + super.getMessage();
    }
}
