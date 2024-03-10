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
import java.util.List;

public class MTree {

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
        return array.stream().map((entry) -> entry.getString(ITreeNode.VALUE).orElse("")).toList();
    }

}
