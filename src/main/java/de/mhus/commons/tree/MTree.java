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

import de.mhus.commons.errors.MException;
import de.mhus.commons.services.MService;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
