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
package de.mhus.commons.security;

import de.mhus.commons.tools.MSystem;
import de.mhus.commons.services.MService;

import java.util.Collection;

public class MPassword {

    public static final String ROT13 = "r1";
    public static final String ROT13AND5 = "r2";
    public static final String DUMMY = "dummy";
    public static final String MD5 = "md5";

    // legacy
    private static final String PREFIX_DUMMY = "`X";
    private static final String PREFIX_ROT13 = "`B:";
    private static final String PREFIX_SPECIAL1 = "`A";
    private static final String SPECIAL1 = "s1";
    // END legacy

    public static final String PREFIX = "`";
    public static final String SEPARATOR = "$";

    private static final String CFG_DEFAULT = MSystem.getEnv(MPassword.class, "default", ROT13AND5);

    private MPassword() {}

    public static IPassword get() {
        return MService.getService(IPassword.class);
    }

    public static Collection<String> supportedEncodings() {
        return get().supportedEncodings();
    }

    public static String encode(String method, String plain) {
        return encode(method, plain, null);
    }

    public static String encode(String method, String plain, String secret) {
        return PREFIX + method + SEPARATOR + get().encode(method, plain, secret);
    }

    public static String decode(String method, String encoded, String secret) {
        return get().decode(method, encoded, secret);
    }

    public static String encode(String plain) {
        return encode(CFG_DEFAULT, plain, null);
    }

    public static boolean isEncoded(String plain) {
        if (plain == null) return false;
        return plain.startsWith(PREFIX);
    }

    public static String decode(String encoded) {
        return decode(encoded, null);
    }

    public static String decode(String encoded, String secret) {
        if (encoded == null) return null;
        if (!isEncoded(encoded)) return encoded;
        // legacy
        if (encoded.startsWith(PREFIX_ROT13)) return get().decode(ROT13AND5, encoded, secret);
        if (encoded.startsWith(PREFIX_DUMMY)) return get().decode(DUMMY, encoded, secret);
        if (encoded.startsWith(PREFIX_SPECIAL1)) return get().decode(SPECIAL1, encoded, secret);
        // END legacy
        int pos = encoded.indexOf(SEPARATOR, 1);
        if (pos == -1) return encoded;
        String method = encoded.substring(1, pos);
        encoded = encoded.substring(pos + 1);
        return get().decode(method, encoded, secret);
    }

    public static boolean validate(String plain, String encoded) {
        return validate(plain, encoded, null);
    }

    public static boolean validate(String plain, String encoded, String secret) {
        if (encoded == null) return false;
        if (!isEncoded(encoded)) return encoded.equals(plain);
        // legacy
        if (encoded.startsWith(PREFIX_ROT13))
            return get().validate(ROT13AND5, plain, encoded, secret);
        if (encoded.startsWith(PREFIX_DUMMY)) return get().validate(DUMMY, plain, encoded, secret);
        if (encoded.startsWith(PREFIX_SPECIAL1))
            return get().validate(SPECIAL1, plain, encoded, secret);
        // END legacy
        int pos = encoded.indexOf(SEPARATOR, 1);
        if (pos == -1) return encoded.equals(plain);
        ;
        String method = encoded.substring(1, pos);
        encoded = encoded.substring(pos + 1);
        return get().validate(method, plain, encoded, secret);
    }
}
