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
package de.mhus.lib.test;

import de.mhus.commons.tools.MCollection;
import de.mhus.commons.util.EmptySet;
import de.mhus.lib.test.util.TestCase;
import org.assertj.core.api.Assert;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MCollectionTest extends TestCase {

    @Test
    public void testNotNull() {
        {
            var result = MCollection.notNull("a", "b", null, "c");
            System.out.println(Arrays.toString(result));
            assertThat(result).containsExactly("a", "b", "c");
        }
        {
            var result = MCollection.notNull(null, "a", "b", null, "c");
            System.out.println(Arrays.toString(result));
            assertThat(result).containsExactly("a", "b", "c");
        }
        {
            var result = MCollection.notNull("a", "b", null, "c", null);
            System.out.println(Arrays.toString(result));
            assertThat(result).containsExactly("a", "b", "c");
        }
        {
            var result = MCollection.notNull(null, null, null);
            System.out.println(Arrays.toString(result));
            assertThat(result).isEmpty();
        }
        {
            var result = MCollection.notNull();
            System.out.println(Arrays.toString(result));
            assertThat(result).isEmpty();
        }
    }

    @Test
    public void testReplaceMap() {
        Map<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        MCollection.replaceAll(map, (k, v) -> "x");
        assertEquals("x", map.get("a"));
        assertEquals("x", map.get("b"));
        assertEquals("x", map.get("c"));
    }

    @Test
    public void testDetachedList() {
        LinkedList<String> list = new LinkedList<>();
        list.add("a");
        list.add("b");
        list.add("c");
        List<String> detached = MCollection.detached(list);
        detached.add("d");
        assertEquals(3, list.size());
    }

    @Test
    public void testDetachedSet() {
        HashSet<String> set = new HashSet<>();
        set.add("a");
        set.add("b");
        set.add("c");
        Set<String> detached = MCollection.detached(set);
        detached.add("d");
        assertEquals(3, set.size());
    }

    @Test
    public void testDetachedMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("a", "1");
        map.put("b", "2");
        map.put("c", "3");
        Map<String, String> detached = MCollection.detached(map);
        detached.put("d", "4");
        assertEquals(3, map.size());
    }

    @Test
    public void testArrayManipulation() {
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.append(array, "d");
            equals(new String[] { "a", "b", "c", "d" }, array);
        }
        {
            String[] array = new String[] {};
            array = MCollection.append(array, "d");
            equals(new String[] { "d" }, array);
        }
        // insert
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.insert(array, 0, "d");
            equals(new String[] { "d", "a", "b", "c" }, array);
        }
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.insert(array, 3, "d");
            equals(new String[] { "a", "b", "c", "d" }, array);
        }
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.insert(array, 1, "d");
            equals(new String[] { "a", "d", "b", "c" }, array);
        }
        // remove
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.remove(array, 0, 1);
            equals(new String[] { "b", "c" }, array);
        }
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.remove(array, 0, 3);
            equals(new String[] {}, array);
        }
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.remove(array, 1, 1);
            equals(new String[] { "a", "c" }, array);
        }
        {
            String[] array = new String[] { "a", "b", "c" };
            array = MCollection.remove(array, 2, 1);
            equals(new String[] { "a", "b" }, array);
        }
    }

    private void equals(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            System.err.println("Expected: " + Arrays.toString(expected));
            System.err.println("Actual  : " + Arrays.toString(actual));
            System.err.println("Not the same size: " + expected.length + " != " + actual.length);
            fail();
        }
        for (int i = 0; i < expected.length; i++)
            if (!expected[i].equals(actual[i])) {
                System.err.println("Expected: " + Arrays.toString(expected));
                System.err.println("Actual  : " + Arrays.toString(actual));
                System.err.println("Different at index " + i);
                fail();
            }
    }

    @Test
    public void testListSort() {
        LinkedList<String> l = new LinkedList<>();
        l.add("z");
        l.add("a");
        l.add("m");
        List<String> s = MCollection.sorted(l);
        assertEquals("a", s.get(0));
        assertEquals("m", s.get(1));
        assertEquals("z", s.get(2));
    }

    @Test
    public void testStringList() {
        String list = null;
        char S = ',';
        list = MCollection.append(list, S, "a");
        assertEquals("a", list);
        list = MCollection.append(list, S, "b");
        assertEquals("a,b", list);
        list = MCollection.append(list, S, "c");
        assertEquals("a,b,c", list);

        list = MCollection.set(list, S, "a");
        assertEquals("a,b,c", list);
        list = MCollection.set(list, S, "b");
        assertEquals("a,b,c", list);
        list = MCollection.set(list, S, "c");
        assertEquals("a,b,c", list);

        list = MCollection.remove(list, S, "b");
        assertEquals("a,c", list);
        list = MCollection.remove(list, S, "a");
        assertEquals("c", list);
        list = MCollection.remove(list, S, "c");
        assertEquals("", list);
    }

    @Test
    public void testEmptySet() {
        assertThat(new EmptySet<String>().toArray(new String[1]).length).isEqualTo(0);
    }
}
