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

import de.mhus.commons.M;
import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.NotSupportedException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.TooDeepStructuresException;
import de.mhus.commons.services.MService;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MXml;
import de.mhus.commons.util.MUri;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class MTree {

    public static final TreeNodeList EMPTY_LIST = new EmptyTreeNodeList();
    public static final TreeNode EMPTY_MAP = new EmptyTreeNode();

    public static ITreeNode load(File file) throws MException {
        return MService.getService(ITreeNodeFactory.class).read(file);
    }

    public static ITreeNode load(File parent, String name) throws MException {
        return MService.getService(ITreeNodeFactory.class).find(parent, name);
    }

    // TODO: implement
    // public static ITreeNode load(String content) throws MException {
    // return MService.getService(ITreeNodeFactory.class).read(content);
    // }

    public static void save(ITreeNode node, File file) throws MException {
        MService.getService(ITreeNodeFactory.class).write(node, file);
    }

    public static ITreeNode create() {
        return MService.getService(ITreeNodeFactory.class).create();
    }

    public static List<String> getArrayValueStringList(TreeNodeList array) {
        return array.stream().map((entry) -> entry.getString(ITreeNode.NAMELESS_VALUE).orElse("")).toList();
    }

    public static String getParentPath(String path) {
        int pos = path.lastIndexOf('/');
        if (pos < 0)
            return "";
        return path.substring(0, pos);
    }

    public static String getNodeName(String path) {
        int pos = path.lastIndexOf('/');
        if (pos < 0)
            return path;
        return path.substring(pos + 1);
    }

    /**
     * Transform a object into INode.
     *
     * @param object
     *
     * @return INode
     *
     * @throws Exception
     */
    public static ITreeNode read(TreeNodeSerializable object) throws Exception {
        ITreeNode cfg = new TreeNode();
        if (object == null)
            cfg.setBoolean(ITreeNode.NULL, true);
        else
            object.writeSerializabledNode(cfg);
        return cfg;
    }

    /**
     * Return a node or null if the string is not understand.
     *
     * @param nodeString
     *
     * @return A node object if the node is found or null. If no node is recognized it returns null
     *
     * @throws MException
     */
    public static ITreeNode readNodeFromString(String nodeString) throws MException {
        if (MString.isEmptyTrim(nodeString))
            return new TreeNode();
        if (nodeString.startsWith("[") || nodeString.startsWith("{")) {
            try {
                return readFromJsonString(nodeString);
            } catch (Exception e) {
                throw new MException(RC.STATUS.ERROR, nodeString, e);
            }
        }
        if (nodeString.startsWith("<?")) {
            try {
                return readFromXmlString(MXml.loadXml(nodeString).getDocumentElement());
            } catch (Exception e) {
                throw new MException(RC.STATUS.ERROR, nodeString, e);
            }
        }

        if (nodeString.contains(":")) {
            try {
                return readFromYamlString(nodeString);
            } catch (Exception e) {
            }
        }

        if (nodeString.contains("=")) {
            if (nodeString.contains("&"))
                return readFromProperties(new HashMap<>(MUri.explode(nodeString)));
            else
                return readFromProperties(IProperties.explodeToMProperties(nodeString));
        }

        throw new NotSupportedException("node string not recognized", nodeString);
    }

    /**
     * Return a node or null if the string is not understand.
     *
     * @param nodeStrings
     *
     * @return INode, never null
     *
     * @throws MException
     */
    public static ITreeNode readNodeFromString(String[] nodeStrings) throws MException {
        if (nodeStrings == null || nodeStrings.length == 0)
            return new TreeNode();
        if (nodeStrings.length == 1)
            return readNodeFromString(nodeStrings[0]);
        return readFromProperties(IProperties.explodeToMProperties(nodeStrings));
    }

    public static ITreeNode readFromProperties(Map<String, Object> lines) {
        return new PropertiesNodeBuilder().readFromMap(lines);
    }

    public static ITreeNode readFromMap(Map<?, ?> lines) {
        return new PropertiesNodeBuilder().readFromMap(lines);
    }

    public static <V extends TreeNodeSerializable> Map<String, V> loadToMap(ITreeNode source, Class<V> target)
            throws Exception {
        return new PropertiesNodeBuilder().loadToMap(source, target);
    }

    public static ITreeNode readFromCollection(Collection<?> lines) {
        return new PropertiesNodeBuilder().readFromCollection(lines);
    }

    public static <T extends TreeNodeSerializable> List<T> loadToCollection(ITreeNode source, Class<T> target)
            throws Exception {
        return new PropertiesNodeBuilder().loadToCollection(source, target);
    }

    public static ITreeNode readFromJsonString(String json) throws MException {
        return new JsonStreamNodeBuilder().readFromString(json);
    }

    public static ITreeNode readFromXmlString(Element documentElement) throws MException {
        return new XmlTreeNodeBuilder().readFromElement(documentElement);
    }

    public static ITreeNode readFromYamlString(String yaml) throws MException {
        return new YamlTreeNodeBuilder().readFromString(yaml);
    }

    public static String toCompactJsonString(ITreeNode node) throws MException {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JsonStreamNodeBuilder builder = new JsonStreamNodeBuilder();
            builder.setPretty(false);
            builder.write(node, os);
            return new String(os.toByteArray(), M.UTF_8);
        } catch (Exception e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    public static String toPrettyJsonString(ITreeNode node) throws MException {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JsonStreamNodeBuilder builder = new JsonStreamNodeBuilder();
            builder.setPretty(true);
            builder.write(node, os);
            return new String(os.toByteArray(), M.UTF_8);
        } catch (Exception e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    public static void merge(ITreeNode from, ITreeNode to) throws MException {
        merge(from, to, 0);
    }

    private static void merge(ITreeNode from, ITreeNode to, int level) throws MException {
        if (level > 100)
            throw new TooDeepStructuresException();

        for (ITreeNode node : from.getObjects()) {
            ITreeNode n = to.createObject(node.getName());
            for (String name : node.getPropertyKeys()) {
                n.put(name, node.get(name));
            }
            merge(node, (ITreeNode) n, level + 1);
        }
        for (String key : from.getArrayKeys()) {
            TreeNodeList toArray = to.createArray(key);
            for (ITreeNode node : from.getArray(key).orElse(null)) {
                ITreeNode n = toArray.createObject();
                for (String name : ((ITreeNode) node).getPropertyKeys()) {
                    n.put(name, node.get(name));
                }
                merge(node, (ITreeNode) n, level + 1);
            }
        }
        for (String name : from.getPropertyKeys()) {
            to.put(name, from.get(name));
        }
    }

    public static String[] toStringArray(Collection<ITreeNode> nodes, String key) {
        LinkedList<String> out = new LinkedList<>();
        for (ITreeNode item : nodes) {
            String value = item.getString(key, null);
            if (value != null)
                out.add(value);
        }
        return out.toArray(new String[out.size()]);
    }

    /**
     * Try to un serialize the object with the node. If it fails null will be returned.
     *
     * @param <T>
     *            Type
     * @param node
     *            Node with serialized data
     * @param fillIn
     *            The object to fill
     *
     * @return The fillIn object or null
     */
    public static <T extends TreeNodeSerializable> Optional<T> load(ITreeNode node, T fillIn) {
        if (fillIn == null || node == null)
            return Optional.empty();
        try {
            fillIn.readSerializabledNode(node);
        } catch (Exception e) {
            ITreeNode.LOGGER.debug("deserialize of {} failed", node, e);
            return Optional.empty();
        }
        return Optional.of(fillIn);
    }

    /**
     * Return a wrapped parameter to node object. If the wrapped object is changes also values in the original object
     * will be changed.
     *
     * @param parameters
     *
     * @return A wrapping INode object
     */
    public static ITreeNode wrap(IProperties parameters) {
        return new TreeNodeWrapper(parameters);
    }

    public static String getPath(ITreeNode node) {
        StringBuilder sb = new StringBuilder();
        getPath(node, sb, 0);
        if (sb.length() == 0)
            sb.append("/");
        return sb.toString();
    }

    private static void getPath(ITreeNode node, StringBuilder sb, int level) {
        if (level > M.MAX_DEPTH_LEVEL)
            throw new TooDeepStructuresException("too much node elements", sb);

        ITreeNode parent = node.getParent();
        TreeNodeList list = node.getParentArray();
        if (list != null) {
            int index = -1;
            for (int i = 0; i < list.size(); i++)
                if (list.get(i) == node) {
                    index = i;
                    break;
                }
            sb.insert(0, "]");
            sb.insert(0, index);
            sb.insert(0, "[");
            sb.insert(0, list.getName());
            sb.insert(0, "/");
            if (parent != null)
                getPath(parent, sb, level + 1);
        } else if (parent != null) {
            sb.insert(0, node.getName());
            sb.insert(0, "/");
            getPath(parent, sb, level + 1);
        }
    }

    private static class EmptyTreeNodeList extends TreeNodeList {
        private EmptyTreeNodeList() {
            super("", null);
        }

        @Override
        public boolean add(ITreeNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, ITreeNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ITreeNode remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ITreeNode set(int index, ITreeNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(IProperties e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ITreeNode add(TreeNodeSerializable object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends ITreeNode> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addFirst(ITreeNode e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addLast(ITreeNode e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ITreeNode createObject() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends ITreeNode> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void replaceAll(UnaryOperator<ITreeNode> operator) {
            throw new UnsupportedOperationException();
        }

    }

    private static class EmptyTreeNode extends TreeNode {
        @Override
        public ITreeNode put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends TreeNodeSerializable> T load(T fillIn) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addObject(String key, ITreeNode object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TreeNodeList createArray(String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ITreeNode createObject(String key) {
            return super.createObject(key);
        }

        @Override
        public void putMapToNode(Map<?, ?> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void putMapToNode(Map<?, ?> m, int level) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setObject(String key, ITreeNode object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ITreeNode setObject(String key, TreeNodeSerializable object) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBoolean(String key, boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCalendar(String key, Calendar value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDate(String key, Date value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDouble(String key, double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setFloat(String key, float value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setInt(String key, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLong(String key, long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setNumber(String key, Number value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setString(String key, String value) {
            throw new UnsupportedOperationException();
        }
    }
}
