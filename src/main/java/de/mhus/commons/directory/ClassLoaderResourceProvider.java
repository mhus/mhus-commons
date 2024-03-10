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
package de.mhus.commons.directory;

import de.mhus.commons.tools.MString;
import de.mhus.commons.tools.MSystem;
import de.mhus.commons.tree.ITreeNode;
import de.mhus.commons.tree.TreeNode;
import de.mhus.commons.tree.TreeNodeList;

import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClassLoaderResourceProvider extends MResourceProvider {

    private ClassLoader loader;

    public ClassLoaderResourceProvider() {
        // this(Thread.currentThread().getContextClassLoader());
        this(ClassLoaderResourceProvider.class.getClassLoader());
    }

    public ClassLoaderResourceProvider(ClassLoader loader) {
        this.loader = loader;
    }

    @Override
    public ITreeNode getResourceByPath(String name) {
        return new CLResourceNode(name);
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    private static class CLResourceNode extends TreeNode {

        private static final long serialVersionUID = 1L;
        private String name;

        public CLResourceNode(String name) {
            this.name = name;
        }

        @Override
        public List<String> getPropertyKeys() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Optional<ITreeNode> getObject(String key) {
            return Optional.empty();
        }

        @Override
        public List<ITreeNode> getObjects() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public Optional<TreeNodeList> getArray(String key) {
            return Optional.of(new TreeNodeList(key, this));
        }

        @Override
        public List<String> getObjectKeys() {
            return Collections.EMPTY_LIST;
        }

        @Override
        public String getName() {
            return MString.afterLastIndex(name, '/');
        }

        @Override
        public ITreeNode getParent() {
            return null;
        }

        @Override
        public Object getProperty(String name) {
            return null;
        }

        @Override
        public boolean isProperty(String name) {
            return false;
        }

        @Override
        public void removeProperty(String key) {
        }

        @Override
        public void setProperty(String key, Object value) {
        }

        @Override
        public boolean isEditable() {
            return false;
        }
    }

    @Override
    public ITreeNode getResourceById(String id) {
        return getResourceByPath(id);
    }

    @Override
    public String getName() {
        return MSystem.getObjectId(this);
    }

    @Override
    public InputStream getInputStream(String key) {
        if (key == null)
            return loader.getResourceAsStream(key);
        return null;
    }

    @Override
    public URL getUrl(String key) {
        return loader.getResource(key);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}
