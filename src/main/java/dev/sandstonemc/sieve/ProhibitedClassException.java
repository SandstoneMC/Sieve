package dev.sandstonemc.sieve;

import java.io.Serial;

/**
 * Thrown when a guest module tries to access a host class that it has not been given permission to access. While this
 * does not mean the module is acting maliciously, it does indicate that the guest module can not be loaded and that
 * permission levels must be changed.
 */
public final class ProhibitedClassException extends ClassNotFoundException {

    @Serial
    private static final long serialVersionUID = -4391628373568025220L;

    private final String className;

    public ProhibitedClassException(String className) {
        this.className = className;
    }

    public ProhibitedClassException(String className, String details) {
        super(details);
        this.className = className;
    }

    public ProhibitedClassException(String className, String details, Throwable cause) {
        super(details, cause);
        this.className = className;
    }

    /**
     * Creates an exception with the default message for the provided class.
     *
     * @param className The fully qualified name of the class that could not be accessed.
     * @return A throwable exception for the given class.
     */
    public static ProhibitedClassException of(String className) {
        return new ProhibitedClassException(className, "Blocked attempt to access restricted class! " + className);
    }
}