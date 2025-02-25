package dev.sandstonemc.sieve;

import javax.lang.model.SourceVersion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Provides lookups for classes added by guest modules. Classes are currently mapped using their fully qualified names
 * and are stored as their raw bytes. This implementation is a WIP and will be changed significantly in the future,
 * however its purpose and core functionality will stay the same. Currently class entries need to be manually defined,
 * in the future we will scan JAR files and load all of them automatically.
 */
public final class GuestClassProvider {

    /**
     * Reserved names from modules that are shipped with some JDKs.
     */
    private static final Set<String> RESERVED_JDK = Set.of(
            "com.sun.",
            "sun.",
            "java.",
            "javax.",
            "jdk.",
            "org.ietf.",
            "org.w3c",
            "org.xml",
            "org.jcp",
            "org.netscape"
    );

    /**
     * The minimum depth for a class, including the class itself.
     */
    private static final int MINIMUM_CLASS_DEPTH = 4;
    private final Map<String, byte[]> data = new HashMap<>();
    private final Set<String> reservedNames = new HashSet<>();

    /**
     * Reserves a package name, preventing any guest module from declaring classes in them.
     *
     * @param name A partial package name that will be reserved.
     */
    public void reserve(String name) {
        this.reservedNames.add(name);
    }

    /**
     * Reserves common JDK packages, preventing any guest modules from declaring classes in them.
     */
    public void reserveJDK() {
        this.reservedNames.addAll(RESERVED_JDK);
    }

    public boolean isReserved(String name) {
        for (String r : this.reservedNames) {
            if (name.startsWith(r)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Manually defines a class file.
     *
     * @param name The fully qualified name of the class.
     * @param path The path to a valid .class file.
     */
    public void add(String name, Path path) {
        for (String reserved : this.reservedNames) {
            if (name.startsWith(reserved)) {
                throw new InvalidClassNameException(name, "Could not load class '" + name + "' as '" + reserved + "' is reserved.");
            }
        }
        if (validateGuestClassName(name)) {
            this.data.put(name, read(path));
        }
    }

    /**
     * Adds all class files from a directory and its subdirectories to the guest environment.
     *
     * @param path The directory to add.
     */
    public void addDir(Path path) {
        final int pathLength = path.toString().length() + 1;
        try (Stream<Path> files = Files.walk(path)) {
            files.filter(entry -> entry.getFileName().toString().endsWith(".class")).forEach(entry -> {
                final String pathString = entry.toString();
                if (!pathString.contains("main") && !pathString.contains("IPlugin")) {
                    this.add(pathString.substring(pathLength, pathString.length() - 6).replace("\\", "."), entry);
                }
            });
        }
        catch (IOException e) {
            throw new RuntimeException(e);
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