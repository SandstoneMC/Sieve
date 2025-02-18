package dev.sandstonemc.sieve;

import java.util.HashSet;
import java.util.Set;

/**
 * Determines which host classes can be accessed by guest modules. A guest will be able to access any method within the
 * host, provided the parameter types and return types are also all accessible.
 */
public final class HostClassAccess {

    /**
     * An immutable set of classes in the standard JDK that have been deemed safe to use. These will only be accessible
     * when {@link #allowJDK()} is used.
     */
    public static final Set<String> SAFE_JDK = Set.of(
            "java.lang.Appendable",
            "java.lang.ArithmeticException",
            "java.lang.ArrayIndexOutOfBoundsException",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.CharSequence",
            "java.lang.Double",
            "java.lang.Enum",
            "java.lang.Error",
            "java.lang.Exception",
            "java.lang.Float",
            "java.lang.IllegalArgumentException",
            "java.lang.IllegalStateException",
            "java.lang.IndexOutOfBoundsException",
            "java.lang.Integer",
            "java.lang.InternalError",
            "java.lang.invoke.LambdaMetafactory",
            "java.lang.invoke.StringConcatFactory",
            "java.lang.Iterable",
            "java.lang.Long",
            "java.lang.MatchException",
            "java.lang.Math",
            "java.lang.NegativeArraySizeException",
            "java.lang.NullPointerException",
            "java.lang.Number",
            "java.lang.NumberFormatException",
            "java.lang.Object",
            "java.lang.OutOfMemoryError",
            "java.lang.Record",
            "java.lang.RuntimeException",
            "java.lang.short",
            "java.lang.String",
            "java.lang.StringBuilder",
            "java.lang.StringIndexOutOfBoundsException",
            "java.lang.Throwable",
            "java.lang.Void",
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.math.RoundingMode",
            "java.text.DecimalFormat",
            "java.text.NumberFormat",
            "java.util.AbstractCollection",
            "java.util.AbstractList",
            "java.util.AbstractMap",
            "java.util.AbstractSet",
            "java.util.ArrayDeque",
            "java.util.ArrayList",
            "java.util.Arrays",
            "java.util.BitSet",
            "java.util.Collection",
            "java.util.Collections",
            "java.util.Comparator",
            "java.util.concurrent.Callable",
            "java.util.Enumeration",
            "java.util.EnumMap",
            "java.util.EnumSet",
            "java.util.SequencedCollection",
            "java.util.function.BiConsumer",
            "java.util.function.BiFunction",
            "java.util.function.BinaryOperator",
            "java.util.function.BiPredicate",
            "java.util.function.BooleanSupplier",
            "java.util.function.Consumer",
            "java.util.function.DoubleConsumer",
            "java.util.function.DoubleFunction",
            "java.util.function.DoublePredicate",
            "java.util.function.DoubleSupplier",
            "java.util.function.Function",
            "java.util.function.IntConsumer",
            "java.util.function.IntFunction",
            "java.util.function.IntPredicate",
            "java.util.function.IntSupplier",
            "java.util.function.IntUnaryOperator",
            "java.util.function.LongFunction",
            "java.util.function.LongPredicate",
            "java.util.function.LongSupplier",
            "java.util.function.Predicate",
            "java.util.function.Supplier",
            "java.util.function.ToDoubleFunction",
            "java.util.function.ToIntFunction",
            "java.util.function.UnaryOperator",
            "java.util.HashMap",
            "java.util.HashSet",
            "java.util.Hashtable",
            "java.util.IdentityHashMap",
            "java.util.IllegalFormatException",
            "java.util.Iterator",
            "java.util.LinkedHashMap",
            "java.util.LinkedHashSet",
            "java.util.LinkedList",
            "java.util.List",
            "java.util.ListIterator",
            "java.util.Map",
            "java.util.Map.Entry",
            "java.util.MissingResourceException",
            "java.util.NoSuchElementException",
            "java.util.Optional",
            "java.util.OptionalDouble",
            "java.util.OptionalInt",
            "java.util.OptionalLong",
            "java.util.PriorityQueue",
            "java.util.Queue",
            "java.util.Random",
            "java.util.regex.Matcher",
            "java.util.regex.Pattern",
            "java.util.regex.PatternSyntaxException",
            "java.util.Set",
            "java.util.SortedMap",
            "java.util.SortedSet",
            "java.util.Spliterator",
            "java.util.Spliterators",
            "java.util.Spliterators.AbstractSpliterator",
            "java.util.Stack",
            "java.util.stream.Collector",
            "java.util.stream.Collectors",
            "java.util.stream.DoubleStream",
            "java.util.stream.IntStream",
            "java.util.stream.LongStream",
            "java.util.stream.Stream",
            "java.util.StringJoiner",
            "java.util.TreeMap",
            "java.util.TreeSet",
            "java.util.UUID",
            "java.util.Vector",
            "java.util.WeakHashMap"
    );

    private final Set<String> allowedClasses = new HashSet<>();

    /**
     * Allows a host class to be used by a guest module. Great care must be taken when granting access to a class.
     * Guests will be able to access any method in an allowed class, provided the return type and all method parameter
     * types are also allowed.
     *
     * @param clazz The class to allow.
     */
    public void allow(Class<?> clazz) {
        this.allow(clazz.getCanonicalName());
    }

    /**
     * Allows a host class to be used by a guest module. Great care must be taken when granting access to a class.
     * Guests will be able to access any method in an allowed class, provided the return type and all method parameter
     * types are also allowed.
     *
     * @param name The fully qualified name of the class to allow.
     */
    public void allow(String name) {
        this.allowedClasses.add(name);
    }

    /**
     * Checks if guest modules may use a given class.
     *
     * @param className The fully qualified name of the class to check.
     * @return True if guest modules can use the class.
     */
    public boolean isAllowed(String className) {
        return this.allowedClasses.contains(className);
    }

    /**
     * Allows all classes from {@link #SAFE_JDK} to be used by guest modules.
     */
    public void allowJDK() {
        for (String className : SAFE_JDK) {
            this.allow(className);
        }
    }
}