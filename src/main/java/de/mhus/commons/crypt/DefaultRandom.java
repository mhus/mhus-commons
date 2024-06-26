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

import de.mhus.commons.tools.MString;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Random;

@Slf4j
public class DefaultRandom implements MRandom {

    private SecureRandom secureRandom;

    @Override
    public byte getByte() {
        return (byte) (random() * 255);
    }

    @Override
    public int getInt() {
        return (int) (random() * Integer.MAX_VALUE); // no negative values!
    }

    @Override
    public double getDouble() {
        return random();
    }

    @Override
    public long getLong() {
        return (long) (random() * Long.MAX_VALUE); // no negative values!
    }

    /**
     * Overwrite this to deliver your own random numbers
     *
     * @return
     */
    protected double random() {
        return getSecureRandom().nextDouble();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T adaptTo(Class<? extends T> ifc) {
        if (Random.class.isAssignableFrom(ifc))
            return (T) getSecureRandom();
        return null;
    }

    @Override
    public char getChar() {
        return MString.CHARS_READABLE[getInt() % MString.CHARS_READABLE.length];
    }

    @Override
    public synchronized SecureRandom getSecureRandom() {
        try {
            // secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
            if (secureRandom == null)
                secureRandom = new SecureRandom();
        } catch (Exception e) {
            LOGGER.error("Error", e);
        }
        return secureRandom;
    }

}
