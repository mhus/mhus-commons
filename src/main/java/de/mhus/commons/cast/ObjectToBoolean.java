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

import de.mhus.commons.lang.OptionalBoolean;
import de.mhus.commons.util.Value;

public class ObjectToBoolean implements Caster<Object, Boolean> {

    @Override
    public Class<? extends Boolean> getToClass() {
        return Boolean.class;
    }

    @Override
    public Class<? extends Object> getFromClass() {
        return Object.class;
    }

    @Override
    public Boolean cast(Object in, Boolean def) {
        return toBoolean(in).orElseBoolean(def);
    }

    public OptionalBoolean toBoolean(Object in) {
        if (in == null) return OptionalBoolean.empty();

        if (in instanceof Boolean) return OptionalBoolean.of((Boolean) in);

        if (in instanceof Number) return OptionalBoolean.of(!(((Number) in).intValue() == 0));

        String ins = in.toString().toLowerCase().trim();

        if (ins.equals("yes")
                || ins.equals("true")
                || ins.equals("1")
                || ins.equals("y")
                || ins.equals("on")
                || ins.equals("t")
                || ins.equals("ja") // :-)
                || ins.equals("tak") // :-)
                || ins.equals("oui") // :-)
                || ins.equals("si") // :-)
                || ins.equals("\u4fc2") // :-) chinese
                || ins.equals("HIja'") // :-) // klingon
                || ins.equals("\u2612")) {
            return OptionalBoolean.of(true);
        }

        if (ins.equals("no")
                || ins.equals("false")
                || ins.equals("0")
                || ins.equals("off")
                || ins.equals("n")
                || ins.equals("f")
                || ins.equals("-1")
                || ins.equals("nein") // :-)
                || ins.equals("nie") // :-)
                || ins.equals("non") // :-)
                || ins.equals("\u5514\u4fc2") // :-) chinese
                || ins.equals("Qo'") // :-) klingon
                || ins.equals("\u2610")) {
            return OptionalBoolean.of(false);
        }
        return OptionalBoolean.empty();
    }
}
