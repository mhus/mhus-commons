/**
 * Copyright (C) 2002 Mike Hummel (mh@mhus.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.mhus.commons.tools;

import de.mhus.commons.services.ClassLoaderProvider;
import de.mhus.commons.services.MService;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public class MObject {

    /**
     * Compare two objects. If the objects are null, null is smaller than any other object. If the objects are
     * comparable, the compareTo method is used. If not, the toString method is used.
     *
     * @param a
     *            The first object
     * @param b
     *            The second object
     *
     * @return -1, 0, 1
     *
     * @param <T>
     *            Type of the objects
     */
    public static <T> int compareTo(T a, T b) {
        if (a == null && b == null)
            return 0;
        if (a == null)
            return -1;
        if (b == null)
            return 1;
        if (a instanceof Comparable)
            return ((Comparable<T>) a).compareTo(b);
        return a.toString().compareTo(b.toString());
    }

    public static <T, U extends Comparable<? super U>> Comparator<T> comparing(
            Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor);
        return (Comparator<T> & Serializable) (c1, c2) -> compareTo(keyExtractor.apply(c1), keyExtractor.apply(c2));
    }

    /**
     * Create a new instance of a class. The class must have a default constructor.
     *
     * @param clazz
     *            The class
     *
     * @return The new instance
     *
     * @param <T>
     *            Type of the class
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new instance of a class. The class must have a default constructor.
     *
     * @param clazzName
     *            The class name
     *
     * @return The new instance
     *
     * @param <T>
     *            Type of the class
     */
    public static <T> T newInstance(String clazzName) {
        return newInstance(MService.getService(ClassLoaderProvider.class).getClassLoader(), clazzName);
    }

    /**
     * Create a new instance of a class. The class must have a default constructor.
     *
     * @param activator
     *            The class loader
     * @param clazzName
     *            The class name
     *
     * @return The new instance
     *
     * @param <T>
     *            Type of the class
     */
    public static <T> T newInstance(ClassLoader activator, String clazzName) {
        try {
            return (T) activator.loadClass(clazzName).newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(clazzName, e);
        }
    }

    /**
     * Create a new array of the given component type and size.
     *
     * @param componentType
     *            The component type
     * @param size
     *            The size
     *
     * @return The new array
     *
     * @param <E>
     *            Type of the component
     */
    @SuppressWarnings("unchecked")
    public static <E> E[] newArray(Class<E> componentType, int size) {
        return (E[]) Array.newInstance(componentType, size);
    }

    /**
     * Compare two objects. If the objects are null, null is smaller than any other object. The equals method is used to
     * compare the objects.
     *
     * @param a
     *            The first object
     * @param b
     *            The second object
     *
     * @return true if the objects are equal.
     */
    public static boolean equals(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;
        return a.equals(b);
    }

}
