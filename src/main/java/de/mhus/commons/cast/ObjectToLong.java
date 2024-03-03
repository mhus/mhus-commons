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
package de.mhus.commons.cast;

import de.mhus.commons.util.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.OptionalLong;

@Slf4j
public class ObjectToLong implements Caster<Object, Long> {

    @Override
    public Class<? extends Long> getToClass() {
        return Long.class;
    }

    @Override
    public Class<? extends Object> getFromClass() {
        return Object.class;
    }

    @Override
    public Long cast(Object in, Long def) {
        return toLong(in).orElse(def);
    }

    public OptionalLong toLong(Object in) {
        if (in == null) return OptionalLong.empty();
        if (in instanceof Number) {
            long r = ((Number) in).longValue();
            return OptionalLong.of(r);
        }
        String ins = String.valueOf(in);

        try {

            if (ins.startsWith("0x") || ins.startsWith("-0x") || ins.startsWith("+0x")) {
                int start = 2;
                if (ins.startsWith("-")) start = 3;
                long out = 0;
                for (int i = start; i < ins.length(); i++) {
                    int s = -1;
                    char c = ins.charAt(i);
                    if (c >= '0' && c <= '9') s = c - '0';
                    else if (c >= 'a' && c <= 'f') s = c - 'a' + 10;
                    else if (c >= 'A' && c <= 'F') s = c - 'A' + 10;

                    if (s == -1) throw new NumberFormatException(ins);
                    out = out * 16 + s;
                }
                if (ins.startsWith("-")) out = -out;
                return OptionalLong.of(out);
            }

            long r = Long.parseLong(ins);
            return OptionalLong.of(r);

        } catch (Throwable e) {
            LOGGER.trace("Error: {}", ins, e.toString());
        }
        return OptionalLong.empty();
    }
}
