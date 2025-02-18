package dev.sandstonemc.sieve;

/**
 * Adds a simple sandbox to a classloader.
 */
public final class SieveClassLoader extends ClassLoader {

    private final ClassLoader root;
    private final GuestClassProvider guestAccess;
    private final HostClassAccess hostAccess;

    public SieveClassLoader(GuestClassProvider userEntries, HostClassAccess hostAccess) {
        this(userEntries, hostAccess, SieveClassLoader.class.getClassLoader());
    }

    public SieveClassLoader(GuestClassProvider guestAccess, HostClassAccess hostAccess, ClassLoader root) {
        this.root = root;
        this.guestAccess = guestAccess;
        this.hostAccess = hostAccess;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (guestAccess.contains(name)) {
            return defineGuestClass(this.guestAccess.get(name), name);
        }
        if (this.hostAccess.isAllowed(name)) {
            return this.root.loadClass(name);
        }
        throw ProhibitedClassException.of(name);
    }

    // Defines a class from a guest module.
    private Class<?> defineGuestClass(byte[] classBytes, String name) {
        final Class<?> loadedClass = this.defineClass(name, classBytes, 0, classBytes.length);
        if (loadedClass != null) {
            resolveClass(loadedClass);
        }
        return loadedClass;
    }
}