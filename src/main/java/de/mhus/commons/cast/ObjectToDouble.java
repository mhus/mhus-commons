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

import java.util.OptionalDouble;

@Slf4j
public class ObjectToDouble implements Caster<Object, Double> {

    @Override
    public Class<? extends Double> getToClass() {
        return Double.class;
    }

    @Override
    public Class<? extends Object> getFromClass() {
        return Object.class;
    }

    @Override
    public Double cast(Object in, Double def) {
        return toDouble(in).orElse(def);
    }

    public OptionalDouble toDouble(Object in) {
        if (in == null) return OptionalDouble.empty();
        if (in instanceof Number) {
            double r = ((Number) in).doubleValue();
            return OptionalDouble.of(r);
        }
        try {
            double r = Double.parseDouble(String.valueOf(in));
            return OptionalDouble.of(r);
        } catch (Exception e) {
            LOGGER.trace("cast to double failed {}", in, e.toString());
        }
        return OptionalDouble.empty();
    }
}
