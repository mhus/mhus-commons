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

import de.mhus.commons.tools.MBigMath;
import de.mhus.commons.tools.MCast;
import de.mhus.commons.tools.MMath;
import de.mhus.commons.util.Base64;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MMathTest extends TestCase {

    @Test
    public void testTrimDecimals() {
        double in = 345.1234567;

        {
            double out = MMath.truncateDecimals(in, 0);
            assertEquals(345.0, out);
        }
        {
            double out = MMath.truncateDecimals(in, 1);
            assertEquals(345.1, out);
        }
        {
            double out = MMath.truncateDecimals(in, 2);
            assertEquals(345.12, out);
        }
        {
            double out = MMath.truncateDecimals(in, 3);
            assertEquals(345.123, out);
        }
        {
            double out = MMath.truncateDecimals(in, 4);
            assertEquals(345.1234, out);
        }
        {
            double out = MMath.truncateDecimals(in, 5);
            assertEquals(345.12345, out);
        }
    }

    @Test
    public void testByteAddRotate() {
        for (byte d = Byte.MIN_VALUE; d < Byte.MAX_VALUE; d++) {
            for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
                byte l = MMath.addRotate(b, d);
                byte r = MMath.subRotate(l, d);
                if (b != r) {
                    System.out.println(b + " -> " + l + " -> " + r);
                    System.out.println(
                            MCast.toBitsString(b) + " -> " + MCast.toBitsString(l) + " -> " + MCast.toBitsString(r));
                }
                assertEquals(b, r);
            }
        }
    }

    @Test
    public void testByteRotate() {
        for (int d = 1; d < 8; d++) {
            for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
                byte l = MMath.rotl(b, d);
                byte r = MMath.rotr(l, d);
                if (b != r) {
                    System.out.println(b + " -> " + l + " -> " + r);
                    System.out.println(
                            MCast.toBitsString(b) + " -> " + MCast.toBitsString(l) + " -> " + MCast.toBitsString(r));
                }
                assertEquals(b, r);
            }
        }
    }

    @Test
    public void testIntRotate() {
        for (int d = 1; d < 32; d++) {
            {
                int i = Integer.MIN_VALUE;
                int l = MMath.rotl(i, d);
                int r = MMath.rotr(l, d);
                if (i != r)
                    System.out.println(i + " -> " + l + " -> " + r);
                assertEquals(i, r);
            }
            {
                int i = Integer.MAX_VALUE;
                int l = MMath.rotl(i, d);
                int r = MMath.rotr(l, d);
                if (i != r)
                    System.out.println(i + " -> " + l + " -> " + r);
                assertEquals(i, r);
            }
            {
                int i = 1;
                int l = MMath.rotl(i, d);
                int r = MMath.rotr(l, d);
                if (i != r)
                    System.out.println(i + " -> " + l + " -> " + r);
                assertEquals(i, r);
            }
            {
                int i = -1;
                int l = MMath.rotl(i, d);
                int r = MMath.rotr(l, d);
                if (i != r)
                    System.out.println(i + " -> " + l + " -> " + r);
                assertEquals(i, r);
            }
            {
                int i = 0;
                int l = MMath.rotl(i, d);
                int r = MMath.rotr(l, d);
                if (i != r)
                    System.out.println(i + " -> " + l + " -> " + r);
                assertEquals(i, r);
            }
        }
    }

    @Test
    public void testBigMath() {
        BigDecimal a = BigDecimal.valueOf(10);
        BigDecimal b = BigDecimal.valueOf(20);

        assertEquals(a, MBigMath.min(a, b));
        assertEquals(b, MBigMath.max(a, b));
    }

    @Test
    public void testBase64Uuid() {
        for (int i = 0; i < 10; i++) {
            UUID id = UUID.randomUUID();
            String base = Base64.uuidToBase64(id);
            System.out.println("UUID Compress: " + id + " to " + base + " " + base.length());
            UUID id2 = Base64.base64ToUuid(base);
            assertEquals(id, id2);
        }
    }

    @Test
    public void testRound() {
        assertEquals(1.23, MMath.round(1.23456789, 2));
        assertEquals(-1.23, MMath.round(-1.23456789, 2));
        assertEquals(1.24, MMath.round(1.23556789, 2));
        assertEquals(-1.24, MMath.round(-1.23556789, 2));
        assertEquals(1000.0, MMath.round(1000.0d, 17));
        assertEquals(90080070060.1, MMath.round(90080070060.1d, 9));
        assertEquals(-1000.0, MMath.round(-1000.0d, 17));
        assertEquals(-90080070060.1, MMath.round(-90080070060.1d, 9));
    }

}
