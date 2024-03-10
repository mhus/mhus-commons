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
package de.mhus.commons.util;

import de.mhus.commons.lang.IValuesProvider;

import java.util.Map;

public class MapValuesProvider implements IValuesProvider {

    private Map<String, Object> map;

    public MapValuesProvider(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public Object get(String key) {
        return map.get(key);
    }

    @Override
    public String toString() {
        return String.valueOf(map);
    }

    public Map<String, Object> getMap() {
        return map;
    }
}
