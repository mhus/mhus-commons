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
package de.mhus.commons.tree;

import de.mhus.commons.M;
import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.MRuntimeException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.TooDeepStructuresException;
import de.mhus.commons.tools.MCast;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MXml;
import de.mhus.commons.util.MUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A INode extends the concept of properties to a object oriented structure. A property can also be
 * an object or array of objects. The INode will not really separate objects and arrays. If you
 * require an array and it's only a single objects you will get a list with a single object and vies
 * versa.
 *
 * @author mikehummel
 */
public interface ITreeNode extends IProperties {

    Logger LOGGER = LoggerFactory.getLogger(ITreeNode.class);

    public static final String NAMELESS_VALUE = "";
    public static final String VALUE = "value";
    public static final String VALUES = "values";
    public static final String ID = "_id";
    public static final String HELPER_VALUE = "_";
    public static final String CLASS = "_class";
    public static final String NULL = "_null";

    /**
     * Returns true if t@Override he key is an object.
     *
     * @param key
     * @return If the property is an object or array
     */
    boolean isObject(String key);

    Optional<ITreeNode> getObject(String key);

    boolean isArray(String key);

    Optional<TreeNodeList> getArray(String key);

    ITreeNode getObjectByPath(String path);

    String getExtracted(String key, String def);

    String getExtracted(String key);

    List<ITreeNode> getObjects();

    void setObject(String key, ITreeNode object);

    /**
     * Add the Object to a list of objects named with key.
     *
     * @param key
     * @param object
     */
    void addObject(String key, ITreeNode object);

    ITreeNode setObject(String key, TreeNodeSerializable object);

    ITreeNode createObject(String key);

    List<String> getPropertyKeys();

    String getName();

    ITreeNode getParent();

    List<String> getObjectKeys();

    /**
     * Return in every case a list. An Array List or list with a single Object or a object with
     * nameless value or an empty list.
     *
     * @param key
     * @return A list
     */
    TreeNodeList getList(String key);

    /**
     * Return a iterator over a array or a single object. Return an empty iterator if not found. Use
     * this function to iterate over arrays or objects.
     *
     * @param key
     * @return Never null.
     */
    List<ITreeNode> getObjectList(String key);

    List<String> getObjectAndArrayKeys();

    List<String> getArrayKeys();

    TreeNodeList createArray(String key);

    //    INode cloneObject(INode node);

    default <T extends TreeNodeSerializable> T load(T fillIn) {
        if (fillIn == null) return null;
        if (getBoolean(NULL, false)) return null;
        try {
            fillIn.readSerializabledNode(this);
        } catch (Exception e) {
            throw new MRuntimeException(RC.STATUS.ERROR, fillIn, this, e);
        }
        return fillIn;
    }

    /**
     * Transform a object into INode.
     *
     * @param object
     * @return INode
     * @throws Exception
     */
    static ITreeNode read(TreeNodeSerializable object) throws Exception {
        ITreeNode cfg = new TreeNode();
        if (object == null) cfg.setBoolean(ITreeNode.NULL, true);
        else object.writeSerializabledNode(cfg);
        return cfg;
    }

    /**
     * Return a node or null if the string is not understand.
     *
     * @param nodeString
     * @return A node object if the node is found or null. If no node is recognized it returns null
     * @throws MException
     */
    static ITreeNode readNodeFromString(String nodeString) throws MException {
        if (MString.isEmptyTrim(nodeString)) return new TreeNode();
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

        if (nodeString.contains("=")) {
            if (nodeString.contains("&"))
                return readFromProperties(new HashMap<>(MUri.explode(nodeString)));
            else return readFromProperties(IProperties.explodeToMProperties(nodeString));
        }

        return null;
    }

    /**
     * Return a node or null if the string is not understand.
     *
     * @param nodeStrings
     * @return INode, never null
     * @throws MException
     */
    static ITreeNode readNodeFromString(String[] nodeStrings) throws MException {
        if (nodeStrings == null || nodeStrings.length == 0) return new TreeNode();
        if (nodeStrings.length == 1) return readNodeFromString(nodeStrings[0]);
        return readFromProperties(IProperties.explodeToMProperties(nodeStrings));
    }

    static ITreeNode readFromProperties(Map<String, Object> lines) {
        return new PropertiesNodeBuilder().readFromMap(lines);
    }

