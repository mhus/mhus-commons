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
