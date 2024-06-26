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
package de.mhus.commons.pojo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.mhus.commons.tools.MCast;
import de.mhus.commons.tools.MCollection;
import de.mhus.commons.tools.MDate;
import de.mhus.commons.tools.MObject;
import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MSystem;
import de.mhus.commons.tools.MXml;
import de.mhus.commons.annotations.generic.Public;
import de.mhus.commons.annotations.pojo.Embedded;
import de.mhus.commons.annotations.pojo.Hidden;
import de.mhus.commons.cast.Caster;
import de.mhus.commons.json.TransformHelper;
import de.mhus.commons.tree.ITreeNode;
import de.mhus.commons.tree.IProperties;
import de.mhus.commons.tree.TreeNode;
import de.mhus.commons.tree.MProperties;
import de.mhus.commons.tree.TreeNodeList;
import de.mhus.commons.services.ClassLoaderProvider;
import de.mhus.commons.services.MService;
import de.mhus.commons.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Element;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

@Slf4j
public class MPojo {

    public static final String DEEP = "deep";

    private static final int MAX_LEVEL = 10;
    private static PojoModelFactory defaultModelFactory;

    private static PojoModelFactory attributesModelFactory;
    private static ClassLoader defaultClassLoader;

    public static synchronized PojoModelFactory getDefaultModelFactory() {
        if (defaultModelFactory == null)
            defaultModelFactory = new PojoModelFactory() {

                @Override
                public PojoModel createPojoModel(Class<?> pojoClass) {
                    PojoModel model = new PojoParser().parse(pojoClass, "_", null)
                            .filter(new DefaultFilter(true, false, false, false, true)).getModel();
                    return model;
                }
            };
        return defaultModelFactory;
    }

    public static synchronized PojoModelFactory getAttributesModelFactory() {
        if (attributesModelFactory == null)
            attributesModelFactory = new PojoModelFactory() {

                @Override
                public PojoModel createPojoModel(Class<?> pojoClass) {
                    PojoModel model = new PojoParser().parse(pojoClass, new AttributesStrategy(true, true, "_", null))
                            .filter(new DefaultFilter(true, false, false, false, true)).getModel();
                    return model;
                }
            };
        return attributesModelFactory;
    }

    // public static synchronized PojoModelFactory getAttributeModelFactory() {
    // if (defaultModelFactory == null)
    // defaultModelFactory =
    // new PojoModelFactory() {
    //
    // @Override
    // public PojoModel createPojoModel(Class<?> pojoClass) {
    // PojoModel model =
    // new PojoParser()
    // .parse(
    // pojoClass,
    // new AttributesStrategy(true, true, "_",
    // null))
    // .filter(
    // new DefaultFilter(
    // true, false, false, false, true))
    // .getModel();
    // return model;
    // }
    // };
    // return defaultModelFactory;
    // }

    public static ITreeNode pojoToNode(Object from) throws IOException {
        return pojoToNode(from, getDefaultModelFactory(), false, false);
    }

    public static ITreeNode pojoToNode(Object from, boolean verbose, boolean useAnnotations) throws IOException {
        return pojoToNode(from, getDefaultModelFactory(), verbose, useAnnotations);
    }

    public static ITreeNode pojoToNode(Object from, PojoModelFactory factory, boolean verbose, boolean useAnnotations)
            throws IOException {
        TreeNode to = new TreeNode();
        pojoToNode(from, to, factory, verbose, useAnnotations, "", 0);
        return to;
    }

    public static void pojoToNode(Object from, ITreeNode to) throws IOException {
        pojoToNode(from, to, getDefaultModelFactory(), false, false, "", 0);
    }

    public static void pojoToNode(Object from, ITreeNode to, PojoModelFactory factory) throws IOException {
        pojoToNode(from, to, factory, false, false, "", 0);
    }

    public static void pojoToNode(Object from, ITreeNode to, boolean verbose, boolean useAnnotations)
            throws IOException {
        pojoToNode(from, to, getDefaultModelFactory(), verbose, useAnnotations, "", 0);
    }

    private static void pojoToNode(Object from, ITreeNode to, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, String prefix, int level) throws IOException {
        if (level > MAX_LEVEL)
            return;
        if (from == null) {
            to.setBoolean(ITreeNode.NULL, true);
            return;
        }
        if (verbose)
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());

