package dev.sandstonemc.sieve;

import javax.lang.model.SourceVersion;
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

    /**
     * The minimum depth for a class, including the class itself.
     */
    private static final int MINIMUM_CLASS_DEPTH = 4;
    private final Map<String, byte[]> data = new HashMap<>();

    /**
     * Manually defines a class file.
     *
     * @param name The fully qualified name of the class.
     * @param path The path to a valid .class file.
     */
    // TODO Add restricted package names.
    public void add(String name, Path path) {
        if (validateGuestClassName(name)) {
            data.put(name, read(path));
        }
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

    /**
     * Validates that a guest class meets the requirements to be loaded.
     *
     * @param className The fully qualified name of the guest class.
     * @return This method will always return true, opting to throw an exception if the className is not valid.
     */
    public static boolean validateGuestClassName(String className) {
        if (className == null || className.isEmpty()) {
            throw new InvalidClassNameException("", "Package name can not be null or empty.");
        }
        final String[] components = className.split("\\.");
        // Guest classes should always be at least 3 levels deep. While two
        // could also be valid, three is considered best practice because it
        // leads to fewer collisions and improves modularity and scalability.
        if (components.length < MINIMUM_CLASS_DEPTH) {
            throw new InvalidClassNameException(className, "Expected class '" + className + "' to be at least " + MINIMUM_CLASS_DEPTH + " deep. Depth was " + components.length + ". Valid example: com.example.examplemod.HelloWorld");
        }
        final int endIndex = components.length - 1;
        for (int componentIndex = 0; componentIndex < components.length; componentIndex++) {
            final String component = components[componentIndex];
            if (componentIndex == endIndex) {
                // This is not required by Java, however it is bad practice to
                // name classes like this.
                if (Character.isLowerCase(component.charAt(0))) {
                    throw new InvalidClassNameException(className, "Class name does not start with an upper case letter.");
                }
            }
            else {
                for (int charIndex = 1; charIndex < component.length(); charIndex++) {
                    // Java does not require package names to be lowercase,
                    // however it is considered best practice.
                    if (Character.isUpperCase(component.charAt(charIndex))) {
                        throw new InvalidClassNameException(className, "Package name contains an upper case letter. '" + component.charAt(charIndex) + "' in class name '" + className + "'.");
                    }
                }
            }
            // Package and class names can not use reserved keywords, this
            // ensures that they can still be declared. It's likely very
            // difficult to compile a class with a reserved name, but we still
            // check to be safe.
            if (SourceVersion.isKeyword(component)) {
                throw new InvalidClassNameException(className, "Name " + className + "contains reserved keyword literal '" + component + "'. ");
            }
            if (!Character.isJavaIdentifierStart(component.charAt(0))) {
                throw new InvalidClassNameException(className, "Invalid component '" + component + "' in class name '" + className + "'. '" + component.charAt(0) + "'");
            }
            for (int charIndex = 1; charIndex < component.length(); charIndex++) {
                if (!Character.isJavaIdentifierPart(component.charAt(charIndex))) {
                    throw new InvalidClassNameException(className, "Invalid character '" + component.charAt(charIndex) + "' in class name '" + className + "'.");
                }
            }
        }
        return true;
    }
}