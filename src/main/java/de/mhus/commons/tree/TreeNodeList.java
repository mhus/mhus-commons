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

import java.util.Collection;
import java.util.LinkedList;

public class TreeNodeList extends LinkedList<ITreeNode> {

    private static final long serialVersionUID = 1L;
    private String name;
    private ITreeNode parent;

    public TreeNodeList(String name, TreeNode parent) {
        this.name = name;
        this.parent = parent;
    }

    @Override
    public boolean addAll(int index, Collection<? extends ITreeNode> c) {
        c.forEach(i -> {
            ((TreeNode) i).name = name;
            ((TreeNode) i).parent = parent;
        });
        return super.addAll(index, c);
    }

    @Override
    public boolean add(ITreeNode e) {
        ((TreeNode) e).name = name;
        ((TreeNode) e).parent = parent;
        return super.add(e);
    }

    public boolean add(IProperties e) {
        TreeNode node = new TreeNode();
        node.parent = parent;
        node.putAll(e);
        return super.add(node);
    }

    public ITreeNode add(TreeNodeSerializable object) {
        ITreeNode cfg = createObject();
        try {
            object.writeSerializabledNode(cfg);
        } catch (Exception e) {
            throw new MRuntimeException(RC.STATUS.ERROR, e);
        }
        return cfg;
    }

    @Override
    public void addFirst(ITreeNode e) {
        ((TreeNode) e).name = name;
        ((TreeNode) e).parent = parent;
        super.addFirst(e);
    }

    @Override
    public void addLast(ITreeNode e) {
        ((TreeNode) e).name = name;
        ((TreeNode) e).parent = parent;
        super.addLast(e);
    }

    @Override
    public ITreeNode set(int index, ITreeNode e) {
        if (e instanceof TreeNode) {
            ((TreeNode) e).name = name;
            ((TreeNode) e).parent = parent;
        }
        return super.set(index, e);
    }

    public ITreeNode createObject() {
        TreeNode ret = new TreeNode(name, this);
        super.add(ret);
        return ret;
    }

    public ITreeNode getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }
}
