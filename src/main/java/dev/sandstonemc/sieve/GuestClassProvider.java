package dev.sandstonemc.sieve;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides lookups for classes added by guest modules. Classes are currently mapped using their fully qualified names
 * and are stored as their raw bytes. This implementation is a WIP and will be changed significantly in the future,
 * however its purpose and core functionality will stay the same. Currently class entries need to be manually defined,
 * in the future we will scan JAR files and load all of them automatically.
 */
public final class GuestClassProvider {

    private final Map<String, byte[]> data = new HashMap<>();

    /**
     * Manually defines a class file.
     *
     * @param name The fully qualified name of the class.
     * @param path The path to a valid .class file.
     */
    // TODO Add restricted package names.
    public void add(String name, Path path) {
        data.put(name, read(path));
    }

    /**
     * Checks if a class with the given name exists.
     *
     * @param name The fully qualified name of the class.
     * @return If the class exists.
     */
    public boolean contains(String name) {
        return data.containsKey(name);
    }

    /**
     * Gets the raw bytes for a given class if they exist.
     *
     * @param name The fully qualified name of the class.
     * @return The bytes for the class or null if it does not exist.
     */
    public byte[] get(String name) {
        return data.get(name);
    }

    private static byte[] read(Path path) {
        try {
            return Files.readAllBytes(path);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}