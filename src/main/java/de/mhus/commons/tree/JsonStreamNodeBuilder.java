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

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.util.NullValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map.Entry;

public class JsonStreamNodeBuilder extends ITreeNodeBuilder {

    private JsonFactory factory = new JsonFactory();
    private boolean pretty = false;

    @Override
    public ITreeNode read(InputStream is) throws MException {
        ITreeNode root = null;
        ITreeNode node = null;
        TreeNodeList array = null;
        try {
            try (JsonParser jsonParser = factory.createParser(is)) {

                while (true) {
                    JsonToken token = jsonParser.nextToken();
                    if (token == null)
                        break;
                    if (token == JsonToken.FIELD_NAME)
                        continue;
                    if (root == null) {
                        // START ARRAY OR OBJECT
                        if (token == JsonToken.START_ARRAY) {
                            root = new TreeNode();
                            node = root;
                            array = node.createArray(ITreeNode.NAMELESS_VALUE);
                        } else if (token == JsonToken.START_OBJECT) {
                            root = new TreeNode();
                            node = root;
                            array = null;
                        }
                    } else if (array != null) {
                        // IN ARRAY
                        if (token == JsonToken.START_ARRAY) {
                            ITreeNode obj = array.createObject();
                            array = obj.createArray(ITreeNode.NAMELESS_VALUE);
                            node = null;
                        } else if (token == JsonToken.START_OBJECT) {
                            node = array.createObject();
                            array = null;
                        } else if (token == JsonToken.END_OBJECT) {
                            // should not happen
                        } else if (token == JsonToken.END_ARRAY) {
                            if (ITreeNode.NAMELESS_VALUE.equals(array.getName()) && array.getParent() != null
                                    && array.getParent().getParentArray() != null) {
                                ITreeNode obj = array.getParent();
                                array = obj.getParentArray();
                                node = null;
                            } else {
                                node = array.getParent();
                                array = null;
                            }
                        } else if (token == JsonToken.VALUE_STRING)
                            array.createObject().setString(jsonParser.getCurrentName(), jsonParser.getValueAsString());
                        else if (token == JsonToken.VALUE_FALSE)
                            array.createObject().setBoolean(jsonParser.getCurrentName(), false);
                        else if (token == JsonToken.VALUE_TRUE)
                            array.createObject().setBoolean(jsonParser.getCurrentName(), true);
                        else if (token == JsonToken.VALUE_NUMBER_FLOAT)
                            array.createObject().setDouble(jsonParser.getCurrentName(), jsonParser.getDoubleValue());
                        else if (token == JsonToken.VALUE_NUMBER_INT)
                            array.createObject().setLong(jsonParser.getCurrentName(), jsonParser.getLongValue());
                    } else if (node != null) {
                        // IN OBJECT
                        if (token == JsonToken.START_ARRAY) {
                            array = node.createArray(jsonParser.getCurrentName());
                            node = null;
                        } else if (token == JsonToken.START_OBJECT) {
                            node = node.createObject(jsonParser.getCurrentName());
                            array = null;
                        } else if (token == JsonToken.END_OBJECT) {
                            if (node.getParentArray() != null) {
                                array = node.getParentArray();
                                node = null;
                            } else {
                                node = node.getParent();
                                array = null;
                            }
                        } else if (token == JsonToken.VALUE_STRING)
                            node.setString(jsonParser.getCurrentName(), jsonParser.getValueAsString());
                        else if (token == JsonToken.END_ARRAY) {
                            // should not happen
                        } else if (token == JsonToken.VALUE_FALSE)
                            node.setBoolean(jsonParser.getCurrentName(), false);
                        else if (token == JsonToken.VALUE_TRUE)
                            node.setBoolean(jsonParser.getCurrentName(), true);
                        else if (token == JsonToken.VALUE_NUMBER_FLOAT)
                            node.setDouble(jsonParser.getCurrentName(), jsonParser.getDoubleValue());
                        else if (token == JsonToken.VALUE_NUMBER_INT)
                            node.setLong(jsonParser.getCurrentName(), jsonParser.getLongValue());
                    } else {
                        // should not happen
                    }
                }
            }

            if (root == null)
                root = new TreeNode();
            return root;
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    @Override
    public void write(ITreeNode node, OutputStream os) throws MException {
        try {

            JsonGenerator generator = factory.createGenerator(os, JsonEncoding.UTF8);
            if (pretty)
                generator.useDefaultPrettyPrinter();
            write(node, generator);
            generator.close();
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    private void write(ITreeNode node, JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        for (Entry<String, Object> entry : node.entrySet()) {
            if (entry.getValue() instanceof ITreeNode) {
                gen.writeFieldName(entry.getKey());
                write((ITreeNode) entry.getValue(), gen);
            } else if (entry.getValue() instanceof TreeNodeList) {
                gen.writeFieldName(entry.getKey());
                write((TreeNodeList) entry.getValue(), gen);
            } else if (entry.getValue() instanceof Boolean)
                gen.writeBooleanField(entry.getKey(), (Boolean) entry.getValue());
            else if (entry.getValue() instanceof String)
                gen.writeStringField(entry.getKey(), (String) entry.getValue());
            else if (entry.getValue() instanceof Date)
                gen.writeNumberField(entry.getKey(), ((Date) entry.getValue()).getTime());
            else if (entry.getValue() instanceof NullValue)
                gen.writeNullField(entry.getKey());
            else if (entry.getValue() instanceof Integer)
                gen.writeNumberField(entry.getKey(), (Integer) entry.getValue());
            else if (entry.getValue() instanceof Long)
                gen.writeNumberField(entry.getKey(), (Long) entry.getValue());
            else if (entry.getValue() instanceof Float)
                gen.writeNumberField(entry.getKey(), (Float) entry.getValue());
            else if (entry.getValue() instanceof Double)
                gen.writeNumberField(entry.getKey(), (Double) entry.getValue());
            else if (entry.getValue() instanceof Short)
                gen.writeNumberField(entry.getKey(), (Short) entry.getValue());
            else if (entry.getValue() instanceof BigInteger) {
                gen.writeFieldName(entry.getKey());
                gen.writeNumber((BigInteger) entry.getValue());
            } else if (entry.getValue() instanceof BigDecimal)
                gen.writeNumberField(entry.getKey(), (BigDecimal) entry.getValue());
            else
                gen.writeStringField(entry.getKey(), String.valueOf(entry.getValue()));
        }

        gen.writeEndObject();
    }

    private void write(TreeNodeList list, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (ITreeNode entry : list) {
            write(entry, gen);
        }
        gen.writeEndArray();
    }

    public boolean isPretty() {
        return pretty;
    }

    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }
}
