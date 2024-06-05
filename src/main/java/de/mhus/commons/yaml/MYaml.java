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
package de.mhus.commons.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import de.mhus.commons.util.Iterate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MYaml {

    private static Yaml yaml;

    public static YMap load(File file) throws FileNotFoundException, IOException {
        try (InputStream is = new FileInputStream(file)) {
            return load(is);
        }
    }

    public static YMap load(InputStream is) {
        getYaml();
        Object obj = yaml.load(is);
        return new YMap(obj);
    }

    public static synchronized Yaml getYaml() {
        if (yaml == null) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);
            yaml = new Yaml(options);
        }
        return yaml;
    }

    @SuppressWarnings("rawtypes")
    public static YElement loadFromString(String content) {
        getYaml();
        Object obj = yaml.load(content);
        if (obj instanceof Map)
            return new YMap((Map) obj);
        if (obj instanceof List)
            return new YList((List) obj);
        return new YElement(obj);
    }

    public static YMap loadMapFromString(String content) {
        getYaml();
        YMap docE = new YMap(yaml.load(content));
        return docE;
    }

    public static YList loadListFromString(String content) {
        getYaml();
        YList docE = new YList(yaml.load(content));
        return docE;
    }

    public static YMap createMap() {
        return new YMap(new LinkedHashMap<String, Object>());
    }

    public static YList createList() {
        return new YList(new LinkedList<>());
    }

    public static void write(YElement elemY, OutputStream os) throws IOException {
        try (Writer writer = new OutputStreamWriter(os)) {
            write(elemY, writer);
        }
    }

    public static void write(YElement elemY, File file) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            write(elemY, writer);
        }
    }

    public static void write(YElement elemY, Writer writer) {
        getYaml();
        yaml.dump(elemY.getObject(), writer);
    }

    public static String toString(YElement elemY) {
        getYaml();
        return yaml.dump(elemY.getObject());
    }

    public static YElement toYaml(JsonNode json) throws IOException {
        if (json == null) return new YElement(null);
        if (json.isNull()) return new YElement(null);
        // if (json.isEmpty()) return new YElement(null); // "" or []
        if (json.isTextual()) return new YElement(json.asText());
        if (json.isBoolean()) return new YElement(json.asBoolean());
        if (json.isInt()) return new YElement(json.asInt());
        if (json.isDouble()) return new YElement(json.asDouble());
        if (json.isLong()) return new YElement(json.asLong());
        if (json.isBigInteger()) return new YElement(json.bigIntegerValue());
        if (json.isBigDecimal()) return new YElement(json.decimalValue());
        if (json.isFloat()) return new YElement(json.floatValue());
        if (json.isShort()) return new YElement(json.shortValue());
        if (json.isNumber()) return new YElement(json.numberValue()); // should not happen
        if (json.isBinary()) return new YElement(json.binaryValue());
        if (json.isArray()) {
            YList list = createList();
            for (JsonNode item : json) {
                list.add(toYaml(item));
            }
            return list;
        }
        if (json.isObject()) {
            YMap map = createMap();
            for (String key : new Iterate<>(json.fieldNames())) {
                map.put(key, toYaml(json.get(key)));
            }
            return map;
        }
        return null;  // should not happen
    }

}
