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

import de.mhus.commons.crypt.DummyEncoder;
import de.mhus.commons.crypt.Md5Encoder;
import de.mhus.commons.crypt.Rot13And5Encoder;
import de.mhus.commons.crypt.Rot13Encoder;
import de.mhus.commons.errors.NotSupportedException;
import de.mhus.commons.services.MService;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultPassword implements IPassword {

    private static Map<String, IPasswordEncoder> encodings = null;
    private Collection<String> keys;

    @Override
    public Collection<String> supportedEncodings() {
        init();
        return keys;
    }

    protected final synchronized void init() {
        if (encodings != null) return;
        encodings = new HashMap<>();
        encodings.put(MPassword.ROT13, new Rot13Encoder());
        encodings.put(MPassword.ROT13AND5, new Rot13And5Encoder());
        encodings.put(MPassword.DUMMY, new DummyEncoder());
        encodings.put(MPassword.MD5, new Md5Encoder());

        Map<String, IPasswordEncoder> map = MService.getService(IPasswordEncoderFactory.class).get();
        if (map != null) map.forEach((k, v) -> encodings.put(k.toLowerCase(), v));

        additionalEncodings(encodings);

        keys = Collections.unmodifiableCollection(encodings.keySet());
    }

    protected void additionalEncodings(Map<String, IPasswordEncoder> encodings2) {}

    @Override
    public String encode(String method, String plain, String secret) {
        if (plain == null) return null;
        init();

        IPasswordEncoder encoder = encodings.get(method.toLowerCase());
        if (encoder == null) throw new NotSupportedException("method {1} not found", method);

        return encoder.encode(plain, secret);
    }

    @Override
    public String decode(String method, String encoded, String secret) {
        if (encoded == null) return null;
        init();

        IPasswordEncoder encoder = encodings.get(method.toLowerCase());
        if (encoder == null) throw new NotSupportedException("method {1} not found", method);

        return encoder.decode(encoded, secret);
    }

    @Override
    public boolean validate(String method, String plain, String encoded, String secret) {
        if (plain == null) return false;
        init();

        IPasswordEncoder encoder = encodings.get(method.toLowerCase());
        if (encoder == null) throw new NotSupportedException("method {1} not found", method);

        return encoder.validate(plain, encoded, secret);
    }
}
