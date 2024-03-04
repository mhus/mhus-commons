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

import de.mhus.commons.tools.MSystem;
import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.MRuntimeException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.TooDeepStructuresException;
import de.mhus.commons.pojo.MPojo;
import de.mhus.commons.tools.MCollection;
import de.mhus.commons.tools.MDate;
import de.mhus.commons.tools.MString;
import de.mhus.commons.util.NullValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PropertiesNodeBuilder extends ITreeNodeBuilder {

    protected static final int CFG_MAX_LEVEL =
            MSystem.getEnv(PropertiesNodeBuilder.class,"maxLevel", 100);

    @Override
    public ITreeNode read(InputStream is) throws MException {
        try {
            MProperties p = MProperties.load(is);
            return readFromMap(p);
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    @Override
    public void write(ITreeNode node, OutputStream os) throws MException {
        MProperties p = new MProperties(node);
        try {
            p.save(os);
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    public ITreeNode readFromMap(Map<?, ?> map) {
        return readFromMap(map, 0);
    }

    public ITreeNode readFromCollection(Collection<?> col) {
        ITreeNode node = new TreeNode();
        readFromCollection(node, ITreeNode.NAMELESS_VALUE, col, 0);
        return node;
    }

    protected void readFromCollection(ITreeNode node, String key, Collection<?> col, int level) {
        level++;
        if (level > CFG_MAX_LEVEL) throw new TooDeepStructuresException();

        TreeNodeList arr = node.createArray(key);
        for (Object item : col) {
            ITreeNode obj = readObject(item, level);
            arr.add(obj);
        }
    }

    protected ITreeNode readFromMap(Map<?, ?> map, int level) {
        level++;
        if (level > CFG_MAX_LEVEL) throw new TooDeepStructuresException();

        ITreeNode node = new TreeNode();
        for (Entry<?, ?> entry : map.entrySet()) {
            String key = MString.valueOf(entry.getKey());
            Object val = entry.getValue();
            if (val == null || val instanceof NullValue) {
                // null object or ignore ?
            } else if (val instanceof String
                    || val.getClass().isPrimitive()
                    || val instanceof Number
                    || val instanceof Date
                    || val instanceof Boolean) {
                node.put(key, val);
            } else {
                ITreeNode obj = readObject(val, level);
                if (obj.isProperty(ITreeNode.NAMELESS_VALUE)) {
                    node.put(key, obj.get(ITreeNode.NAMELESS_VALUE));
                    if (node.isProperty(ITreeNode.HELPER_VALUE))
                        node.put(ITreeNode.HELPER_VALUE + key, node.get(ITreeNode.HELPER_VALUE));
                } else if (obj.isObject(ITreeNode.NAMELESS_VALUE)) {
                    node.addObject(key, obj.getObject(ITreeNode.NAMELESS_VALUE).orElse(null));
                } else node.addObject(key, obj);
            }
        }
        return node;
    }

    public ITreeNode readObject(Object item) {
        return readObject(item, 0);
    }

    protected ITreeNode readObject(Object item, int level) {
        level++;
        if (level > CFG_MAX_LEVEL) throw new TooDeepStructuresException();

        if (item == null) {
            TreeNode obj = new TreeNode();
            obj.setBoolean(ITreeNode.NULL, true);
            return obj;
        } else if (item instanceof TreeNodeSerializable) {
            TreeNode obj = new TreeNode();
            try {
                ((TreeNodeSerializable) item).writeSerializabledNode(obj);
            } catch (Exception e) {
                throw new MRuntimeException(RC.STATUS.ERROR, item, e);
            }
            return obj;
        } else if (item instanceof ITreeNode) {
            return (ITreeNode) item;
        } else if (item instanceof Map) {
            ITreeNode obj = readFromMap((Map<?, ?>) item, level);
            return obj;
        } else if (item instanceof String
                || item.getClass().isPrimitive()
                || item instanceof Number
                || item instanceof Date
                || item instanceof Boolean) {
            TreeNode obj = new TreeNode();
            obj.put(ITreeNode.NAMELESS_VALUE, item);
            return obj;
        } else if (item instanceof Date) {
            TreeNode obj = new TreeNode();
            obj.put(ITreeNode.NAMELESS_VALUE, ((Date) item).getTime());
            obj.put(ITreeNode.HELPER_VALUE, MDate.toIso8601((Date) item));
            return obj;
        } else if (item.getClass().isArray()) {
            TreeNode obj = new TreeNode();
            obj.setString(ITreeNode.CLASS, item.getClass().getCanonicalName());
            readFromCollection(
                    obj, ITreeNode.NAMELESS_VALUE, MCollection.toList(((Object[]) item)), level);
            return obj;
        } else if (item instanceof Collection) {
            TreeNode obj = new TreeNode();
            obj.setString(ITreeNode.CLASS, item.getClass().getCanonicalName());
            readFromCollection(obj, ITreeNode.NAMELESS_VALUE, (Collection<?>) item, level);
            return obj;
        } else {
            TreeNode obj = new TreeNode();
            try {
                MPojo.pojoToNode(item, obj);
            } catch (IOException e) {
                throw new MRuntimeException(RC.STATUS.ERROR, item, e);
            }
            return obj;
        }
    }

    public <T extends TreeNodeSerializable> List<T> loadToCollection(ITreeNode source, Class<T> target)
            throws Exception {
        ArrayList<T> out = new ArrayList<>();
        for (ITreeNode entry : source.getArray(ITreeNode.NAMELESS_VALUE).get()) {
            T inst = target.getConstructor().newInstance();
            inst.readSerializabledNode(entry);
            out.add(inst);
        }
        return out;
    }

    public <V extends TreeNodeSerializable> Map<String, V> loadToMap(ITreeNode source, Class<V> target)
            throws Exception {
        HashMap<String, V> out = new HashMap<>();
        for (ITreeNode entry : source.getObjects()) {
            V inst = target.getConstructor().newInstance();
            inst.readSerializabledNode(entry);
            out.put(entry.getName(), inst);
        }
        return out;
    }
}
