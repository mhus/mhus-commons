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

import de.mhus.commons.M;
import de.mhus.commons.lang.IsNull;
import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.MRuntimeException;
import de.mhus.commons.errors.MaxDepthReached;
import de.mhus.commons.errors.NotFoundException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.parser.CompiledString;
import de.mhus.commons.util.SingleList;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TreeNode extends MProperties implements ITreeNode {

    protected String name;
    protected ITreeNode parent;
    protected TreeNodeStringCompiler compiler;
    protected HashMap<String, CompiledString> compiledCache;
    protected TreeNodeList array;

    public TreeNode() {}

    public TreeNode(String name) {
        this.name = name;
    }

    public TreeNode(String name, TreeNodeList array) {
        this.name = name;
        this.array = array;
        if (array != null) this.parent = array.getParent();
    }

    public TreeNode(String name, ITreeNode parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public boolean isObject(String key) {
        Object val = get(key);
        if (val == null) return false;
        return val instanceof ITreeNode;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public ITreeNode getAsObject(String key) {
        Object val = get(key);
        if (val == null) return null;
        if (val instanceof ITreeNode) return (ITreeNode) val;
        if (val instanceof IProperties) return new TreeNodeWrapper((IProperties) val);

        TreeNode ret = new TreeNode();
        if (val instanceof Map) {
            ret.putAll((Map) val);
        } else ret.put(NAMELESS_VALUE, val);
        return ret;
    }

    @Override
    public Optional<ITreeNode> getObject(String key) {
        Object val = get(key);
        if (val == null) return Optional.empty();
        if (val instanceof ITreeNode) return Optional.of((ITreeNode) val);
        return Optional.empty();
    }

    @Override
    public boolean isArray(String key) {
        Object val = get(key);
        if (val == null) return false;
        return val instanceof TreeNodeList;
    }

    @Override
    public Optional<TreeNodeList> getArray(String key) {
        Object val = get(key);
        if (val == null) return Optional.empty();
        if (val instanceof List) return Optional.of((TreeNodeList) val);
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ITreeNode> getObjectList(String key) {
        Object val = get(key);
        if (val == null) return (List<ITreeNode>) Collections.EMPTY_LIST;
        // if (val == null) throw new NotFoundException("value not found",key);
        if (val instanceof ITreeNode) return new SingleList<ITreeNode>((ITreeNode) val);
        if (val instanceof TreeNodeList) return Collections.unmodifiableList((TreeNodeList) val);
        return (List<ITreeNode>) Collections.EMPTY_LIST;
        // throw new NotFoundException("value is not a NodeList or INode",key);
    }

    @Override
    public ITreeNode getObjectByPath(String path) {
        if (path == null) return null;
        if (path.equals("") || path.equals(".")) return this;
        while (path.startsWith("/")) path = path.substring(1);
        if (path.length() == 0) return this;
        int p = path.indexOf('/');
        if (p < 0) return getObject(path).orElse(null);
        ITreeNode next = getObject(path.substring(0, p)).orElse(null);
        if (next == null) return null;
        return next.getObjectByPath(path.substring(p + 1));
    }

    @Override
    public String getExtracted(String key, String def) {
        return getExtracted(key, def, 0);
    }

    @Override
    public String getExtracted(String key) {
        return getExtracted(key, null);
    }

    @Override
    public TreeNodeList getList(String key) {
        if (isArray(key)) return getArray(key).orElse(null);
        if (isObject(key)) {
            TreeNodeList ret = new TreeNodeList(key, this);
            ret.add(getObject(key).orElse(null));
            return ret;
        }
        if (containsKey(key)) {
            TreeNodeList ret = new TreeNodeList(key, this);
            TreeNode obj = new TreeNode(key, this);
            obj.put(NAMELESS_VALUE, get(key));
            ret.add(obj);
            return ret;
        }
        return new TreeNodeList(key, this);
    }

    protected String getExtracted(String key, String def, int level) {

        if (level > 10) return def;

        String value = getString(key, null);

        if (value == null) return def;
        if (value.indexOf('$') < 0) return value;

        synchronized (this) {
            if (compiler == null) {
                compiler = new TreeNodeStringCompiler(this);
                compiledCache = new HashMap<String, CompiledString>();
            }
            CompiledString cached = compiledCache.get(key);
            if (cached == null) {
                cached = compiler.compileString(value);
                compiledCache.put(key, cached);
            }
            try {
                return cached.execute(new TreeNodeStringCompiler.NodeMap(level, this));
            } catch (MException e) {
                throw new MRuntimeException(RC.STATUS.ERROR, key, e);
            }
        }
    }

    @Override
    public List<ITreeNode> getObjects() {
        ArrayList<ITreeNode> out = new ArrayList<>();
        for (Object val : values()) {
            if (val instanceof ITreeNode) out.add((ITreeNode) val);
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public void setObject(String key, ITreeNode object) {
        if (object == null) {
            remove(key);
            return;
        }
        ((TreeNode) object).parent = this;
        ((TreeNode) object).name = key;
        put(key, object);
    }

    @Override
    public void addObject(String key, ITreeNode object) {
        Object obj = get(key);
        if (obj != null) {
            if (obj instanceof TreeNodeList) {
                ((TreeNodeList) obj).add(object);
            } else if (obj instanceof ITreeNode) {
                LinkedList<Object> list = new LinkedList<>();
                list.add(obj);
                put(key, obj);
                list.add(object);
            } else {
                // overwrite non object and arrays
                TreeNodeList list = new TreeNodeList(key, this);
                put(key, list);
                list.add(object);
            }
        } else {
            setObject(key, object);
            return;
        }
    }

    @Override
    public ITreeNode setObject(String key, TreeNodeSerializable object) {
        ITreeNode cfg = createObject(key);
        try {
            object.writeSerializabledNode(cfg);
        } catch (Exception e) {
            throw new MRuntimeException(RC.STATUS.ERROR, e);
        }
        return cfg;
    }

    @Override
    public ITreeNode createObject(String key) {
        ITreeNode obj = new TreeNode();
        addObject(key, obj);
        return obj;
    }

    @Override
    public TreeNodeList createArray(String key) {
        TreeNodeList list = new TreeNodeList(key, this);
        put(key, list);
        return list;
    }

    /**
     * Return only the property keys without objects and arrays.
     *
     * @return The property keys
     */
    @Override
    public List<String> getPropertyKeys() {
        ArrayList<String> out = new ArrayList<>();
        for (Entry<String, Object> entry : entrySet()) {
            if (!(entry.getValue() instanceof ITreeNode) && !(entry.getValue() instanceof TreeNodeList))
                out.add(entry.getKey());
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ITreeNode getParent() {
        return parent;
    }

    @Override
    public TreeNodeList getParentArray() {
        return array;
    }

    @Override
    public List<String> getObjectKeys() {
        ArrayList<String> out = new ArrayList<>();
        for (Entry<String, Object> entry : entrySet()) {
            if (entry.getValue() instanceof ITreeNode) out.add(entry.getKey());
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public List<String> getArrayKeys() {
        ArrayList<String> out = new ArrayList<>();
        for (Entry<String, Object> entry : entrySet()) {
            if (entry.getValue() instanceof TreeNodeList) out.add(entry.getKey());
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public List<String> getObjectAndArrayKeys() {
        ArrayList<String> out = new ArrayList<>();
        for (Entry<String, Object> entry : entrySet()) {
            if (entry.getValue() instanceof ITreeNode || entry.getValue() instanceof TreeNodeList)
                out.add(entry.getKey());
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public synchronized String toString() {
        return (name == null || array != null ? "" : name + ":") + super.toString();
    }

    @Override
    public boolean isProperties() {
        for (Object val : values())
            if ((val instanceof TreeNodeList) || (val instanceof ITreeNode)) return false;
        return true;
    }

    public void putMapToNode(Map<?, ?> m) {
        putMapToNode(m, 0);
    }

    protected void putMapToNode(Map<?, ?> m, int level) {
        if (level > M.MAX_DEPTH_LEVEL) throw new MaxDepthReached();
        for (Map.Entry<?, ?> e : m.entrySet())
            if (e.getValue() instanceof IsNull) remove(e.getKey());
            else {
                if (e.getValue() instanceof Map) {
                    TreeNode cfg = new TreeNode();
                    cfg.putMapToNode((Map<?, ?>) e.getValue(), level + 1);
                    put(String.valueOf(e.getKey()), cfg);
                } else if (e.getValue() instanceof List) {
                    TreeNodeList list = new TreeNodeList(String.valueOf(e.getKey()), null);
                    put(String.valueOf(e.getKey()), list);
                    for (Object obj : ((List<?>) e.getValue())) {
                        if (obj instanceof ITreeNode) {
                            list.add((ITreeNode) obj);
                        } else {
                            TreeNode cfg = (TreeNode) list.createObject();
                            if (obj instanceof Map) {
                                cfg.putMapToNode((Map<?, ?>) obj, level + 1);
                            } else cfg.put(NAMELESS_VALUE, obj);
                        }
                    }
                } else put(String.valueOf(e.getKey()), e.getValue());
            }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(name);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        name = (String) in.readObject();
    }
}
