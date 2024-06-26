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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.mhus.commons.tools.MJson;
import de.mhus.commons.tools.MObject;
import de.mhus.commons.errors.NotFoundRuntimeException;
import de.mhus.commons.tree.ITreeNode;
import de.mhus.commons.tree.TreeNodeList;
import de.mhus.commons.tree.TreeNodeSerializable;
import de.mhus.commons.pojo.MPojo;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class TableRow implements Serializable, TreeNodeSerializable {

    private static final long serialVersionUID = 1L;
    LinkedList<Object> data = new LinkedList<>();
    private Table table;
    private ClassLoader cl = Thread.currentThread().getContextClassLoader();

    public List<Object> getData() {
        return data;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeInt(data.size());
        for (Object d : data) {
            // write data
            if (d == null || d instanceof Serializable) {
                // via java default
                out.writeInt(0);
                out.writeObject(d);
            } else {
                // via pojo
                out.writeInt(1);
                ObjectNode to = MJson.createObjectNode();
                MPojo.pojoToJson(d, to);
                out.writeUTF(d.getClass().getCanonicalName());
                out.writeUTF(MJson.toString(to));
            }
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int size = in.readInt();
        // data.clear(); Create new one because of concurrent modifications
        data = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            int code = in.readInt();
            if (code == 0) {
                Object d = in.readObject();
                data.add(d);
            } else if (code == 1) {
                String clazzName = in.readUTF();
                Object obj;
                try {
                    obj = MObject.newInstance(cl, clazzName);
                } catch (Exception e) {
                    throw new IOException(e);
                }
                String jsonString = in.readUTF();
                JsonNode json = MJson.load(jsonString);
                MPojo.jsonToPojo(json, obj);
                data.add(obj);
            }
        }
    }

    @Override
    public void readSerializabledNode(ITreeNode cfg) throws Exception {
        for (ITreeNode cell : cfg.getArray("data").orElseGet(() -> cfg.createArray("data"))) {
            Object obj = MPojo.nodeToPojoObject(cell);
            data.add(obj);
        }
    }

    @Override
    public void writeSerializabledNode(ITreeNode cfg) throws Exception {
        TreeNodeList arr = cfg.createArray("data");
        for (Object d : data) {
            ITreeNode cell = arr.createObject();
            MPojo.pojoToNode(d, cell, true, false);
        }
    }

    public void appendData(Object... d) {
        for (Object o : d)
            data.add(o);
    }

    public void setData(Object... d) {
        data.clear();
        for (Object o : d)
            data.add(o);
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Object get(int index) {
        if (index < 0 || index >= data.size())
            throw new NotFoundRuntimeException("column index not found", index);
        return data.get(index);
    }

    public Object get(String name) {
        int index = table.getColumnIndex(name);
        if (index == -1)
            throw new NotFoundRuntimeException("column not found", name);
        return get(index);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name, T def) {
        int index = table.getColumnIndex(name);
        if (index == -1)
            return def;
        Object val = get(index);
        if (val == null)
            return def;
        return (T) val;
    }

    @Override
    public String toString() {
        return String.valueOf(data);
    }
}
