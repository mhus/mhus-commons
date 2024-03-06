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
package de.mhus.commons.node;

import de.mhus.commons.lang.OptionalBoolean;
import de.mhus.commons.tools.MCast;
import de.mhus.commons.lang.IsNull;
import de.mhus.commons.errors.MRuntimeException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.tools.MString;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public abstract class AbstractProperties implements IProperties {

    private static final long serialVersionUID = 1L;

    /**
     * Overwrite this function to provide values in string format.
     *
     * @param key
     * @return null if the property not exists or the property value.
     */
    @Override
    public abstract Object getProperty(String key);

    //	@Deprecated
    //	public boolean getProperty(String name, boolean def) {
    //		return getBoolean(name, def);
    //	}
    //
    //	@Deprecated
    //	public String getProperty(String name, String def) {
    //		Object out = getProperty(name);
    //		if (out == null) return def;
    //		return String.valueOf(out);
    //	}

    @Override
    public String getString(String key, String def) {
        Object out;
        try {
            out = getProperty(key);
        } catch (Exception e) {
            return def;
        }
        if (out == null) return def;
        return String.valueOf(out);
    }

    @Override
    public Optional<String> getString(String key) {
        Object out = getProperty(key);
        if (out == null) Optional.empty();
        return Optional.ofNullable(MString.valueOf(out));
    }

    @Override
    public boolean getBoolean(String key, boolean def) {
        Object out;
        try {
            out = getProperty(key);
        } catch (Exception e) {
            return def;
        }
        return MCast.toboolean(out, def);
    }

    @Override
    public OptionalBoolean getBoolean(String key) {
        Object out = getProperty(key);
        if (out == null) Optional.empty();
        return MCast.toboolean(out);
    }

    @Override
    public int getInt(String key, int def) {
        Object out;
        try {
            out = getProperty(key);
        } catch (Exception e) {
            return def;
        }
        return MCast.toint(out, def);
    }

    @Override
    public long getLong(String key, long def) {
        Object out;
        try {
            out = getProperty(key);
        } catch (Exception e) {
            return def;
        }
        return MCast.tolong(out, def);
    }

    @Override
    public float getFloat(String key, float def) {
        Object out;
        try {
            out = getProperty(key);
        } catch (Exception e) {
            return def;
        }
        return MCast.tofloat(out, def);
    }

    @Override
    public double getDouble(String key, double def) {
        Object out;
        try {
            out = getProperty(key);
        } catch (Exception e) {
            return def;
        }
        return MCast.todouble(out, def);
    }

    @Override
    public Optional<Calendar> getCalendar(String key) {
        Object out = getProperty(key);
        return Optional.of(MCast.toCalendar(out));
    }

    @Override
    public Optional<Date> getDate(String key) {
        try {
            Object out = getProperty(key);
            return Optional.of(MCast.toDate(out, null));
        } catch (Exception t) {
        }
        return Optional.empty();
    }

    @Override
    public void setString(String key, String value) {
        setProperty(key, value);
    }

    @Override
    public void setInt(String key, int value) {
        setProperty(key, value);
    }

    @Override
    public void setLong(String key, long value) {
        setProperty(key, value);
    }

    @Override
    public void setDouble(String key, double value) {
        setProperty(key, value);
    }

    @Override
    public void setFloat(String key, float value) {
        setProperty(key, value);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        setProperty(key, value);
    }

    @Override
    public void setCalendar(String key, Calendar value) {
        setProperty(key, value);
    }

    @Override
    public void setDate(String key, Date value) {
        setProperty(key, value);
    }

    @Override
    public void setNumber(String key, Number value) {
        if (value == null) {
            removeProperty(key);
            return;
        }
        if (value instanceof Integer) setInt(key, (Integer) value);
        else if (value instanceof Long) {
            setLong(key, (Long) value);
        } else if (value instanceof Float) {
            setFloat(key, (Float) value);
        } else if (value instanceof Double) {
            setDouble(key, (Double) value);
        } else
            throw new MRuntimeException(
                    RC.SYNTAX_ERROR, "Unknown number class", key, value.getClass());
    }

    @Override
    public Number getNumber(String key, Number def) {
        Object out = getProperty(key);
        if (out == null) return def;
        if (out instanceof Number) return (Number) out;
        try {
            return MCast.todouble(out, 0);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Return true if the property exists.
     *
     * @param key
     * @return if exists
     */
    @Override
    public abstract boolean isProperty(String key);

    /**
     * Remove the property field in the list of properties.
     *
     * @param key
     */
    @Override
    public abstract void removeProperty(String key);

    /**
     * Overwrite this function to allow changes in properties.
     *
     * @param key
     * @param value
     */
    @Override
    public abstract void setProperty(String key, Object value);

    /**
     * Overwrite this function and return true if the property set can be edited.
     *
     * @return if is editable
     */
    @Override
    public abstract boolean isEditable();

    /** @return the keys */
    @Override
    public abstract Set<String> keys();

    @Override
    public Iterator<Entry<String, Object>> iterator() {
        return new IPIterator();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> out = new HashMap<>();
        for (Entry<String, Object> entry : this) {
            out.put(entry.getKey(), entry.getValue());
        }
        return out;
    }

    private class IPIterator implements Iterator<Entry<String, Object>> {

        private Iterator<String> keys;
        private String currentkey;

        IPIterator() {
            keys = keys().iterator();
        }

        @Override
        public boolean hasNext() {
            return keys.hasNext();
        }

        @Override
        public Entry<String, Object> next() {
            currentkey = keys.next();
            return new IPEntry(currentkey);
        }

        @Override
        public void remove() {
            try {
                removeProperty(currentkey);
            } catch (Exception e) {
                LOGGER.trace("Error", e);
            }
        }
    }

    private class IPEntry implements Entry<String, Object> {

        private String key;

        public IPEntry(String next) {
            key = next;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            try {
                return getProperty(key);
            } catch (Exception e) {
                throw new MRuntimeException(RC.STATUS.NOT_FOUND, e);
            }
        }

        @Override
        public Object setValue(Object value) {
            Object old = null;
            try {
                old = getProperty(key);
            } catch (Exception e1) {
                LOGGER.trace("Error: {}", key, e1);
            }
            try {
                setProperty(key, value);
            } catch (Exception e) {
                LOGGER.trace("Error: {}", key, e);
            }
            return old;
        }
    }

    @Override
    public abstract int size();

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) return false;
        return isProperty(String.valueOf(key));
    }

    @Override
    public Object get(Object key) {
        if (key == null) return null;
        return getProperty(String.valueOf(key));
    }

    @Override
    public Object put(String key, Object value) {
        Object current = get(key);
        setProperty(key, value);
        return current;
    }

    @Override
    public Object remove(Object key) {
        if (key == null) return null;
        Object current = get(key);
        removeProperty(String.valueOf(key));
        return current;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        for (Entry<? extends String, ? extends Object> e : m.entrySet())
            if (e.getValue() instanceof IsNull) remove(e.getKey());
            else put(e.getKey(), e.getValue());
    }

    public void putMap(Map<?, ?> m) {
        for (Entry<?, ?> e : m.entrySet())
            if (e.getValue() instanceof IsNull) remove(e.getKey());
            else put(String.valueOf(e.getKey()), e.getValue());
    }

    public void putReadProperties(IReadProperties m) {
        for (Entry<? extends String, ? extends Object> e : m.entrySet())
            if (e.getValue() instanceof IsNull) remove(e.getKey());
            else put(e.getKey(), e.getValue());
    }

    //	@Override
    //	public void clear() {
    //
    //		for (String name : keys())
    //			removeProperty(name);
    //	}

    @Override
    public Set<String> keySet() {
        return keys();
    }

    @Override
    public String getFormatted(String key, String def, Object... values) {
        String format = getString(key, def);
        if (format == null) return def; // probably null
        return String.format(format, values);
    }

    @Override
    public String getStringOrCreate(String name, Function<String, String> def) {
        Object out;
        try {
            out = getProperty(name);
        } catch (Exception e) {
            return def.apply(name);
        }
        if (out == null) return def.apply(name);
        return String.valueOf(out);
    }
}
