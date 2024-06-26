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
package de.mhus.commons.tree;

import de.mhus.commons.lang.OptionalBoolean;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public interface IReadonly extends Iterable<Entry<String, Object>> {

    String getString(String name, String def);

    String getStringOrCreate(String name, Function<String, String> def);

    Optional<String> getString(String name);

    boolean getBoolean(String name, boolean def);

    // boolean getBooleanOrCreate(String name, Function<String, Boolean> def);

    OptionalBoolean getBoolean(String name);

    int getInt(String name, int def);

    OptionalInt getInt(String name);

    OptionalLong getLong(String name);

    OptionalDouble getDouble(String name);

    // int getIntOrCreate(String name, Function<String, Integer> def);

    long getLong(String name, long def);

    // long getLongOrCreate(String name, Function<String, Long> def);

    float getFloat(String name, float def);

    // float getFloatOrCreate(String name, Function<String, Float> def);

    double getDouble(String name, double def);

    // double getDoubleOrCreate(String name, Function<String, Double> def);

    Optional<Calendar> getCalendar(String name);

    // Calendar getCalendarOrCreate(String name, Function<String, Calendar> def) throws
    // MException;

    Optional<Date> getDate(String name);

    // Date getDateOrCreate(String name, Function<String, Date> def);

    Number getNumber(String name, Number def);

    // Number getNumberOrCreate(String name, Function<String, Number> def);

    boolean isProperty(String name);

    Set<String> keys();

    Object get(Object name);

    Object getProperty(String name);

    boolean containsValue(Object value);

    boolean containsKey(Object key);

    Collection<Object> values();

    Set<Entry<String, Object>> entrySet();

    int size();
}
