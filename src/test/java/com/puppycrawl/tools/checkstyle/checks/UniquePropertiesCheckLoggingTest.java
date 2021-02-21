package com.puppycrawl.tools.checkstyle.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;

public class UniquePropertiesCheckLoggingTest extends AbstractModuleTestSupport {
    @Override
    protected String getPackageLocation() {
        return "com/puppycrawl/tools/checkstyle/checks/uniqueproperties";
    }

    /**
     * Iteration over the duplicated keys and their logging in {@code processFiltered} method of
     * {@code UniquePropertiesCheck} class is nondeterministic. This test reveals the nondeterminism
     * by populating 2 instances of {@code UniqueProperties} with the same entries but in a different order.
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testLogging() throws Exception {
        final UniqueProperties properties = new UniqueProperties();
        properties.put("alpha", "val1");
        properties.put("beta", "val2");
        properties.put("alpha", "val1");
        properties.put("beta", "val2");

        Iterator<Map.Entry<String, AtomicInteger>> duplication = properties.getDuplicatedKeys().entrySet().iterator();
        final String keyName = duplication.next().getKey();
        String expected = "alpha";
        assertEquals(expected, keyName);  // This assert fails

        final UniqueProperties properties1 = new UniqueProperties();
        properties1.put("beta", "val2");
        properties1.put("alpha", "val1");
        properties1.put("beta", "val2");
        properties1.put("alpha", "val1");  // This assert passes

        Iterator<Map.Entry<String, AtomicInteger>> duplication1 = properties1.getDuplicatedKeys().entrySet().iterator();
        final String keyName1 = duplication1.next().getKey();
        String expected1 = "alpha";
        assertEquals(expected1, keyName1);
    }

    /**
     * Fixes the nondeterminism exposed in {@code testLogging} by using {@code UniquePropertiesOrdered}
     * instead of {@code UniqueProperties}.
     *
     * @throws Exception
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testLoggingOrdered() throws Exception {
        final UniquePropertiesOrdered properties = new UniquePropertiesOrdered();
        properties.put("alpha", "1");
        properties.put("beta", "2");
        properties.put("alpha", "1");
        properties.put("beta", "2");

        Iterator<Map.Entry<String, AtomicInteger>> duplication = properties.getDuplicatedKeysOrdered().entrySet().iterator();
        final String keyName = duplication.next().getKey();
        String expected = "alpha";
        assertEquals(expected, keyName);

        final UniquePropertiesOrdered properties1 = new UniquePropertiesOrdered();
        properties1.put("beta", "2");
        properties1.put("alpha", "1");
        properties1.put("beta", "2");
        properties1.put("alpha", "1");

        Iterator<Map.Entry<String, AtomicInteger>> duplication1 = properties1.getDuplicatedKeysOrdered().entrySet().iterator();
        final String keyName1 = duplication1.next().getKey();
        String expected1 = "alpha";
        assertEquals(expected1, keyName1);
    }

    /**
     * Properties subclass to store duplicated property keys in a separate map.
     *
     * @noinspection ClassExtendsConcreteCollection, SerializableHasSerializationMethods
     */
    private static class UniqueProperties extends Properties {

        private static final long serialVersionUID = 1L;
        /**
         * Map, holding duplicated keys and their count. Keys are added here only if they
         * already exist in Properties' inner map.
         */
        private final Map<String, AtomicInteger> duplicatedKeys = new HashMap<>();

        /**
         * Puts the value into properties by the key specified.
         *
         * @noinspection UseOfPropertiesAsHashtable
         */
        @Override
        public synchronized Object put(Object key, Object value) {
            final Object oldValue = super.put(key, value);
            if (oldValue != null && key instanceof String) {
                final String keyString = (String) key;

                duplicatedKeys.computeIfAbsent(keyString, empty -> new AtomicInteger(0))
                        .incrementAndGet();
            }
            return oldValue;
        }

        /**
         * Retrieves a collections of duplicated properties keys.
         *
         * @return A collection of duplicated keys.
         */
        public Map<String, AtomicInteger> getDuplicatedKeys() {
            return new HashMap<>(duplicatedKeys);
        }
    }

    /**
     * Modified Properties subclass to store duplicated property keys in a separate ordered map.
     *
     * @noinspection ClassExtendsConcreteCollection, SerializableHasSerializationMethods
     */
    private static class UniquePropertiesOrdered extends Properties {

        private static final long serialVersionUID = 1L;
        /**
         * Map, holding duplicated keys and their count. Keys are added here only if they
         * already exist in Properties' inner map.
         */
        private final Map<String, AtomicInteger> duplicatedKeys = new TreeMap<>();

        /**
         * Puts the value into properties by the key specified.
         *
         * @noinspection UseOfPropertiesAsHashtable
         */
        @Override
        public synchronized Object put(Object key, Object value) {
            final Object oldValue = super.put(key, value);
            if (oldValue != null && key instanceof String) {
                final String keyString = (String) key;

                duplicatedKeys.computeIfAbsent(keyString, empty -> new AtomicInteger(0))
                        .incrementAndGet();
            }
            return oldValue;
        }

        /**
         * Retrieves a collections of duplicated properties keys.
         *
         * @return An ordered collection of duplicated keys.
         */
        public Map<String, AtomicInteger> getDuplicatedKeysOrdered() {
            return new TreeMap<>(duplicatedKeys);
        }
    }
}
