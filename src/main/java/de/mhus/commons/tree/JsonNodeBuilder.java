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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.TooDeepStructuresException;
import de.mhus.commons.tools.MDate;
import de.mhus.commons.tools.MJson;
import de.mhus.commons.util.MIterable;
import de.mhus.commons.util.NullValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

public class JsonNodeBuilder extends ITreeNodeBuilder {

    @Override
    public ITreeNode read(InputStream is) throws MException {
        try {
            JsonNode docJ = MJson.load(is);
            return fromJson(docJ);
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    public ITreeNode fromJson(JsonNode docJ) throws MException {
        TreeNode node = new TreeNode();
        if (docJ.isArray()) {
            TreeNodeList array = node.createArray(ITreeNode.NAMELESS_VALUE);
            for (JsonNode itemJ : docJ) {
                ITreeNode obj = array.createObject();
                fill(obj, "", itemJ, 0);
            }
        } else if (docJ.isObject()) {
            fill(node, "", docJ, 0);
        } else if (docJ.isValueNode()) {
            // TODO separate for each type
            node.setString(ITreeNode.NAMELESS_VALUE, docJ.asText());
        } else {
            throw new MException(RC.SYNTAX_ERROR, "Unknown basic json object type");
        }

        return node;
    }

    private void fill(ITreeNode node, String name, JsonNode json, int level) {

        if (level > 100)
            throw new TooDeepStructuresException();

        if (json.isValueNode()) {
            node.put(ITreeNode.NAMELESS_VALUE, json.asText());
            return;
        }

        for (Map.Entry<String, JsonNode> itemJ : new MIterable<>(json.fields())) {
            if (itemJ.getValue().isArray()) {
                TreeNodeList array = node.createArray(itemJ.getKey());
                for (JsonNode item2J : itemJ.getValue()) {
                    ITreeNode obj = array.createObject();
                    fill(obj, itemJ.getKey(), item2J, level + 1);
                }
            } else if (itemJ.getValue().isObject()) {
                ITreeNode obj = node.createObject(itemJ.getKey());
                fill(obj, itemJ.getKey(), itemJ.getValue(), level + 1);
            } else
                node.put(itemJ.getKey(), itemJ.getValue().asText());
        }
    }

    @Override
    public void write(ITreeNode node, OutputStream os) throws MException {
        try {
            JsonNode objectJ = writeToJsonNode(node);
            MJson.save(objectJ, os);
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    public JsonNode writeToJsonNode(ITreeNode node) {
        if (node.isArray(ITreeNode.NAMELESS_VALUE) && node.size() == 1) {
            ArrayNode arrayJ = MJson.createArrayNode();
            for (ITreeNode itemC : node.getArray(ITreeNode.NAMELESS_VALUE).orElse(null)) {
                ObjectNode objectJ = arrayJ.addObject();
                fill(objectJ, itemC, 1);
            }
            return arrayJ;
        } else {
            ObjectNode objectJ = MJson.createObjectNode();
            fill(objectJ, node, 0);
            return objectJ;
        }
    }

    public ObjectNode writeToJsonNodeObject(ITreeNode node) {
        ObjectNode objectJ = MJson.createObjectNode();
        fill(objectJ, node, 0);
        return objectJ;
    }

    private void fill(ObjectNode objectJ, ITreeNode itemC, int level) {
        if (level > 100)
            throw new TooDeepStructuresException();

        for (String key : itemC.keys()) {
            if (itemC.isArray(key)) {
                ArrayNode arrJ = objectJ.putArray(key);
                for (ITreeNode arrC : itemC.getArray(key).orElse(null)) {
                    ObjectNode newJ = arrJ.addObject();
                    fill(newJ, arrC, level + 1);
                }
            } else if (itemC.isObject(key)) {
                ObjectNode newJ = MJson.createObjectNode();
                fill(newJ, itemC.getObject(key).orElse(null), level + 1);
                objectJ.set(key, newJ);
            } else if (itemC.get(key) instanceof NullValue)
                objectJ.putNull(key);
            else {
                Object o = itemC.get(key);
                if (o instanceof String) {
                    objectJ.put(key, (String) o);
                } else if (o instanceof Boolean)
                    objectJ.put(key, (Boolean) o);
                else if (o instanceof Integer)
                    objectJ.put(key, (Integer) o);
                else if (o instanceof Long)
                    objectJ.put(key, (Long) o);
                else if (o instanceof Double)
                    objectJ.put(key, (Double) o);
                else if (o instanceof Float)
                    objectJ.put(key, (Float) o);
                else if (o instanceof Short)
                    objectJ.put(key, (Short) o);
                else if (o instanceof BigInteger)
                    objectJ.put(key, (BigInteger) o);
                else if (o instanceof BigDecimal)
                    objectJ.put(key, (BigDecimal) o);
                else if (o instanceof byte[])
                    objectJ.put(key, (byte[]) o);
                else if (o instanceof Date) {
                    objectJ.put(key, ((Date) o).getTime());
                    objectJ.put("_" + key, MDate.toIso8601((Date) o));
                } else if (o instanceof Enum) {
                    objectJ.put(key, ((Enum<?>) o).ordinal());
                    objectJ.put("_" + key, ((Enum<?>) o).name());
                } else if (o instanceof NullValue) {
                    objectJ.putNull(key);
                } else
                    objectJ.put(key, itemC.getString(key, null));
            }
        }
    }
}