        if (from instanceof String) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setString(ITreeNode.NAMELESS_VALUE, (String) from);
        } else if (from instanceof Long) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setLong(ITreeNode.NAMELESS_VALUE, (Long) from);
        } else if (from instanceof Integer) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setInt(ITreeNode.NAMELESS_VALUE, (Integer) from);
        } else if (from instanceof Boolean) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setBoolean(ITreeNode.NAMELESS_VALUE, (Boolean) from);
        } else if (from instanceof Float) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setFloat(ITreeNode.NAMELESS_VALUE, (Float) from);
        } else if (from instanceof Short) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setInt(ITreeNode.NAMELESS_VALUE, (Short) from);
        } else if (from instanceof Byte) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setLong(ITreeNode.NAMELESS_VALUE, (Byte) from);
        } else if (from instanceof java.sql.Timestamp) {
            to.setString(ITreeNode.CLASS, "java.sql.Timestamp");
            to.setLong(ITreeNode.NAMELESS_VALUE, ((java.sql.Timestamp) from).getTime());
        } else if (from instanceof Date) {
            to.setString(ITreeNode.CLASS, "java.util.Date");
            to.setLong(ITreeNode.NAMELESS_VALUE, ((Date) from).getTime());
        } else if (from instanceof Enum) {
            to.setString(ITreeNode.CLASS, "java.lang.Enum");
            to.setString(ITreeNode.CLASS + "_", from.getClass().getCanonicalName());
            to.setInt(ITreeNode.NAMELESS_VALUE, ((Enum<?>) from).ordinal());
            to.setString(ITreeNode.HELPER_VALUE, ((Enum<?>) from).toString());
        } else if (from instanceof Character) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setInt(ITreeNode.NAMELESS_VALUE, (Character) from);
        } else if (from instanceof Double) {
            to.setString(ITreeNode.CLASS, from.getClass().getCanonicalName());
            to.setDouble(ITreeNode.NAMELESS_VALUE, (Double) from);
        } else if (from instanceof UUID) {
            to.setString(ITreeNode.CLASS, "java.util.UUID");
            to.setString(ITreeNode.NAMELESS_VALUE, ((UUID) from).toString());
        } else {
            PojoModel model = factory.createPojoModel(from.getClass());
            for (PojoAttribute<?> attr : model) {
                boolean deep = false;
                if (!attr.canRead())
                    continue;
                if (attr.getName().equals("class"))
                    continue;
                if (useAnnotations) {
                    Hidden hidden = attr.getAnnotation(Hidden.class);
                    if (hidden != null)
                        continue;
                    Public pub = attr.getAnnotation(Public.class);
                    if (pub != null) {
                        if (!pub.readable())
                            continue;
                        if (MCollection.contains(pub.hints(), MPojo.DEEP))
                            deep = true;
                    }
                    Embedded emb = attr.getAnnotation(Embedded.class); // experimental
                    if (emb != null) {
                        Object value = attr.get(from);
                        String name = attr.getName();
                        pojoToNode(value, to, factory, verbose, useAnnotations, prefix + name + "_", level + 1);
                        continue;
                    }
                }
                Object value = attr.get(from);
                String name = attr.getName();
                setNodeValue(to, prefix + name, value, factory, verbose, useAnnotations, deep, prefix, level + 1);
            }
        }
    }

    public static void setNodeValue(ITreeNode to, String name, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep) throws IOException {
        setNodeValue(to, name, value, factory, verbose, useAnnotations, deep, "", 0);
    }

    @SuppressWarnings("unchecked")
    private static void setNodeValue(ITreeNode to, String name, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep, String prefix, int level) throws IOException {
        if (level > MAX_LEVEL)
            return;
        try {
            if (verbose) {
                if (value != null)
                    to.setString(ITreeNode.CLASS + "_" + prefix + name, value.getClass().getCanonicalName());
                if (value == null) {
                    to.setBoolean(ITreeNode.NULL + "_" + prefix + name, true);
                }
            }
            if (value instanceof Boolean)
                to.setBoolean(prefix + name, (boolean) value);
            else if (value instanceof Integer)
                to.setInt(prefix + name, (int) value);
            else if (value instanceof String)
                to.setString(prefix + name, (String) value);
            else if (value instanceof Long)
                to.setLong(prefix + name, (long) value);
            // else if (value instanceof byte[]) to.put(prefix + name, (byte[]) value);
            else if (value instanceof Float)
                to.setFloat(prefix + name, (float) value);
            else if (value instanceof Double)
                to.setDouble(prefix + name, (double) value);
            else if (value instanceof Short)
                to.setInt(prefix + name, (short) value);
            else if (value instanceof Date)
                to.setLong(prefix + name, ((Date) value).getTime());
            else if (value instanceof Character)
                to.put(prefix + name, Character.toString((Character) value));
            else if (value instanceof Date) {
                to.put("_" + prefix + name, MDate.toIso8601((Date) value));
                to.put(prefix + name, ((Date) value).getTime());
            } else if (value instanceof BigDecimal)
                to.put(prefix + name, (BigDecimal) value);
            else if (value instanceof ITreeNode)
                to.addObject(prefix + name, (ITreeNode) value);
            else if (value.getClass().isEnum()) {
                to.put(prefix + name, ((Enum<?>) value).ordinal());
                to.put("_" + prefix + name, ((Enum<?>) value).name());
            } else if (value instanceof Map) {
                ITreeNode obj = to.createObject(prefix + name);
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                    setNodeValue(obj, String.valueOf(entry.getKey()), entry.getValue(), factory, verbose,
                            useAnnotations, true, prefix, level + 1);
                }
            } else if (value.getClass().isArray()) {
                TreeNodeList array = to.createArray(prefix + name);
                for (Object o : (Object[]) value) {
                    addNodeValue(array, o, factory, verbose, useAnnotations, true, prefix, level + 1);
                }
            } else if (value instanceof Collection) {
                TreeNodeList array = to.createArray(prefix + name);
                for (Object o : ((Collection<Object>) value)) {
                    addNodeValue(array, o, factory, verbose, useAnnotations, true, prefix, level + 1);
                }
            } else {
                if (deep) {
                    ITreeNode too = to.createObject(prefix + name);
                    pojoToNode(value, too, factory, verbose, useAnnotations, prefix, level + 1);
                } else {
                    to.put(prefix + name, String.valueOf(value));
                }
            }
        } catch (Throwable t) {
            LOGGER.trace("Failed to set Node value", t);
        }
    }

    public static void addNodeValue(TreeNodeList to, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep) throws IOException {
        addNodeValue(to, value, factory, verbose, useAnnotations, deep, "", 0);
    }

    @SuppressWarnings("unchecked")
    private static void addNodeValue(TreeNodeList to, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep, String prefix, int level) throws IOException {
        if (level > MAX_LEVEL)
            return;
        try {
            ITreeNode oo = to.createObject();
            if (verbose) {
                if (value != null)
                    oo.setString(ITreeNode.CLASS, value.getClass().getCanonicalName());
                if (value == null) {
                    oo.setBoolean(ITreeNode.NULL, true);
                }
            } else if (value instanceof Boolean)
                oo.setBoolean(ITreeNode.NAMELESS_VALUE, (boolean) value);
            else if (value instanceof Integer)
                oo.setInt(ITreeNode.NAMELESS_VALUE, (int) value);
            else if (value instanceof String)
                oo.setString(ITreeNode.NAMELESS_VALUE, (String) value);
            else if (value instanceof Long)
                oo.setLong(ITreeNode.NAMELESS_VALUE, (Long) value);
            else if (value instanceof Date) {
                oo.setLong(ITreeNode.NAMELESS_VALUE, ((Date) value).getTime());
                oo.setString(ITreeNode.HELPER_VALUE, MDate.toIso8601((Date) value));
            }
            // else if (value instanceof byte[]) oo.set(INode.NAMELESS_VALUE, (byte[])
            // value);
            else if (value instanceof Float)
                oo.setFloat(ITreeNode.NAMELESS_VALUE, (Float) value);
            // else if (value instanceof BigDecimal) oo.set(INode.NAMELESS_VALUE,
            // (BigDecimal) value);
            else if (value instanceof ITreeNode)
                oo.addObject(ITreeNode.NAMELESS_VALUE, (ITreeNode) value);
            else if (value.getClass().isEnum()) {
                oo.setInt(ITreeNode.HELPER_VALUE, ((Enum<?>) value).ordinal());
                oo.setString(ITreeNode.NAMELESS_VALUE, ((Enum<?>) value).name());
            } else if (value instanceof Map) {
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                    setNodeValue(oo, String.valueOf(entry.getKey()), entry.getValue(), factory, verbose, useAnnotations,
                            true, prefix, level + 1);
                }
            } else if (value instanceof Collection) {
                TreeNodeList array = oo.createArray(ITreeNode.NAMELESS_VALUE);
                for (Object o : ((Collection<Object>) value)) {
                    addNodeValue(array, o, factory, verbose, useAnnotations, true, prefix, level + 1);
                }
            } else {
                if (deep) {
                    pojoToNode(value, oo, factory, verbose, useAnnotations, prefix, level + 1);
                } else {
                    oo.setString(ITreeNode.NAMELESS_VALUE, String.valueOf(value));
                }
            }
        } catch (Throwable t) {
            LOGGER.trace("Failed to add node value", t);
        }
    }

    public static void nodeToPojo(ITreeNode from, Object to) throws IOException {
        nodeToPojo(from, to, getDefaultModelFactory(), false, false);
    }

    public static void nodeToPojo(ITreeNode from, Object to, boolean force) throws IOException {
        nodeToPojo(from, to, getDefaultModelFactory(), force, false);
    }

    public static void nodeToPojo(ITreeNode from, Object to, boolean force, boolean verbose) throws IOException {
        nodeToPojo(from, to, getDefaultModelFactory(), force, verbose);
    }

    public static void nodeToPojo(ITreeNode from, Object to, PojoModelFactory factory) throws IOException {
        nodeToPojo(from, to, factory, false, false);
    }

    public static Object nodeToPojoObject(ITreeNode from) throws Exception {
        return nodeToPojoObject(from, getDefaultModelFactory(), null, false, false);
    }

    public static Object nodeToPojoObject(ITreeNode from, boolean force) throws Exception {
        return nodeToPojoObject(from, getDefaultModelFactory(), null, force, false);
    }

    public static Object nodeToPojoObject(ITreeNode from, boolean force, boolean verbose) throws Exception {
        return nodeToPojoObject(from, getDefaultModelFactory(), null, force, verbose);
    }

    public static Object nodeToPojoObject(ITreeNode from, PojoModelFactory factory) throws Exception {
        return nodeToPojoObject(from, factory, null, false, false);
    }

    public static Object nodeToPojoObject(ITreeNode from, PojoModelFactory factory, ClassLoader activator,
            boolean force, boolean verbose) throws Exception {

        if (activator == null)
            activator = getDefaultClassLoader();
        if (from.getBoolean(ITreeNode.NULL, false))
            return null;
        String clazz = from.getString(ITreeNode.CLASS, null);
        if (clazz == null)
            return null;
        switch (clazz) {
        case "java.lang.Boolean":
            return from.getBoolean(ITreeNode.NAMELESS_VALUE, false);
        case "java.lang.Integer":
            return from.getInt(ITreeNode.NAMELESS_VALUE, 0);
        case "java.lang.Long":
            return from.getLong(ITreeNode.NAMELESS_VALUE, 0);
        case "java.lang.Short":
            return (short) from.getInt(ITreeNode.NAMELESS_VALUE, 0);
        case "java.lang.Double":
            return from.getDouble(ITreeNode.NAMELESS_VALUE, 0);
        case "java.lang.Float":
            return from.getFloat(ITreeNode.NAMELESS_VALUE, 0);
        case "java.lang.Byte":
            return (byte) from.getInt(ITreeNode.NAMELESS_VALUE, 0);
        case "java.lang.String":
            return from.getString(ITreeNode.NAMELESS_VALUE);
        case "java.util.Date":
            return new Date(from.getLong(ITreeNode.NAMELESS_VALUE, 0));
        case "java.lang.Character":
            return (char) from.getInt(ITreeNode.NAMELESS_VALUE, 0);
        case "java.util.UUID":
            return UUID.fromString(from.getString(ITreeNode.NAMELESS_VALUE).get());
        case "java.sql.Timestamp":
            return new java.sql.Timestamp(from.getLong(ITreeNode.NAMELESS_VALUE, 0));
        case "java.lang.Enum":
            String enuName = from.getString(ITreeNode.CLASS + "_").get();
            Class<? extends Enum<?>> type = MSystem.getEnum(enuName, activator);
            if (type != null) {
                Object[] cons = type.getEnumConstants();
                int ord = from.getInt(ITreeNode.NAMELESS_VALUE, 0);
                if (ord >= 0 && ord < cons.length)
                    return cons[ord];
            }
            return null;
        }
        Object obj = MObject.newInstance(activator, clazz);
        nodeToPojo(from, obj, factory, force, verbose);
        return obj;
    }

    private static ClassLoader getDefaultClassLoader() {
        return defaultClassLoader == null ? MService.getService(ClassLoaderProvider.class).getClassLoader()
                : defaultClassLoader;
    }

    /**
     * @param from
     * @param to
     * @param factory
     * @param force
     * @param verbose
     *            Use verbose hints from serialization to create the correct object
     *
     * @throws IOException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void nodeToPojo(ITreeNode from, Object to, PojoModelFactory factory, boolean force, boolean verbose)
            throws IOException {
        PojoModel model = factory.createPojoModel(to.getClass());
        for (PojoAttribute<Object> attr : model) {

            if (!force && !attr.canWrite())
                continue;

            String name = attr.getName();
            Class<?> type = attr.getType();

            try {
                if (!from.containsKey(name)) {

                } else if (type == Boolean.class || type == boolean.class)
                    attr.set(to, from.getBoolean(name, false), force);
                else if (type == Integer.class || type == int.class)
                    attr.set(to, from.getInt(name, 0), force);
                else if (type == Long.class || type == long.class)
                    attr.set(to, from.getLong(name, 0), force);
                else if (type == Double.class || type == double.class)
                    attr.set(to, from.getDouble(name, 0), force);
                else if (type == Float.class || type == float.class)
                    attr.set(to, from.getFloat(name, 0), force);
                else if (type == Byte.class || type == byte.class)
                    attr.set(to, (byte) from.getInt(name, 0), force);
                else if (type == Short.class || type == short.class)
                    attr.set(to, (short) from.getInt(name, 0), force);
                else if (type == Character.class || type == char.class)
                    attr.set(to, (char) from.getInt(name, 0), force);
                else if (type == Date.class)
                    attr.set(to, new Date(from.getLong(name, 0)), force);
                else if (type == String.class)
                    attr.set(to, from.getString(name, null), force);
                else if (type == UUID.class)
                    try {
                        attr.set(to, UUID.fromString(from.getString(name, null)), force);
                    } catch (IllegalArgumentException e) {
                        attr.set(to, null, force);
                    }
                else if (type.isEnum()) {
                    Object[] cons = type.getEnumConstants();
                    int ord = from.getInt(name, 0);
                    Object c = cons.length > 0 ? cons[0] : null;
                    if (ord >= 0 && ord < cons.length)
                        c = cons[ord];
                    attr.set(to, c, force);
                } else if (Map.class.isAssignableFrom(type)) {
                    ITreeNode obj = from.getObject(name).orElse(null);
                    if (obj != null) {
                        Map inst = null;
                        if (type == Map.class)
                            inst = new HashMap<>();
                        else
                            inst = (Map) type.getConstructor().newInstance();
                        if (verbose)
                            for (Entry<String, Object> entry : obj.entrySet()) {
                                if (!entry.getKey().startsWith("_")) {
                                    String hint = obj.getString(ITreeNode.CLASS + "_" + entry.getKey(), null);
                                    addNodeValue(inst, entry.getKey(), entry.getValue(), hint, factory, force, verbose);
                                }
                            }
                        else
                            inst.putAll(obj);
                        attr.set(to, inst, force);
                    }
                } else if (Collection.class.isAssignableFrom(type)) {
                    TreeNodeList array = from.getArray(name).orElse(null);
                    if (array != null) {
                        Collection inst = null;
                        if (type == Collection.class || type == List.class)
                            inst = new ArrayList<>();
                        else
                            inst = (Collection) type.getConstructor().newInstance();
                        for (ITreeNode obj : array) {
                            if (verbose) {
                                String hint = obj.getString(ITreeNode.CLASS, null);
                                Object val = toNodeValue(model, hint, factory, force, verbose);
                                if (val != null)
                                    inst.add(val);
                            } else if (obj.containsKey(ITreeNode.NAMELESS_VALUE))
                                inst.add(obj.get(ITreeNode.NAMELESS_VALUE));
                            else {
                                // oo = // not possible, cant
                                // generate a complex object from no type
                            }
                        }
                        attr.set(to, inst, force);
                    }
                } else
                    attr.set(to, from.getString(name, null), force);
            } catch (Throwable t) {
                LOGGER.debug("Error: {} {}", MSystem.getClassName(to), name, t);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void addNodeValue(Map inst, String name, Object value, String hint, PojoModelFactory factory,
            boolean force, boolean verbose) {
        Object val = toNodeValue(value, hint, factory, force, verbose);
        if (val != null)
            inst.put(name, val);
    }

    public static Object toNodeValue(Object value, String hint, PojoModelFactory factory, boolean force,
            boolean verbose) {
        Object val = value;
        if (MString.isEmptyTrim(hint)) {
            // nothing
        } else {
            val = MCast.to(value, hint);
            if (val == null && value instanceof ITreeNode) {
                try {
                    val = MObject.newInstance(getDefaultClassLoader(), hint);
                    nodeToPojo((ITreeNode) value, val, factory, force, verbose);
                } catch (Throwable t) {
                    LOGGER.debug("Can't create object {}", hint, t);
                }
            } else if (val == null) {
                try {
                    // try enum
                    Class<?> type = MSystem.getEnum(hint, null);
                    if (type != null) {
                        Object[] cons = type.getEnumConstants();
                        int ord = MCast.toint(value, 0);
                        val = cons.length > 0 ? cons[0] : null;
                        if (ord >= 0 && ord < cons.length)
                            val = cons[ord];
                    }
                } catch (Throwable t) {
                    LOGGER.debug("Error: {}", hint, t);
                }
                if (val == null)
                    val = value; // fallback
            }
        }
        return val;
    }

    public static void pojoToJson(Object from, ObjectNode to) throws IOException {
        pojoToJson(from, to, getDefaultModelFactory());
    }

    public static void pojoToJson(Object from, ObjectNode to, PojoModelFactory factory) throws IOException {
        pojoToJson(from, to, factory, false, false, "", 0);
    }

    public static void pojoToJson(Object from, ObjectNode to, PojoModelFactory factory, boolean useAnnotations)
            throws IOException {
        pojoToJson(from, to, factory, false, useAnnotations, "", 0);
    }

    public static void pojoToJson(Object from, ObjectNode to, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations) throws IOException {
        pojoToJson(from, to, factory, verbose, useAnnotations, "", 0);
    }

    private static void pojoToJson(Object from, ObjectNode to, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, String prefix, int level) throws IOException {
        if (level > MAX_LEVEL)
            return;
        PojoModel model = factory.createPojoModel(from.getClass());
        for (PojoAttribute<?> attr : model) {
            boolean deep = false;
            if (!attr.canRead())
                continue;
            if (useAnnotations) {
                Hidden hidden = attr.getAnnotation(Hidden.class);
                if (hidden != null)
                    continue;
                Public pub = attr.getAnnotation(Public.class);
                if (pub != null) {
                    if (!pub.readable())
                        continue;
                    if (MCollection.contains(pub.hints(), MPojo.DEEP))
                        deep = true;
                }
                Embedded emb = attr.getAnnotation(Embedded.class);
                if (emb != null) {
                    Object value = attr.get(from);
                    String name = attr.getName();
                    pojoToJson(value, to, factory, verbose, useAnnotations, prefix + name + "_", level + 1);
                    continue;
                }
            }
            Object value = attr.get(from);
            String name = attr.getName();
            setJsonValue(to, prefix + name, value, factory, verbose, useAnnotations, deep, prefix, level + 1);
        }
    }

    public static void addJsonValue(ArrayNode to, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep) throws IOException {
        addJsonValue(to, value, factory, verbose, useAnnotations, deep, "", 0);
    }

    @SuppressWarnings("unchecked")
    private static void addJsonValue(ArrayNode to, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep, String prefix, int level) throws IOException {
        if (level > MAX_LEVEL)
            return;
        try {
            if (value == null)
                to.addNull();
            else if (value instanceof Boolean)
                to.add((boolean) value);
            else if (value instanceof Integer)
                to.add((int) value);
            else if (value instanceof String)
                to.add((String) value);
            else if (value instanceof Long)
                to.add((Long) value);
            else if (value instanceof byte[])
                to.add((byte[]) value);
            else if (value instanceof Float)
                to.add((Float) value);
            else if (value instanceof BigDecimal)
                to.add((BigDecimal) value);
            else if (value instanceof JsonNode)
                to.add((JsonNode) value);
            else if (value.getClass().isEnum()) {
                to.add(((Enum<?>) value).ordinal());
                // to.put(name + "_", ((Enum<?>)value).name());
            } else if (value instanceof Map) {
                ObjectNode obj = to.objectNode();
                to.add(obj);
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                    setJsonValue(obj, String.valueOf(entry.getKey()), entry.getValue(), factory, verbose,
                            useAnnotations, true, prefix, level + 1);
                }
            } else if (value instanceof Collection) {
                ArrayNode array = to.arrayNode();
                to.add(array);
                for (Object o : ((Collection<Object>) value)) {
                    addJsonValue(array, o, factory, verbose, useAnnotations, true, prefix, level + 1);
                }
            } else {
                if (deep) {
                    ObjectNode too = to.objectNode();
                    to.add(too);
                    pojoToJson(value, too, factory, verbose, useAnnotations, prefix, level + 1);
                } else {
                    to.add(String.valueOf(value));
                }
            }
        } catch (Throwable t) {
            LOGGER.trace("Error", t);
        }
    }

    public static void setJsonValue(ObjectNode to, String name, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep) throws IOException {
        setJsonValue(to, name, value, factory, verbose, useAnnotations, deep, "", 0);
    }

    @SuppressWarnings("unchecked")
    public static void setJsonValue(ObjectNode to, String name, Object value, PojoModelFactory factory, boolean verbose,
            boolean useAnnotations, boolean deep, String prefix, int level) throws IOException {
        if (level > MAX_LEVEL)
            return;
        try {
            if (verbose) {
                if (value != null)
                    to.put(ITreeNode.CLASS, value.getClass().getCanonicalName());
            }
            if (value == null)
                to.putNull(prefix + name);
            else if (value instanceof Boolean)
                to.put(prefix + name, (boolean) value);
            else if (value instanceof Integer)
                to.put(prefix + name, (int) value);
            else if (value instanceof String)
                to.put(prefix + name, (String) value);
            else if (value instanceof Long)
                to.put(prefix + name, (long) value);
            else if (value instanceof byte[])
                to.put(prefix + name, (byte[]) value);
            else if (value instanceof Float)
                to.put(prefix + name, (float) value);
            else if (value instanceof Double)
                to.put(prefix + name, (double) value);
            else if (value instanceof Short)
                to.put(prefix + name, (short) value);
            else if (value instanceof Character)
                to.put(prefix + name, Character.toString((Character) value));
            else if (value instanceof Date) {
                to.put(prefix + name, ((Date) value).getTime());
                to.put("_" + prefix + name, MDate.toIso8601((Date) value));
            } else if (value instanceof BigDecimal)
                to.put(prefix + name, (BigDecimal) value);
            else if (value instanceof JsonNode)
                to.set(prefix + name, (JsonNode) value);
            else if (value.getClass().isEnum()) {
                to.put(prefix + name, ((Enum<?>) value).ordinal());
                to.put("_" + prefix + name, ((Enum<?>) value).name());
            } else if (value instanceof Map) {
                ObjectNode obj = to.objectNode();
                to.set(prefix + name, obj);
                for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                    setJsonValue(obj, String.valueOf(entry.getKey()), entry.getValue(), factory, verbose,
                            useAnnotations, true, prefix, level + 1);
                }
            } else if (value.getClass().isArray()) {
                ArrayNode array = to.arrayNode();
                to.set(prefix + name, array);
                for (Object o : (Object[]) value) {
                    addJsonValue(array, o, factory, verbose, useAnnotations, true, prefix, level + 1);
                }
            } else if (value instanceof Collection) {
                ArrayNode array = to.arrayNode();
                to.set(prefix + name, array);
                for (Object o : ((Collection<Object>) value)) {
                    addJsonValue(array, o, factory, verbose, useAnnotations, true, prefix, level + 1);
                }
            } else {
                if (deep) {
                    ObjectNode too = to.objectNode();
                    to.set(prefix + name, too);
                    pojoToJson(value, too, factory, verbose, useAnnotations, prefix, level + 1);
                } else {
                    to.put(prefix + name, String.valueOf(value));
                }
            }
        } catch (Throwable t) {
            LOGGER.trace("Error", t);
        }
    }

    public static void jsonToPojo(JsonNode from, Object to) throws IOException {
        jsonToPojo(from, to, getDefaultModelFactory(), false);
    }

    public static void jsonToPojo(JsonNode from, Object to, boolean force) throws IOException {
        jsonToPojo(from, to, getDefaultModelFactory(), force);
    }

    public static void jsonToPojo(JsonNode from, Object to, PojoModelFactory factory) throws IOException {
        jsonToPojo(from, to, factory, false);
    }

    @SuppressWarnings("unchecked")
    public static void jsonToPojo(JsonNode from, Object to, PojoModelFactory factory, boolean force)
            throws IOException {
        PojoModel model = factory.createPojoModel(to.getClass());
        for (PojoAttribute<Object> attr : model) {

            if (!attr.canWrite())
                continue;

            String name = attr.getName();
            Class<?> type = attr.getType();
            JsonNode json = from.get(name);

            try {
                if (json == null || !attr.canWrite()) {

                } else if (type == Boolean.class || type == boolean.class)
                    attr.set(to, json.asBoolean(false), force);
                else if (type == Integer.class || type == int.class)
                    attr.set(to, json.asInt(0), force);
                else if (type == Long.class || type == long.class)
                    attr.set(to, json.asLong(0), force);
                else if (type == Double.class || type == double.class)
                    attr.set(to, json.asDouble(0), force);
                else if (type == Float.class || type == float.class)
                    attr.set(to, (float) json.asDouble(0), force);
                else if (type == Byte.class || type == byte.class)
                    attr.set(to, (byte) json.asInt(0), force);
                else if (type == Short.class || type == short.class)
                    attr.set(to, (short) json.asInt(0), force);
                else if (type == Character.class || type == char.class)
                    attr.set(to, (char) json.asInt(0), force);
                else if (type == String.class)
                    attr.set(to, json.asText(), force);
                else if (type == UUID.class)
                    try {
                        attr.set(to, UUID.fromString(json.asText()), force);
                    } catch (IllegalArgumentException e) {
                        attr.set(to, null, force);
                    }
                else if (type.isEnum()) {
                    Object[] cons = type.getEnumConstants();
                    int ord = json.asInt(0);
                    Object c = cons.length > 0 ? cons[0] : null;
                    if (ord >= 0 && ord < cons.length)
                        c = cons[ord];
                    attr.set(to, c, force);
                } else
                    attr.set(to, json.asText(), force);
            } catch (Throwable t) {
                LOGGER.debug("Error: {} {}", MSystem.getClassName(to), name, t);
            }
        }
    }

    public static void pojoToXml(Object from, Element to) throws IOException {
        pojoToXml(from, to, getDefaultModelFactory());
    }

    public static void pojoToXml(Object from, Element to, PojoModelFactory factory) throws IOException {
        pojoToXml(from, to, factory, 0);
    }

    public static void pojoToXml(Object from, Element to, PojoModelFactory factory, int level) throws IOException {
        if (level > MAX_LEVEL)
            return;
        PojoModel model = factory.createPojoModel(from.getClass());
        for (PojoAttribute<?> attr : model) {

            try {
                if (!attr.canRead())
                    continue;

                Object value = attr.get(from);
                String name = attr.getName();

                Element a = to.getOwnerDocument().createElement("attribute");
                to.appendChild(a);
                a.setAttribute("name", name);

                if (value == null) {
                    a.setAttribute("null", "true");
                    // to.setAttribute(name, (String)null);
                } else if (value instanceof Boolean)
                    a.setAttribute("boolean", MCast.toString((boolean) value));
                else if (value instanceof Integer)
                    a.setAttribute("int", MCast.toString((int) value));
                else if (value instanceof Long)
                    a.setAttribute("long", MCast.toString((long) value));
                else if (value instanceof Date)
                    a.setAttribute("date", MCast.toString(((Date) value).getTime()));
                else if (value instanceof String) {
                    if (hasValidChars((String) value))
                        a.setAttribute("string", (String) value);
                    else {
                        a.setAttribute("encoding", "base64");
                        a.setAttribute("string", Base64.encode((String) value));
                    }
                } else if (value.getClass().isEnum()) {
                    a.setAttribute("enum", MCast.toString(((Enum<?>) value).ordinal()));
                    a.setAttribute("value", ((Enum<?>) value).name());
                } else if (value instanceof UUID) {
                    a.setAttribute("uuid", ((UUID) value).toString());
                } else if (value instanceof Serializable) {
                    a.setAttribute("serializable", "true");

                    CDATASection cdata = a.getOwnerDocument().createCDATASection("");
                    String data = MCast.toBinaryString(MCast.toBinary(value));
                    cdata.setData(data);
                    a.appendChild(cdata);
                } else {
                    a.setAttribute("type", value.getClass().getCanonicalName());
                    pojoToXml(value, a, factory, level + 1);
                }

            } catch (Throwable t) {
                LOGGER.debug("Error: {} {}", MSystem.getClassName(from), attr.getName(), t);
            }
        }
    }

    private static boolean hasValidChars(String value) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t' || c >= 32 && c <= 55295) {
            } else {
                return false;
            }
        }
        return true;
    }

    public static void xmlToPojo(Element from, Object to, ClassLoader act) throws IOException {
        xmlToPojo(from, to, getDefaultModelFactory(), act, false);
    }

    public static void xmlToPojo(Element from, Object to, ClassLoader act, boolean force) throws IOException {
        xmlToPojo(from, to, getDefaultModelFactory(), act, force);
    }

    public static void xmlToPojo(Element from, Object to, PojoModelFactory factory, ClassLoader act)
            throws IOException {
        xmlToPojo(from, to, factory, act, false);
    }

    @SuppressWarnings("unchecked")
    public static void xmlToPojo(Element from, Object to, PojoModelFactory factory, ClassLoader act, boolean force)
            throws IOException {
        PojoModel model = factory.createPojoModel(to.getClass());

        HashMap<String, Element> index = new HashMap<>();
        for (Element e : MXml.getLocalElementIterator(from, "attribute"))
            index.put(e.getAttribute("name"), e);

        for (PojoAttribute<Object> attr : model) {

            try {
                if (!attr.canWrite())
                    continue;

                String name = attr.getName();
                // Class<?> type = attr.getType();
                Element a = index.get(name);
                if (a == null) {
                    LOGGER.debug("attribute not found {}", name, to.getClass());
                    continue;
                }
                {
                    String value = a.getAttribute("null");
                    if (MString.isSet(value) && value.equals("true")) {
                        attr.set(to, null, force);
                        continue;
                    }
                }
                if (a.hasAttribute("string")) {
                    String data = a.getAttribute("encoding");
                    if ("base64".equals(data)) {
                        String value = new String(Base64.decode(a.getAttribute("string")));
                        attr.set(to, value, force);
                    } else {
                        String value = a.getAttribute("string");
                        attr.set(to, value, force);
                    }
                    continue;
                }
                if (a.hasAttribute("boolean")) {
                    String value = a.getAttribute("boolean");
                    attr.set(to, MCast.toboolean(value, false), force);
                    continue;
                }
                if (a.hasAttribute("int")) {
                    String value = a.getAttribute("int");
                    attr.set(to, MCast.toint(value, 0), force);
                    continue;
                }
                if (a.hasAttribute("long")) {
                    String value = a.getAttribute("long");
                    attr.set(to, MCast.tolong(value, 0), force);
                    continue;
                }
                if (a.hasAttribute("date")) {
                    String value = a.getAttribute("date");
                    Date obj = new Date();
                    obj.setTime(MCast.tolong(value, 0));
                    attr.set(to, obj, force);
                    continue;
                }
                if (a.hasAttribute("uuid")) {
                    String value = a.getAttribute("uuid");
                    try {
                        attr.set(to, UUID.fromString(value), force);
                    } catch (Throwable t) {
                        LOGGER.debug("Error: {}", name, t);
                    }
                    continue;
                }
                if (a.hasAttribute("enum")) {
                    String value = a.getAttribute("enum");
                    attr.set(to, MCast.toint(value, 0), force);
                    continue;
                }
                if ("true".equals(a.getAttribute("serializable"))) {
                    CDATASection cdata = MXml.findCDataSection(a);
                    if (cdata != null) {
                        String data = cdata.getData();
                        try {
                            Object obj = MCast.fromBinary(MCast.fromBinaryString(data));
                            attr.set(to, obj, force);
                        } catch (ClassNotFoundException e1) {
                            throw new IOException(e1);
                        }
                    }
                }
                if (a.hasAttribute("type")) {
                    String value = a.getAttribute("type");
                    try {
                        Object obj = MObject.newInstance(act, value);
                        xmlToPojo(a, obj, factory, act);
                        attr.set(to, obj, force);
                    } catch (Exception e1) {
                        LOGGER.debug("Error: {} {}", name, to.getClass(), e1);
                    }
                    continue;
                }

            } catch (Throwable t) {
                LOGGER.debug("Error: {} {}", MSystem.getClassName(to), attr.getName(), t);
            }
        }
    }

    /**
     * Functionize a String. Remove bad names and set first characters to upper. Return def if the name can't be
     * created, e.g. only numbers.
     *
     * @param in
     * @param firstUpper
     * @param def
     *
     * @return The function name
     */
    public static String toFunctionName(String in, boolean firstUpper, String def) {
        if (MString.isEmpty(in))
            return def;
        boolean first = firstUpper;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_') {
                if (first)
                    c = Character.toUpperCase(c);
                first = false;
                out.append(c);
            } else if (!first && c >= '0' && c <= '9') {
                out.append(c);
            } else {
                first = true;
            }
        }

        if (out.length() == 0)
            return def;
        return out.toString();
    }

    public static IProperties pojoToProperties(Object from) throws IOException {
        return pojoToProperties(from, getDefaultModelFactory());
    }

    public static IProperties pojoToProperties(Object from, PojoModelFactory factory) throws IOException {
        MProperties out = new MProperties();
        PojoModel model = factory.createPojoModel(from.getClass());

        for (PojoAttribute<?> attr : model) {

            try {
                if (!attr.canRead())
                    continue;

                Object value = attr.get(from);

                String name = attr.getName();
                Class<?> type = attr.getType();
                if (type == int.class)
                    out.setInt(name, (int) value);
                else if (type == Integer.class)
                    out.setInt(name, (Integer) value);
                else if (type == long.class)
                    out.setLong(name, (long) value);
                else if (type == Long.class)
                    out.setLong(name, (Long) value);
                else if (type == float.class)
                    out.setFloat(name, (float) value);
                else if (type == Float.class)
                    out.setFloat(name, (Float) value);
                else if (type == double.class)
                    out.setDouble(name, (double) value);
                else if (type == Double.class)
                    out.setDouble(name, (Double) value);
                else if (type == boolean.class)
                    out.setBoolean(name, (boolean) value);
                else if (type == Boolean.class)
                    out.setBoolean(name, (Boolean) value);
                else if (type == String.class)
                    out.setString(name, (String) value);
                else if (type == Date.class)
                    out.setDate(name, (Date) value);
                else
                    out.setString(name, String.valueOf(value));

            } catch (Throwable t) {
                LOGGER.debug("Error: {} {}", MSystem.getClassName(from), attr.getName(), t);
            }
        }
        return out;
    }

    public static void propertiesToPojo(IProperties from, Object to) throws IOException {
        propertiesToPojo(from, to, getDefaultModelFactory(), null, false);
    }

    public static void propertiesToPojo(IProperties from, Object to, boolean force) throws IOException {
        propertiesToPojo(from, to, getDefaultModelFactory(), null, force);
    }

    public static void propertiesToPojo(IProperties from, Object to, PojoModelFactory factory) throws IOException {
        propertiesToPojo(from, to, factory, null, false);
    }

    @SuppressWarnings("unchecked")
    public static void propertiesToPojo(IProperties from, Object to, PojoModelFactory factory,
            Caster<Object, Object> unknownHadler, boolean force) throws IOException {
        PojoModel model = factory.createPojoModel(to.getClass());
        for (PojoAttribute<Object> attr : model) {

            if (!attr.canWrite())
                continue;

            String name = attr.getName();
            Class<?> type = attr.getType();
            try {
                if (!from.isProperty(name) || !attr.canWrite()) {

                } else if (type == Boolean.class || type == boolean.class)
                    attr.set(to, from.getBoolean(name, false), force);
                else if (type == Integer.class || type == int.class)
                    attr.set(to, from.getInt(name, 0), force);
                else if (type == String.class)
                    attr.set(to, from.getString(name, null), force);
                else if (type == UUID.class)
                    try {
                        attr.set(to, UUID.fromString(from.getString(name).get()), force);
                    } catch (IllegalArgumentException e) {
                        attr.set(to, null, force);
                    }
                else if (type.isEnum()) {
                    Object[] cons = type.getEnumConstants();
                    int ord = from.getInt(name, 0);
                    Object c = cons.length > 0 ? cons[0] : null;
                    if (ord >= 0 && ord < cons.length)
                        c = cons[ord];
                    attr.set(to, c, force);
                } else
                    attr.set(to,
                            unknownHadler == null ? from.getString(name) : unknownHadler.cast(from.get(name), null),
                            force);
            } catch (Throwable t) {
                LOGGER.debug("Error: {} {}", MSystem.getClassName(to), name, t);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void propertiesToPojo(Map<String, String> from, Object to, TransformHelper helper)
            throws IOException {
        PojoModel model = helper.createPojoModel(from);
        for (PojoAttribute<Object> attr : model) {
            String name = attr.getName();
            String value = from.get(name);
            if (value != null) {
                attr.set(to, value, helper.isForce());
            }
        }
    }

    public static void pojoToObjectStream(Object from, ObjectOutputStream to) throws IOException {
        pojoToObjectStream(from, to, getDefaultModelFactory());
    }

    public static void pojoToObjectStream(Object from, ObjectOutputStream to, PojoModelFactory factory)
            throws IOException {
        PojoModel model = factory.createPojoModel(from.getClass());
        for (PojoAttribute<?> attr : model) {
            String name = attr.getName();
            Object value = attr.get(from);
            to.writeObject(name);
            to.writeObject(value);
        }
        to.writeObject(" ");
    }

    public static void objectStreamToPojo(ObjectInputStream from, Object to)
            throws IOException, ClassNotFoundException {
        objectStreamToPojo(from, to, getDefaultModelFactory(), false);
    }

    public static void objectStreamToPojo(ObjectInputStream from, Object to, boolean force)
            throws IOException, ClassNotFoundException {
        objectStreamToPojo(from, to, getDefaultModelFactory(), force);
    }

    public static void objectStreamToPojo(ObjectInputStream from, Object to, PojoModelFactory factory)
            throws IOException, ClassNotFoundException {
        objectStreamToPojo(from, to, factory, false);
    }

    @SuppressWarnings("unchecked")
    public static void objectStreamToPojo(ObjectInputStream from, Object to, PojoModelFactory factory, boolean force)
            throws IOException, ClassNotFoundException {
        PojoModel model = factory.createPojoModel(to.getClass());
        while (true) {
            String name = (String) from.readObject();
            if (name.equals(" "))
                break;
            Object value = from.readObject();
            @SuppressWarnings("rawtypes")
            PojoAttribute attr = model.getAttribute(name);
            if (attr != null)
                attr.set(to, value, force);
        }
    }

    public static void base64ToObject(String content, Object obj, PojoModelFactory factory)
            throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(content));
        ObjectInputStream ois = new ObjectInputStream(bais);
        MPojo.objectStreamToPojo(ois, obj, factory);
    }

    public static String objectToBase64(Object obj, PojoModelFactory factory) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        MPojo.pojoToObjectStream(obj, oos, factory);

        return Base64.encode(baos.toByteArray());
    }
}
