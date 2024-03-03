/**
 * Copyright (C) 2022 Mike Hummel (mh@mhus.de)
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
package de.mhus.commons.crypt;

import de.mhus.commons.errors.ErrorRuntimeException;

import java.io.UnsupportedEncodingException;

public class MD5 {
    private static final int A = 0x67452301;
    private static final int B = (int) 0xEFCDAB89L;
    private static final int C = (int) 0x98BADCFEL;
    private static final int D = 0x10325476;
    private static final int[] AMTS = {7, 12, 17, 22, 5, 9, 14, 20, 4, 11, 16, 23, 6, 10, 15, 21};
    private static final int[] T =
            new int[] {
                -680876936, -389564586, 606105819, -1044525330, -176418897, 1200080426,
                -1473231341, -45705983, 1770035416, -1958414417, -42063, -1990404162,
                1804603682, -40341101, -1502002290, 1236535329, -165796510, -1069501632,
                643717713, -373897302, -701558691, 38016083, -660478335, -405537848,
                568446438, -1019803690, -187363961, 1163531501, -1444681467, -51403784,
                1735328473, -1926607734, -378558, -2022574463, 1839030562, -35309556,
                -1530992060, 1272893353, -155497632, -1094730640, 681279174, -358537222,
                -722521979, 76029189, -640364487, -421815835, 530742520, -995338651,
                -198630844, 1126891415, -1416354905, -57434055, 1700485571, -1894986606,
                -1051523, -2054922799, 1873313359, -30611744, -1560198380, 1309151649,
                -145523070, -1120210379, 718787259, -343485551
            };

    public static String hash(String text) {
        try {
            return toHexString(hash(text.getBytes("UTF-8"))).toLowerCase();
        } catch (UnsupportedEncodingException e) {
            throw new ErrorRuntimeException(e);
        }
    }

    public static byte[] hash(byte[] data) {
        int size = data.length;
        int sizeBlocks = ((size + 8) >>> 6) + 1;
        int total = sizeBlocks << 6;
        byte[] padding = new byte[total - size];
        padding[0] = (byte) 0x80;
        long lenBits = (long) size << 3;
        for (int i = 0; i < 8; i++) {
            padding[padding.length - 8 + i] = (byte) lenBits;
            lenBits >>>= 8;
        }
        int a = A;
        int b = B;
        int c = C;
        int d = D;
        int[] buffer = new int[16];
        for (int i = 0; i < sizeBlocks; i++) {
            int index = i << 6;
            for (int j = 0; j < 64; j++, index++)
                buffer[j >>> 2] =
                        ((int) ((index < size) ? data[index] : padding[index - size]) << 24)
                                | (buffer[j >>> 2] >>> 8);
            int orgA = a;
            int orgB = b;
            int orgC = c;
            int orgD = d;
            for (int j = 0; j < 64; j++) {
                int div = j >>> 4;
                int f = 0;
                int bIndex = j;
                switch (div) {
                    case 0:
                        f = (b & c) | (~b & d);
                        break;
                    case 1:
                        f = (b & d) | (c & ~d);
                        bIndex = (bIndex * 5 + 1) & 0x0F;
                        break;
                    case 2:
                        f = b ^ c ^ d;
                        bIndex = (bIndex * 3 + 5) & 0x0F;
                        break;
                    case 3:
                        f = c ^ (b | ~d);
                        bIndex = (bIndex * 7) & 0x0F;
                        break;
                }
                int tmp =
                        b
                                + Integer.rotateLeft(
                                        a + f + buffer[bIndex] + T[j], AMTS[(div << 2) | (j & 3)]);
                a = d;
                d = c;
                c = b;
                b = tmp;
            }
            a += orgA;
            b += orgB;
            c += orgC;
            d += orgD;
        }
        byte[] md5 = new byte[16];
        int count = 0;
        for (int i = 0; i < 4; i++) {
            int n = (i == 0) ? a : ((i == 1) ? b : ((i == 2) ? c : d));
            for (int j = 0; j < 4; j++) {
                md5[count++] = (byte) n;
                n >>>= 8;
            }
        }
        return md5;
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            sb.append(String.format("%02X", b[i] & 0xFF));
        }
        return sb.toString();
    }
}
