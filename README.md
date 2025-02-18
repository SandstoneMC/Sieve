# Sieve
Sieve is a sandboxed class loader intended for loading sandboxed Minecraft 
mods.

## Terms & Concepts
The Sieve class loader uses a host/client model. 

The **host** is the system and runtime environment where the JDK, Minecraft,
Sanstone, and other mod loaders live. Code running in the host environment is
not sandboxed and has no additional restrictions. Guest modules can not access
host code unless it has been explicitly been exposed to them.

**Guests** are third party modules that contain untrusted code. They have
access to the full Java language, however host classes will be completely
unavailable unless the host exposes it.

If a guest tries to load a host class that has not been exposed, a
`ProhibitedClassException` is thrown. There are several ways a guest can load
a class.

- Using the class directly.
- Invoking a method that returns a host type or has a host type as a parameter.
- Accessing a field that is of a host type.

Host methods invoked by guest modules will not class load anything using this 
sandbox. This allows for APIs that safeguard access to restricted code to exist
but can also uninitentionally allow guests to escape the sandbox if you are not
cautios with what you expose.

## Usage Guide

### Host Access
On their own guest modules are not capable of doing anything. In order to
access classes in any way they must be exposed from the host environment. 
This is currently done by creating a `HostClassAccess` instance. This
provides the sandbox for the class loader and allows you to expose code.
The `allowJDK` method can be used to add access to standard JDK classes that
have been deemed relatively safe. You can also use the `allow` method to add
any class from the host.

```java
        final HostClassAccess host = new HostClassAccess();
        host.allowJDK();
        host.allow("dev.sandstonemc.sieve.test.IPlugin");
```

Host code is completely unrestricted as it is not contained within the sandbox.
When exposing host code to guests great care must be taken to avoid creating
ways to escape the sandbox. For example, guest code can not access File or any
method that returns or accepts a file, however if the body of a method creates
a file internally it would allow the guest to create new files. This may be
desired for APIs like providing access to config files, however arbitrary 
file creation would defeat the purpose.

### Guests
Classes from guest modules need to be loaded into the class loader. This is
currently done using `GuestClassProvider` which requires class files to be
manually defined. In the future we will load classes from JAR files located
in the right folder.

```java
        final GuestClassProvider guest = new GuestClassProvider();
        guest.add("dev.sandstonemc.sieve.test.TestPlugin", Path.of("./build\\classes\\java\\test\\dev\\sandstonemc\\sieve\\test\\TestPlugin.class")); // Created when the project is built.
```

### Classloading
The `SieveClassLoader` is created using a `HostClassAccess` and 
`GuestClassProvider` described in the previous sections. The host can use the 
class loader to load classes from guests. This is currently done manually but
will likely be done by defining entry points in a sandstone.mods.json file at
a later point.

```java
        final IPlugin testPlugin = newInstance(classLoader, IPlugin.class, "dev.sandstonemc.sieve.test.TestPlugin");
        System.out.println("Hello from plugin " + testPlugin.getName());
```

## Tests
There is currently a test system using plugins in the test sourceset. Proper
testing will come later, this is only an initial example.

## Future Development

- Abstract HostClassAccess and GuestClassProvider to allow for different implementations.
- Load entire JAR file into the GuestClassProvider.
- Improved error handling.
- Support for 3rd party host libraries like JOML and Guava.
- Proper test cases.