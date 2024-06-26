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

import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.MRuntimeException;
import de.mhus.commons.tree.IProperties;
import de.mhus.commons.util.ReadOnlyList;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MCollection {

    /**
     * Create a new list and add all items from the array. Avoid concurrentModificationException.
     *
     * @param list
     *            The list
     *
     * @return new list of items
     *
     * @param <T>
     */
    public static <T> List<T> detached(List<T> list) {
        if (list == null)
            return Collections.EMPTY_LIST;
        return new ArrayList<>(list);
    }

    /**
     * Create a new map and add all items from the map. Avoid concurrentModificationException.
     *
     * @param set
     *            The set
     *
     * @return new set of items
     *
     * @param <T>
     */
    public static <T> Set<T> detached(Set<T> set) {
        if (set == null)
            return Collections.EMPTY_SET;
        return new HashSet<>(set);
    }

    /**
     * Create a new map and add all items from the map. Avoid concurrentModificationException.
     *
     * @param map
     *            The map
     *
     * @return new map of items
     *
     * @param <K>
     * @param <V>
     */
    public static <K, V> Map<K, V> detached(Map<K, V> map) {
        if (map == null)
            return Collections.EMPTY_MAP;
        return new HashMap<>(map);
    }

    /**
     * Create a new list and add all items from the array.
     *
     * @param iter
     *            The iterable object
     *
     * @return new list of items in the iterable
     */
    public static <T> List<T> toList(Iterable<T> iter) {
        LinkedList<T> out = new LinkedList<>();
        for (T item : iter)
            out.add(item);
        return out;
    }

    /**
     * Create a new set and add all items from the array.
     *
     * @param items
     *            The array
     *
     * @return new set of items
     */
    @SuppressWarnings("unchecked")
    public static <T> Set<T> toSet(T... items) {
        HashSet<T> set = new HashSet<>();
        for (T item : items)
            set.add(item);
        return set;
    }

    /**
     * Returns true of col is not null and the value of item is included in the collection. It compares with the
     * equals() method of the collection item. Also an item value of null will be compared.
     *
     * @param col
     *            The collection
     * @param item
     *            The searched item
     *
     * @return true if contains the item
     */
    public static boolean contains(Collection<?> col, Object item) {
        if (col == null)
            return false;
        return col.contains(item);
    }

    /**
     * Returns true of array is not null and the value of item is included in the array. It compares with the equals()
     * method of the array item. Also a item value of null will be compared.
     *
     * @param array
     *            The array
     * @param item
     *            The searched item
     *
     * @return true if contains the item
     */
    public static boolean contains(Object[] array, Object item) {
        if (array == null)
            return false;
        for (Object o : array) {
            if (item == null && o == null || o != null && o.equals(item))
                return true;
        }
        return false;
    }

    /**
     * Returns the index of the item in the array. It compares with the equals() method of the array. If the item was
     * not found -1 will be returned. Also a item value of null will be compared.
     *
     * @param array
     *            The array
     * @param item
     *            The searched item
     *
     * @return The index of the item or -1
     */
    public static int indexOf(Object[] array, Object item) {
        if (array == null)
            return -1;
        int pos = -1;
        for (Object o : array) {
            pos++;
            if (item == null && o == null || o != null && o.equals(item))
                return pos;
        }
        return -1;
    }

    /**
     * Fills a list at the end with the values of an array, ignoring null values.
     *
     * @param array
     * @param list
     */
    public static <T> void copyArray(T[] array, Collection<T> list) {
        if (array == null || list == null)
            return;
        for (T item : array)
            if (item != null)
                list.add(item);
    }

    // from
    // http://stackoverflow.com/questions/203984/how-do-i-remove-repeated-elements-from-arraylist
    /**
     * remove duplicated entries
     *
     * @param list
     */
    public static <T> void removeDuplicates(List<T> list) {
        final Set<T> encountered = new HashSet<T>();
        for (Iterator<T> iter = list.iterator(); iter.hasNext();) {
            final T t = iter.next();
            final boolean first = encountered.add(t);
            if (!first) {
                iter.remove();
            }
        }
    }

    /**
     * remove duplicated entries, Attention exponential runtime behavior !!! Running from beginning to the end, the
     * first element will be left, following removed.
     *
     * @param list
     * @param comparator
     */
    public static <T> void removeDuplicates(List<T> list, Comparator<T> comparator) {
        final Set<T> encountered = new HashSet<>();
        for (Iterator<T> iter = list.iterator(); iter.hasNext();) {
            final T t = iter.next();
            boolean removed = false;
            for (Iterator<T> iter2 = encountered.iterator(); iter2.hasNext();) {
                final T e = iter2.next();
                if (comparator.compare(t, e) == 0) {
                    iter.remove();
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                encountered.add(t);
            }
        }
    }

    /**
     * Append new elements to the array and return a new array.
     *
     * @param array
     *            The current array
     * @param newElements
     *            The new elements
     */
    @SafeVarargs
    public static <T> T[] append(T[] array, T... newElements) {

        if (newElements == null || newElements.length == 0)
            return array;
        if (array == null)
            return newElements;

        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + newElements.length);
        System.arraycopy(array, 0, newArray, 0, array.length);
        System.arraycopy(newElements, 0, newArray, array.length, newElements.length);

        return newArray;
    }

    /**
     * Insert new elements to the array and return a new array.
     *
     * @param array
     *            The current array
     * @param index
     *            The index where to insert
     * @param newElements
     *            The new elements
     *
     * @return The new array
     */
    @SafeVarargs
    public static <T> T[] insert(T[] array, int index, T... newElements) {

        if (newElements == null || newElements.length == 0)
            return array;
        if (array == null)
            return newElements;
        if (index < 0 || index > array.length)
            throw new IndexOutOfBoundsException("Array.length: " + array.length + " Index: " + index);

        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length + newElements.length);
        System.arraycopy(array, 0, newArray, 0, index);
        System.arraycopy(newElements, 0, newArray, index, newElements.length);
        System.arraycopy(array, index, newArray, index + newElements.length, array.length - index);

        return newArray;
    }

    /**
     * Remove a part from the array and return a new array.
     *
     * @param array
     *            The current array
     * @param offset
     *            The start index
     * @param len
     *            The length to remove
     *
     * @return The new array
     */
    public static <T> T[] remove(T[] array, int offset, int len) {

        if (array == null)
            return null;
        if (offset < 0 || offset + len > array.length)
            throw new IndexOutOfBoundsException(
                    "Array.length: " + array.length + " Offset: " + offset + " Len: " + len);

        @SuppressWarnings("unchecked")
        T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), array.length - len);
        System.arraycopy(array, 0, newArray, 0, offset);
        System.arraycopy(array, offset + len, newArray, offset, array.length - len - offset);

        return newArray;
    }

    /**
     * Order the integer array and remove duplicates if unique is true.
     *
     * @param array
     *            The array
     * @param unique
     *            Remove duplicates
     *
     * @return The new array
     */
    public static int[] order(int[] array, boolean unique) {
        if (unique) {
            HashSet<Integer> set = new HashSet<>();
            for (int i : array)
                set.add(i);
            int[] out = new int[set.size()];
            Iterator<Integer> iter = set.iterator();
            for (int i = 0; i < out.length; i++)
                out[i] = iter.next();
            return out;
        }

        LinkedList<Integer> list = new LinkedList<>();
        for (int i : array)
            list.add(i);
        Collections.sort(list);
        int[] out = new int[list.size()];
        Iterator<Integer> iter = list.iterator();
        for (int i = 0; i < out.length; i++)
            out[i] = iter.next();
        return out;
    }

    /**
     * Order the long array and remove duplicates if unique is true.
     *
     * @param array
     *            The array
     * @param unique
     *            Remove duplicates if true
     *
     * @return The new array
     */
    public static long[] order(long[] array, boolean unique) {
        if (unique) {
            HashSet<Long> set = new HashSet<>();
            for (long i : array)
                set.add(i);
            long[] out = new long[set.size()];
            Iterator<Long> iter = set.iterator();
            for (int i = 0; i < out.length; i++)
                out[i] = iter.next();
            return out;
        }

        LinkedList<Long> list = new LinkedList<>();
        for (long i : array)
            list.add(i);
        Collections.sort(list);
        long[] out = new long[list.size()];
        Iterator<Long> iter = list.iterator();
        for (int i = 0; i < out.length; i++)
            out[i] = iter.next();
        return out;
    }

    /**
     * Create a new array and fill in the values from from to to.
     *
     * @param from
     *            The start value
     * @param to
     *            The end value
     *
     * @return The new array
     */
    public static int[] fillIntArray(int from, int to) {
        int[] out = new int[to - from];
        for (int l = 0; l < out.length; l++)
            out[l] = l + from;
        return out;
    }

    /**
     * Create a new map with string values and convert all object values to strings.
     *
     * @param in
     *            The object valued map
     * @param ignoreNull
     *            If true null values will be ignored
     *
     * @return The new map with string values
     */
    public static Map<String, String> toStringMap(Map<Object, Object> in, boolean ignoreNull) {
        HashMap<String, String> out = new HashMap<String, String>();
        for (Entry<Object, Object> e : in.entrySet()) {
            if (e.getValue() == null) {
                if (!ignoreNull)
                    out.put(e.getKey().toString(), null);
            } else {
                out.put(e.getKey().toString(), e.getValue().toString());
            }
        }
        return out;
    }

    /**
     * Create a new map with string values and convert all object values to strings.
     *
     * @param in
     *            The object valued map
     * @param ignoreNull
     *            If true null values will be ignored
     *
     * @return The new map with string values
     */
    public static Map<String, String> toStringMap(IProperties in, boolean ignoreNull) {
        HashMap<String, String> out = new HashMap<String, String>();
        for (Map.Entry<String, Object> e : in) {
            if (e.getValue() == null) {
                if (!ignoreNull)
                    out.put(e.getKey(), "");
            } else {
                out.put(e.getKey(), e.getValue().toString());
            }
        }
        return out;
    }

    /**
     * Create a new List from the array.
     *
     * @param array
     *            The array
     *
     * @return The new list
     */
    public static <T> List<T> toList(@SuppressWarnings("unchecked") T... array) {
        LinkedList<T> out = new LinkedList<>();
        for (T item : array)
            out.add(item);
        return out;
    }

    /**
     * Create a new TreeSet (ordered) from the array.
     *
     * @param items
     *            The array
     *
     * @return The new set
     */
    public static <T> TreeSet<T> toTreeSet(T[] items) {
        TreeSet<T> ret = new TreeSet<T>();
        for (T item : items)
            if (item != null)
                ret.add(item);
        return ret;
    }

    /**
     * Create a new HashSet from the array.
     *
     * @param items
     *            The array
     *
     * @return The new set
     */
    public static <T> HashSet<T> toHashSet(T[] items) {
        HashSet<T> ret = new HashSet<T>();
        for (T item : items)
            if (item != null)
                ret.add(item);
        return ret;
    }

    /**
     * Add all items from the array to the end of the list.
     *
     * @param list
     *            The list
     * @param items
     *            The array
     */
    public static <T> void addAll(List<T> list, T[] items) {
        for (T i : items)
            if (i != null)
                list.add(i);
    }

    /**
     * Add all items from the array to the set.
     *
     * @param list
     *            The set
     * @param items
     *            The array
     */
    public static <T> void addAll(Set<T> list, T[] items) {
        for (T i : items)
            if (i != null)
                list.add(i);
    }

    /**
     * Returns a new Read Only list containing the given items. In contrast to Collections.unmodifiableList() it will
     * create a copy of the list.
     *
     * @param in
     *            The list
     *
     * @return The new read only list
     */
    public static <T> List<T> toReadOnlyList(List<? extends T> in) {
        return new ReadOnlyList<T>(in);
    }

    /**
     * Returns a new list containing the given items.
     *
     * @return The new list
     */
    public static <T> List<T> toList(Collection<? extends T> set) {
        LinkedList<T> out = new LinkedList<>();
        out.addAll(set);
        return out;
    }

    /**
     * Returns a new set containing the given items.
     *
     * @param list
     *            The list
     *
     * @return The new set
     */
    public static <T> Set<T> toSet(Collection<? extends T> list) {
        HashSet<T> set = new HashSet<>();
        set.addAll(list);
        return set;
    }

    /**
     * Returns true if the given item is part of the list. The list itself is a char separated list of items. White
     * spaces are not allowed! The search is case sensitive.
     *
     * @param list
     *            The list of items
     * @param separator
     *            The separator between the list items
     * @param item
     *            The searched item
     *
     * @return true if the item is part of the list
     */
    public static boolean contains(String list, char separator, String item) {
        if (list == null || item == null)
            return false;
        // (.*,|)test(,.*|)
        String s = Pattern.quote(String.valueOf(separator));
        return list.matches("(.*" + s + "|)" + Pattern.quote(item) + "(" + s + ".*|)");
    }

    /**
     * Append the item to the end of the list using the separator. If list is empty the item will be the first element.
     *
     * @param list
     *            The list of items
     * @param separator
     *            The item separator
     * @param item
     *            New item to append
     *
     * @return The new list
     */
    public static String append(String list, char separator, String item) {
        if (item == null)
            return list;
        if (MString.isEmpty(list))
            return item;
        return list + separator + item;
    }

    /**
     * Append the item to the end of the list using the separator if not already exists. If list is empty the item will
     * be the first element.
     *
     * @param list
     *            The list of items
     * @param separator
     *            The item separator
     * @param item
     *            New item to append
     *
     * @return The new list
     */
    public static String set(String list, char separator, String item) {
        if (item == null)
            return list;
        if (MString.isEmpty(list))
            return item;
        if (contains(list, separator, item))
            return list;
        return list + separator + item;
    }

    /**
     * Remove the given item from the list once.
     *
     * @param list
     *            List of items
     * @param separator
     *            Separator between the items
     * @param item
     *            The item to remove
     *
     * @return New list with removed item
     */
    public static String remove(String list, char separator, String item) {
        if (list == null || item == null || list.length() == 0)
            return list;
        if (list.equals(item))
            return ""; // last element
        if (list.startsWith(item + separator))
            return list.substring(item.length() + 1); // first element
        if (list.endsWith(separator + item))
            return list.substring(0, list.length() - 1 - item.length()); // last element
        return list.replaceFirst(Pattern.quote(separator + item + separator), String.valueOf(separator)); // somewhere
                                                                                                          // in the
                                                                                                          // middle
    }

    /**
     * Returns a new instance of Map with sorted keys.
     *
     * @param in
     *            The source map
     *
     * @return a new sorted map
     */
    public static <K, V> Map<K, V> sorted(Map<K, V> in) {
        return new TreeMap<K, V>(in);
    }

    /**
     * If sort is possible (instance of Comparable) the function will create a new instance of list, copy and sort all
     * the entries from the source into the new list and returns the created list.
     *
     * <p>
     * If the list could not be sorted the original list object will be returned.
     *
     * @param in
     *            The source list
     *
     * @return A new sorted list
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <K> List<K> sorted(List<K> in) {
        if (in == null || in.size() <= 0)
            return Collections.EMPTY_LIST;
        if (in.get(0) instanceof Comparable) {
            LinkedList<Comparable> l = new LinkedList<>();
            for (K item : in)
                if (item instanceof Comparable)
                    l.add((Comparable) item);
            Collections.sort(l);
            return (List<K>) l;
        }
        return detached(in);
    }

    /**
     * The function will create a new instance of map, copy and sort all the entries from the source.
     *
     * @param in
     *            The source map
     * @param comp
     *            The comparator
     *
     * @return A new sorted map
     */
    public static <K, V> Map<K, V> sorted(Map<K, V> in, Comparator<? super K> comp) {
        TreeMap<K, V> out = new TreeMap<K, V>(comp);
        out.putAll(in);
        return out;
    }

    /**
     * The function will create a new instance of list, copy and sort all the entries from the source into the new list
     * and returns the created list.
     *
     * @param in
     *            The source list
     * @param comp
     *            The comparator
     *
     * @return A new sorted list
     */
    public static <K> List<K> sorted(List<K> in, Comparator<? super K> comp) {
        if (in == null || in.size() <= 0)
            return Collections.EMPTY_LIST;
        if (in.get(0) instanceof Comparable) {
            LinkedList<K> l = new LinkedList<>();
            for (K item : in)
                if (item instanceof Comparable)
                    l.add(item);
            Collections.sort(l, comp);
            return (List<K>) l;
        }
        return detached(in);
    }

    /**
     * Process for each entry in the array. Return the new value for each entry.
     *
     * @param array
     *            The array
     * @param manipulator
     *            The function to manipulate each entry.
     */
    public static <T> void replaceAll(T[] array, Function<T, T> manipulator) {
        if (array == null)
            return;
        for (int i = 0; i < array.length; i++)
            array[i] = manipulator.apply(array[i]);
    }

    public static <K, V> void replaceAll(Map<K, V> map, BiFunction<K, V, V> manipulator) {
        if (map == null)
            return;
        for (Entry<K, V> entry : map.entrySet()) {
            var newValue = manipulator.apply(entry.getKey(), entry.getValue());
            if (newValue != null)
                entry.setValue(newValue);
        }
    }

    /**
     * Execute the consumer for each entry of the array.
     *
     * @param array
     *            The array
     * @param consumer
     *            The consumer
     */
    public static <T> void forEach(T[] array, Consumer<T> consumer) {
        if (array == null)
            return;
        for (int i = 0; i < array.length; i++)
            consumer.accept(array[i]);
    }

    /**
     * Return true if the collection is null or empty.
     *
     * @param col
     *            The collection
     *
     * @return true if the collection is null or empty
     */
    public static boolean isEmpty(Collection<?> col) {
        return col == null || col.size() == 0;
    }

    /**
     * Return true if the map is null or empty.
     *
     * @param map
     *            The map
     *
     * @return true if the map is null or empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.size() == 0;
    }

    /**
     * Return true if the collection is not empty.
     *
     * @param col
     *            The collection
     *
     * @return true if the collection is not empty
     */
    public static boolean isSet(Collection<?> col) {
        return !isEmpty(col);
    }

    /**
     * Return true if the map is not empty.
     *
     * @param map
     *            The map
     *
     * @return true if the map is not empty
     */
    public static boolean isSet(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * Return true if the array is null or empty.
     *
     * @param array
     *            The array
     *
     * @return true if the array is null or empty
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0 || isAllNull(array);
    }

    /**
     * Return true if the array is not empty.
     *
     * @param array
     *            The array
     *
     * @return true if the array is not empty
     */
    public static boolean isSet(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * Return true if all elements are null in the array.
     *
     * @param array
     *            The array
     *
     * @return true if all elements are null
     */
    public static boolean isAllNull(Object[] array) {
        for (Object o : array)
            if (o != null)
                return false;
        return true;
    }

    /**
     * Create a new map and convert all string keys to lower case.
     *
     * @param parameters
     *            The source map
     *
     * @return map with lower case keys
     */
    public static Map<String, Object> toLowerCaseKeys(Map<String, Object> parameters) {
        return parameters.entrySet().parallelStream()
                .collect(Collectors.toMap(entry -> entry.getKey().toLowerCase(), Map.Entry::getValue));
    }

    /**
     * Check if the arrays are equal. The arrays can be null.
     *
     * @param nr1
     *            The first array
     * @param nr2
     *            The second array
     *
     * @return true if the arrays are equal
     */
    public static boolean equals(Object[] nr1, Object[] nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.length != nr2.length)
            return false;
        for (int i = 0; i < nr1.length; i++)
            if (!MObject.equals(nr1[i], nr2[i]))
                return false;
        return true;
    }

    /**
     * Check if the collections are equal. The collections can be null. If both collections are null it will return
     * true.
     *
     * @param nr1
     *            The first collection
     * @param nr2
     *            The second collection
     *
     * @return true if the collections are equal
     */
    public static boolean equals(Collection<?> nr1, Collection<?> nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.size() != nr2.size())
            return false;
        Iterator<?> it1 = nr1.iterator();
        Iterator<?> it2 = nr2.iterator();
        while (it1.hasNext()) {
            if (!MObject.equals(it1.next(), it2.next()))
                return false;
        }
        return true;
    }

    /**
     * Check if the collections are equal. The collections can be null. Will not check the order of the elements. Only
     * if the collections contains the same elements.
     *
     * @param nr1
     *            The first collection
     * @param nr2
     *            The second collection
     *
     * @return true if the collections contains the same elements
     */
    public static boolean equalsAnyOrder(Collection<?> nr1, Collection<?> nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.size() != nr2.size())
            return false;
        Iterator<?> it1 = nr1.iterator();
        while (it1.hasNext()) {
            if (!nr2.contains(it1.next()))
                return false;
        }
        return true;
    }

    /**
     * Check if the arrays are equal. The arrays can be null. Will not check the order of the elements.
     *
     * @param nr1
     *            The first array
     * @param nr2
     *            The second array
     *
     * @return true if the arrays contains the same elements
     */
    public static boolean equalsAnyOrder(Object[] nr1, Object[] nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.length != nr2.length)
            return false;

        HashMap<Object, Integer> buffer = new HashMap<>();
        int shouldNullCount = 0;
        for (Object o : nr2) {
            if (o != null) {
                var cnt = buffer.get(o);
                if (cnt == null)
                    buffer.put(o, 1);
                else
                    buffer.put(o, cnt + 1);
            } else
                shouldNullCount++;
        }
        for (int i = 0; i < nr1.length; i++)
            if (nr1[i] != null) {
                var cnt = buffer.get(nr1[i]);
                if (cnt != null) {
                    if (cnt == 1)
                        buffer.remove(nr1[i]);
                    else
                        buffer.put(nr1[i], cnt - 1);
                } else
                    return false;
            } else {
                if (shouldNullCount <= 0)
                    return false;
                shouldNullCount--;
            }
        if (buffer.size() != 0 || shouldNullCount != 0)
            return false;
        return true;
    }

    /**
     * Check if the arrays are equal. The arrays can be null.
     *
     * @param nr1
     *            The first array
     * @param nr2
     *            The second array
     *
     * @return true if the arrays are equal
     */
    public static boolean equals(byte[] nr1, byte[] nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.length != nr2.length)
            return false;
        for (int i = 0; i < nr1.length; i++)
            if (nr1[i] != nr2[i])
                return false;
        return true;
    }

    /**
     * Check if the arrays are equal. The arrays can be null.
     *
     * @param nr1
     *            The first array
     * @param nr2
     *            The second array
     *
     * @return true if the arrays are equal
     */
    public static boolean equals(int[] nr1, int[] nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.length != nr2.length)
            return false;
        for (int i = 0; i < nr1.length; i++)
            if (nr1[i] != nr2[i])
                return false;
        return true;
    }

    /**
     * Check if the arrays are equal. The arrays can be null.
     *
     * @param nr1
     *            The first array
     * @param nr2
     *            The second array
     *
     * @return true if the arrays are equal
     */
    public static boolean equals(double[] nr1, double[] nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.length != nr2.length)
            return false;
        for (int i = 0; i < nr1.length; i++)
            if (nr1[i] != nr2[i])
                return false;
        return true;
    }

    /**
     * Check if the arrays are equal. The arrays can be null.
     *
     * @param nr1
     *            The first array
     * @param nr2
     *            The second array
     *
     * @return true if the arrays are equal
     */
    public static boolean equals(char[] nr1, char[] nr2) {
        if (nr1 == null && nr2 == null)
            return true;
        if (nr1 == null || nr2 == null)
            return false;
        if (nr1.length != nr2.length)
            return false;
        for (int i = 0; i < nr1.length; i++)
            if (nr1[i] != nr2[i])
                return false;
        return true;
    }

    /**
     * Create an Iterable from an Iterator.
     *
     * @param iterator
     *            The iterator
     *
     * @return The new iterable
     */
    public static <T> Iterable<T> iterate(final Iterator<T> iterator) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return iterator;
            }
        };
    }

    /**
     * Cut a part from an array and create a new array with the values.
     *
     * @param from
     *            The source array
     * @param start
     *            The start index
     * @param stop
     *            The stop/last index
     *
     * @return new cropped array
     */
    public static <T> T[] cropArray(T[] from, int start, int stop) {
        int length = stop - start;
        if (length < 0)
            throw new MRuntimeException(RC.STATUS.SYNTAX_ERROR, "malformed indexes", start, stop, length);
        @SuppressWarnings("unchecked")
        T[] out = (T[]) Array.newInstance(from.getClass().getComponentType(), length);
        System.arraycopy(from, start, out, 0, length);
        return out;
    }

    /**
     * Extend the array with null values at the beginning and end.
     *
     * @param from
     *            The source array
     * @param left
     *            The number of null values at the beginning
     * @param right
     *            The number of null values at the end
     *
     * @return new extended array
     */
    public static <T> T[] extendArray(T[] from, int left, int right) {
        if (left < 0 || right < 0)
            throw new MRuntimeException(RC.STATUS.SYNTAX_ERROR, "malformed extensions", left, right);
        int length = from.length + left + right;
        @SuppressWarnings("unchecked")
        T[] out = (T[]) Array.newInstance(from.getClass().getComponentType(), length);
        System.arraycopy(from, 0, out, left, from.length);
        return out;
    }

    /**
     * Search for an entry and return it use the filter to find it. Will return the first entry or null.
     *
     * @param iter
     *            The list or iterable object
     * @param filter
     *            The filter to find the entry
     *
     * @return The entry or null
     */
    public static <T> T search(Iterable<T> iter, Predicate<? super T> filter) {
        for (T item : iter)
            if (filter.test(item))
                return item;
        return null;
    }

    /**
     * Return a new map and add the attributes alternating key and value.
     *
     * @param keyValues
     *            The key value pairs
     *
     * @return A new Map filed with values
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> asMap(Object... keyValues) {
        HashMap<K, V> out = new HashMap<>();
        for (int i = 0; i < keyValues.length - 1; i = i + 2)
            out.put((K) keyValues[i], (V) keyValues[i + 1]);
        return out;
    }

    /**
     * Transforms a map into a key - value pair string.
     *
     * @param map
     *            The map
     *
     * @return a key-value list
     */
    public static String[] toPairs(Map<String, Object> map) {
        if (map == null)
            return null;
        String[] out = new String[map.size() * 2];
        int cnt = 0;
        for (Entry<String, Object> entry : map.entrySet()) {
            out[cnt] = entry.getKey();
            cnt++;
            out[cnt] = String.valueOf(entry.getValue());
            cnt++;
        }
        return out;
    }

    /**
     * Extract the keys starting with prefix in a new HashMap. Will return an empty map if prefix or map is null.
     *
     * @param <V>
     *            Type of the value
     * @param prefix
     *            Prefix of the key to extract
     * @param map
     *            Map of all entries
     *
     * @return Extracted subset
     */
    public static <V> HashMap<String, V> subset(String prefix, Map<String, V> map) {
        HashMap<String, V> out = new HashMap<>();
        if (prefix == null || map == null)
            return out;
        map.forEach((k, v) -> {
            if (k.startsWith(prefix))
                out.put(k, v);
        });
        return out;
    }

    /**
     * Extract the keys starting with prefix in a new HashMap. It removes the prefix from the keys. Will return an empty
     * map if prefix or map is null.
     *
     * @param <V>
     *            Type of the value
     * @param prefix
     *            Prefix of the key to extract
     * @param map
     *            Map of all entries
     *
     * @return Extracted subset
     */
    public static <V> HashMap<String, V> subsetCrop(String prefix, Map<String, V> map) {
        HashMap<String, V> out = new HashMap<>();
        if (prefix == null || map == null)
            return out;
        int l = prefix.length();
        map.forEach((k, v) -> {
            if (k.startsWith(prefix))
                out.put(k.substring(l), v);
        });
        return out;
    }

    public static <V> V[] notNull(V... array) {
        if (array == null)
            return null;
        int l = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null)
                l++;
        }
        if (l == array.length)
            return array;
        @SuppressWarnings("unchecked")
        V[] out = (V[]) Array.newInstance(array.getClass().getComponentType(), l);
        l = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i] != null) {
                out[l] = array[i];
                l++;
            }
        }
        return out;
    }

    public static List<Integer> toIntegerList(int[] values) {
        if (values == null)
            return List.of();
        List<Integer> out = new ArrayList<>(values.length);
        for (int i : values)
            out.add(i);
        return out;
    }

    public static List<Double> toDoubleList(double[] values) {
        if (values == null)
            return List.of();
        List<Double> out = new ArrayList<>(values.length);
        for (double i : values)
            out.add(i);
        return out;
    }

    public static List<Long> toLongList(long[] values) {
        if (values == null)
            return List.of();
        List<Long> out = new ArrayList<>(values.length);
        for (long i : values)
            out.add(i);
        return out;
    }

}
