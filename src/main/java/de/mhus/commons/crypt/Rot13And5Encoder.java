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
package de.mhus.commons.crypt;

import de.mhus.commons.security.IPasswordEncoder;

public class Rot13And5Encoder implements IPasswordEncoder {

    @Override
    public String encode(String plain, String secret) {
        return Rot13.encode13And5(plain);
    }

    @Override
    public String decode(String encoded, String secret) {
        return Rot13.decode13And5(encoded);
    }

    @Override
    public boolean validate(String plain, String encoded, String secret) {
        return encoded.equals(encode(plain, secret));
    }
}