    static ITreeNode readFromMap(Map<?, ?> lines) {
        return new PropertiesNodeBuilder().readFromMap(lines);
    }

    static <V extends TreeNodeSerializable> Map<String, V> loadToMap(ITreeNode source, Class<V> target)
            throws Exception {
        return new PropertiesNodeBuilder().loadToMap(source, target);
    }

    static ITreeNode readFromCollection(Collection<?> lines) {
        return new PropertiesNodeBuilder().readFromCollection(lines);
    }

    static <T extends TreeNodeSerializable> List<T> loadToCollection(ITreeNode source, Class<T> target)
            throws Exception {
        return new PropertiesNodeBuilder().loadToCollection(source, target);
    }

    static ITreeNode readFromJsonString(String json) throws MException {
        return new JsonStreamNodeBuilder().readFromString(json);
    }

    static ITreeNode readFromXmlString(Element documentElement) throws MException {
        return new XmlTreeNodeBuilder().readFromElement(documentElement);
    }

    static ITreeNode readFromYamlString(String yaml) throws MException {
        return new YamlTreeNodeBuilder().readFromString(yaml);
    }

    static String toCompactJsonString(ITreeNode node) throws MException {
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

    static String toPrettyJsonString(ITreeNode node) throws MException {
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
        if (level > 100) throw new TooDeepStructuresException();

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
            if (value != null) out.add(value);
        }
        return out.toArray(new String[out.size()]);
    }

    /**
     * Try to un serialize the object with the node. If it fails null will be returned.
     *
     * @param <T> Type
     * @param node Node with serialized data
     * @param fillIn The object to fill
     * @return The fillIn object or null
     */
    static <T extends TreeNodeSerializable> Optional<T> load(ITreeNode node, T fillIn) {
        if (fillIn == null || node == null) return Optional.empty();
        try {
            fillIn.readSerializabledNode(node);
        } catch (Exception e) {
            LOGGER.debug("deserialize of {} failed", node, e);
            return Optional.empty();
        }
        return Optional.of(fillIn);
    }

    /**
     * Return a wrapped parameter to node object. If the wrapped object is changes also values in
     * the original object will be changed.
     *
     * @param parameters
     * @return A wrapping INode object
     */
    static ITreeNode wrap(IProperties parameters) {
        return new TreeNodeWrapper(parameters);
    }

    /**
     * Return true if no value is in list from type INode or NodeList. Other Objects will be seen as
     * flat.
     *
     * @return true if compatible with IProperties
     */
    boolean isProperties();

    /**
     * Return the value in every case as INode object. Even if it's not found it will return null.
     * The result could be a new object not attached to the underlying map. Changes may have no
     * affect to the parent node.
     *
     * @param key
     * @return The INode
     */
    ITreeNode getAsObject(String key);

    TreeNodeList getParentArray();

    /**
     * find or create a node in a node path. Path elements separated by slash and can have indexes
     * wih brackets e.g. nr1/nr2[4]/nr3
     *
     * @param root Root element
     * @param path The path to the node
     * @return
     */
    static ITreeNode findOrCreateNode(ITreeNode root, String path) {

        if (path.startsWith("/")) path = path.substring(1);
        if (path.length() == 0) return root;

        TreeNode next = null;
        int pos = path.indexOf('/');
        String name = pos >= 0 ? path.substring(0, pos) : path;
        name = name.trim();
        if (name.endsWith("]")) {
            // array
            int index = MCast.toint(MString.beforeIndex(MString.afterIndex(name, '['), ']'), -1);
            name = MString.beforeIndex(name, '[');
            final var fName = name;
            TreeNodeList array = root.getArray(fName).orElseGet(() -> root.createArray(fName));
            while (array.size() < index + 1) array.createObject();
            next = (TreeNode) array.get(index);
        } else {
            next = (TreeNode) root.getObject(name).orElse(null);
            if (next == null) {
                next = new TreeNode();
                root.addObject(name, next);
            }
        }
        return pos < 0 ? next : findOrCreateNode(next, path.substring(pos + 1));
    }

    static String getPath(ITreeNode node) {
        StringBuilder sb = new StringBuilder();
        getPath(node, sb, 0);
        if (sb.length() == 0) sb.append("/");
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
            if (parent != null) getPath(parent, sb, level + 1);
        } else if (parent != null) {
            sb.insert(0, node.getName());
            sb.insert(0, "/");
            getPath(parent, sb, level + 1);
        }
    }
}
