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

import de.mhus.commons.tools.MCast;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MValidator;
import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.MException;
import de.mhus.commons.tree.IProperties;
import de.mhus.commons.tree.IReadonly;
import de.mhus.commons.tree.MProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Slf4j
public class ParameterDefinition implements Externalizable {

    private String name;
    private String type;
    private String def = null;
    private boolean mandatory;
    private String mapping;
    private String format;
    private IReadonly properties;

    public ParameterDefinition(Map<String, Object> properties) {
        this.properties = new MProperties(properties);
        name = this.properties.getString("name", "");
        type = this.properties.getString("type", "");
        loadProperties();
    }

    public ParameterDefinition(String line) {
        if (MString.isIndex(line, ',')) {
            name = MString.beforeIndex(line, ',');
            line = MString.afterIndex(line, ',');

            properties = IProperties.explodeToMProperties(line.split(","), ':', (char) 0);

        } else {
            name = line;
            if (name.startsWith("*")) {
                mandatory = true;
                name = name.substring(1);
            }
            type = "string";
            return;
        }

        if (name.startsWith("*")) {
            mandatory = true;
            name = name.substring(1);
        }

        loadProperties();
    }

    private void loadProperties() {
        if (type == null)
            type = "";
        type = properties.getString("type", type);

        mandatory = properties.getBoolean("mandatory", mandatory);
        def = properties.getString("default", null);
        mapping = properties.getString("mapping", null);
        format = properties.getString("format", null);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDef() {
        return def;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public String getMapping() {
        return mapping;
    }

    public String getFormat() {
        return format;
    }

    public IReadonly getProperties() {
        return properties;
    }

    public Object transform(Object object) throws MException {
        switch (type) {
        case "int":
        case "integer":
            return MCast.toint(object, MCast.toint(def, 0));
        case "long":
            return MCast.tolong(object, MCast.tolong(def, 0));
        case "bool":
        case "boolean":
            return MCast.toboolean(object, MCast.toboolean(def, false));
        case "datestring": {
            Date date = MCast.toDate(object, MCast.toDate(def, null));
            if (date == null)
                return "";
            return new SimpleDateFormat(format).format(date);
        }
        case "date": {
            Date date = MCast.toDate(object, MCast.toDate(def, null));
            if (date == null)
                return "";
            return date;
        }
        case "enum": {
            String[] parts = def.split(",");
            String val = String.valueOf(object).toLowerCase();
            for (String p : parts)
                if (val.equals(p.toLowerCase()))
                    return p;
            if (isMandatory())
                throw new MException(RC.USAGE, "field is mandatory", name);
            return "";
        }
        case "string":
        case "text": {
            return String.valueOf(object);
        }
        default:
            LOGGER.debug("Unknown Type {} {}", name, type);
        }
        return object;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }

    public boolean validate(Object v) {
        switch (type) {
        case "int":
        case "integer":
            return MValidator.isInteger(v);
        case "long":
            return MValidator.isLong(v);
        case "bool":
        case "boolean":
            return MValidator.isBoolean(v);
        case "datestring": {
            Date date = MCast.toDate(v, MCast.toDate(def, null));
            if (date == null)
                return false;
            return true;
        }
        case "date": {
            Date date = MCast.toDate(v, MCast.toDate(def, null));
            if (date == null)
                return false;
            return true;
        }
        case "enum": {
            String[] parts = def.split(",");
            String val = String.valueOf(v).toLowerCase();
            for (String p : parts)
                if (val.equals(p.toLowerCase()))
                    return true;
            return false;
        }
        case "string":
        case "text":
            return true;
        default:
            LOGGER.debug("Unknown Type {} {}", name, type);
        }
        return true;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(1);
        out.writeObject(name);
        out.writeObject(type);
        out.writeObject(def);
        out.writeBoolean(mandatory);
        out.writeObject(mapping);
        out.writeObject(format);
        out.writeObject(properties);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readInt(); // 1
        name = (String) in.readObject();
        type = (String) in.readObject();
        def = (String) in.readObject();
        mandatory = in.readBoolean();
        mapping = (String) in.readObject();
        format = (String) in.readObject();
        properties = (IReadonly) in.readObject();
    }
}
