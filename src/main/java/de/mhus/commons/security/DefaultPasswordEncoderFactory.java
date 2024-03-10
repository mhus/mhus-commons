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
package de.mhus.commons.security;

import de.mhus.commons.crypt.DummyEncoder;
import de.mhus.commons.crypt.Md5Encoder;
import de.mhus.commons.crypt.Rot13And5Encoder;
import de.mhus.commons.crypt.Rot13Encoder;

import java.util.HashMap;
import java.util.Map;

public class DefaultPasswordEncoderFactory implements IPasswordEncoderFactory {

    private Map<String, IPasswordEncoder> encodings = new HashMap<>();

    public DefaultPasswordEncoderFactory() {
        encodings.put(MPassword.ROT13, new Rot13Encoder());
        encodings.put(MPassword.ROT13AND5, new Rot13And5Encoder());
        encodings.put(MPassword.DUMMY, new DummyEncoder());
        encodings.put(MPassword.MD5, new Md5Encoder());
    }

    @Override
    public Map<String, IPasswordEncoder> get() {
        return encodings;
    }
}
