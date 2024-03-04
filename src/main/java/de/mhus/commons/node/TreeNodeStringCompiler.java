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

import de.mhus.commons.errors.MException;
import de.mhus.commons.parser.StringCompiler;
import de.mhus.commons.parser.StringPart;
import de.mhus.commons.tools.MString;
import de.mhus.commons.lang.IValuesProvider;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TreeNodeStringCompiler extends StringCompiler {

    private TreeNode rootNode;

    TreeNodeStringCompiler(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    protected StringPart createDefaultAttributePart(String part) {
        if (part.startsWith(">root:")) return new RootAttributePart(part);
        //        if (part.startsWith(">js:")) return new DefaultScriptPart(part);
        return new NodeAttributePart(part);
    }

    private class RootAttributePart implements StringPart {
        private String name;
        private String def;
        private ITreeNode node;

        public RootAttributePart(String part) {
            name = MString.afterIndex(part, ':');
            node = rootNode;
            while (node.getParent() != null && node.getParent() != node) node = node.getParent();
            int pos = name.indexOf(',');
            if (pos > 0) {
                def = name.substring(pos + 1);
                name = name.substring(0, pos);
            }
        }

        @Override
        public void execute(StringBuilder out, IValuesProvider attributes) throws MException {
            out.append(node.getString(name, def));
        }

        @Override
        public void dump(int level, StringBuilder out) {
            MString.appendRepeating(level, ' ', out);
            out.append(getClass().getCanonicalName())
                    .append(name)
                    .append("(")
                    .append(def)
                    .append(")");
        }
    }

    private class NodeAttributePart implements StringPart {

        private String name;
        private String def;
        private TreeNode node;

        public NodeAttributePart(String part) {
            name = part;
            int pos = name.indexOf(',');
            if (pos > 0) {
                def = name.substring(pos + 1);
                name = name.substring(0, pos);
            }
            node = rootNode;
            if (name.startsWith("/")) {
                while (node.getParent() != null) node = (TreeNode) node.getParent();
                name = name.substring(1);
            } else
                while (name.startsWith("../")) {
                    node = (TreeNode) node.getParent();
                    name = name.substring(3);
                    if (node == null) break;
                }
        }

        @Override
        public void execute(StringBuilder out, IValuesProvider attributes) throws MException {
            int level = 0;
            if (attributes != null && attributes instanceof NodeMap) {
                level = ((NodeMap) attributes).getLevel();
            }
            if (node == null) out.append(def);
            else out.append(node.getExtracted(name, def, level));
        }

        @Override
        public void dump(int level, StringBuilder out) {
            MString.appendRepeating(level, ' ', out);
            out.append(getClass().getCanonicalName())
                    .append(name)
                    .append("(")
                    .append(def)
                    .append(")");
        }
    }

    static class NodeMap implements Map<String, Object> {

        private int level;
        private TreeNode node;

        NodeMap(int level) {
            this.level = level;
        }

        NodeMap(int level, TreeNode node) {
            this.level = level;
            this.node = node;
        }

        @Override
        public int size() {
            return node == null ? 0 : node.size();
        }

        public int getLevel() {
            return level;
        }

        @Override
        public boolean isEmpty() {
            return node == null ? true : node.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return node == null ? false : node.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return node == null ? false : node.containsValue(value);
        }

        @Override
        public Object get(Object key) {
            return node == null ? null : node.get(key);
        }

        @Override
        public Object put(String key, Object value) {
            return null;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map<? extends String, ? extends Object> m) {}

        @Override
        public void clear() {}

        @Override
        public Set<String> keySet() {
            return node == null ? null : node.keySet();
        }

        @Override
        public Collection<Object> values() {
            return null;
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return null;
        }
    }
}
