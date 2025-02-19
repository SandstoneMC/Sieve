package dev.sandstonemc.sieve;

import java.io.Serial;

/**
 * Thrown when a class has a name that is invalid or otherwise unsupported.
 */
public final class InvalidClassNameException extends IllegalArgumentException {

    @Serial
    private static final long serialVersionUID = 187520294436772453L;

    /**
     * The fully qualified name of the invalid guest class.
     */
    private final String className;

    public InvalidClassNameException(String className) {
        this.className = className;
    }

    public InvalidClassNameException(String className, String details) {
        super(details);
        this.className = className;
    }

    public InvalidClassNameException(String className, String details, Throwable cause) {
        super(details, cause);
        this.className = className;
    }
}