package dev.sandstonemc.sieve.test;

import dev.sandstonemc.sieve.GuestClassProvider;
import dev.sandstonemc.sieve.HostClassAccess;
import dev.sandstonemc.sieve.SieveClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class Main {

    public static void main(String... args) {

        // Define which classes from the host are made available to guests.
        final HostClassAccess host = new HostClassAccess();
        host.allowJDK();
        host.allow("dev.sandstonemc.sieve.test.IPlugin");

        // Collect classes from guest modules. For now, we are just manually
        // defining the class. In a future version we will collect them from
        // jar files.
        final GuestClassProvider guest = new GuestClassProvider();
        guest.reserveJDK();
        guest.add("dev.sandstonemc.sieve.test.TestPlugin", Path.of("./build\\classes\\java\\test\\dev\\sandstonemc\\sieve\\test\\TestPlugin.class")); // Created when the project is built.

        // Creates the sandboxed class loader to load guest module classes.
        // Host classes are loaded through the current class loader context.
        final SieveClassLoader classLoader = new SieveClassLoader(guest, host);

        // Load the test plugin class and instantiate it using reflection.
        // This is only required to invoke the initial entry point. The host
        // can fully utilize the guest afterward.
        final IPlugin testPlugin = newInstance(classLoader, IPlugin.class, "dev.sandstonemc.sieve.test.TestPlugin");
        System.out.println("Hello from plugin " + testPlugin.getName());
    }

    // This is a dirty helper method I write to load and instantiate the class.
    // This is not how things would be done under normal circumstances and is
    // only used to invoke entry points from the guest.
    @SuppressWarnings({"unchecked", "unused"})
    private static <T> T newInstance(SieveClassLoader classLoader, Class<T> type, String name) {
        try {
            final Class<?> loadedClass = classLoader.loadClass(name);
            return (T) loadedClass.getConstructor(new Class[0]).newInstance();
        }
        catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
               NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}