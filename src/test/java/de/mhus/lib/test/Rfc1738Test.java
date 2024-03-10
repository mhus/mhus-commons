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

import de.mhus.commons.util.MUri;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Rfc1738Test extends TestCase {

    @Test
    public void testCoding() {
        String s = "abcdefghijklmnop1234567890 -_+=&12;:.....\u1123";
        String d = MUri.decode(MUri.encode(s));
        assertTrue(s.equals(d));
    }

    @Test
    public void testArrays() {
        String[] s = new String[] { "abc", "def", "123454" };
        String[] d = MUri.explodeArray(MUri.implodeArray(s));
        assertTrue(Arrays.equals(s, d));

        s = new String[] {};
        d = MUri.explodeArray(MUri.implodeArray(s));
        assertTrue(Arrays.equals(s, d));
    }
}
