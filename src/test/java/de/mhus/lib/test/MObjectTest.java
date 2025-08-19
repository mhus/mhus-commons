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

import de.mhus.commons.tools.MObject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class MObjectTest {

    @Test
    public void testCompareTo() {
        // Example usage of MObject.compareTo
        Integer a = 5;
        Integer b = 10;
        int result = MObject.compareTo(a, b);
        assert result < 0 : "Expected a to be less than b";

        String str1 = "apple";
        String str2 = "banana";
        result = MObject.compareTo(str1, str2);
        assert result < 0 : "Expected str1 to be less than str2";

        Object nullObj = null;
        result = MObject.compareTo(nullObj, a);
        assert result < 0 : "Expected null to be less than a non-null object";
    }

    @Test
    public void testComparing() {
        // Example usage of MObject.comparing
        Comparator<Integer> comparator = MObject.comparing(Function.identity());
        int result = comparator.compare(3, 4);
        assert result < 0 : "Expected 3 to be less than 4";

        List<String> list = Arrays.asList("apple", "banana", "cherry");
        list.sort(MObject.comparing(String::length));
        assert list.get(0).equals("apple") : "Expected apple to be the first element after sorting by length";
    }

}
