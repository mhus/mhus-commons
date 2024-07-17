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

import de.mhus.commons.errors.MRuntimeException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.tools.MCast;
import de.mhus.commons.tools.MString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * A INode extends the concept of properties to a object oriented structure. A property can also be an object or array
 * of objects. The INode will not really separate objects and arrays. If you require an array and it's only a single
 * objects you will get a list with a single object and vies versa.
 *
 * @author mikehummel
 */
public interface ITreeNode extends IProperties {

    Logger LOGGER = LoggerFactory.getLogger(ITreeNode.class);

    public static final String NAMELESS_VALUE = "";
    // public static final String VALUE = "value";
    // public static final String VALUES = "values";
    // public static final String ID = "_id";
    public static final String HELPER_VALUE = "_";
    public static final String CLASS = "_class";
    public static final String NULL = "_null";

    /**
     * Returns true if t@Override he key is an object.
     *
     * @param key
     *
     * @return If the property is an object or array
     */
    boolean isObject(String key);

    Optional<ITreeNode> getObject(String key);

    boolean isArray(String key);

    Optional<TreeNodeList> getArray(String key);

    Optional<ITreeNode> getObjectByPath(String path);

    Optional<TreeNodeList> getArrayByPath(String path);

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
     * Return in every case a list. An Array List or list with a single Object or a object with nameless value or an
     * empty list.
     *
     * @param key
     *
     * @return A list
     */
    TreeNodeList getList(String key);

    /**
     * Return a iterator over a array or a single object. Return an empty iterator if not found. Use this function to
     * iterate over arrays or objects.
     *
     * @param key
     *
     * @return Never null.
     */
    List<ITreeNode> getObjectList(String key);

    List<String> getObjectAndArrayKeys();

    List<String> getArrayKeys();

    TreeNodeList createArray(String key);

    // INode cloneObject(INode node);

    default <T extends TreeNodeSerializable> T load(T fillIn) {
        if (fillIn == null)
            return null;
        if (getBoolean(NULL, false))
            return null;
        try {
            fillIn.readSerializabledNode(this);
        } catch (Exception e) {
            throw new MRuntimeException(RC.STATUS.ERROR, fillIn, this, e);
        }
        return fillIn;
    }

    /**
     * Return true if no value is in list from type INode or NodeList. Other Objects will be seen as flat.
     *
     * @return true if compatible with IProperties
     */
    boolean isProperties();

    /**
     * Return the value in every case as INode object. Even if it's not found it will return null. The result could be a
     * new object not attached to the underlying map. Changes may have no affect to the parent node.
     *
     * @param key
     *
     * @return The INode
     */
    ITreeNode getAsObject(String key);

    TreeNodeList getParentArray();

    /**
     * find or create a node in a node path. Path elements separated by slash and can have indexes wih brackets e.g.
     * nr1/nr2[4]/nr3
     *
     * @param root
     *            Root element
     * @param path
     *            The path to the node
     *
     * @return
     */
    static ITreeNode findOrCreateNode(ITreeNode root, String path) {

        if (path.startsWith("/"))
            path = path.substring(1);
        if (path.length() == 0)
            return root;

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
            while (array.size() < index + 1)
                array.createObject();
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

}
