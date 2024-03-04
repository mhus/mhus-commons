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
import de.mhus.commons.errors.RC;
import de.mhus.commons.errors.TooDeepStructuresException;
import de.mhus.commons.yaml.MYaml;
import de.mhus.commons.yaml.YElement;
import de.mhus.commons.yaml.YList;
import de.mhus.commons.yaml.YMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class YamlTreeNodeBuilder extends ITreeNodeBuilder {

    @Override
    public ITreeNode read(InputStream is) {
        YMap itemY = MYaml.load(is);
        TreeNode itemC = new TreeNode();
        if (itemY.isList()) {
            TreeNodeList arrayC = itemC.createArray(ITreeNode.NAMELESS_VALUE);
            fill(arrayC, new YList(itemY.getObject()), 0);
        } else if (itemY.isMap()) fill(itemC, itemY, 0);
        return itemC;
    }

    private void fill(ITreeNode elemC, YMap elemY, int level) {
        if (level > 100) throw new TooDeepStructuresException();

        for (String key : elemY.getKeys()) {
            if (elemY.isList(key)) {
                TreeNodeList arrayC = elemC.createArray(key);
                fill(arrayC, elemY.getList(key), level + 1);
            } else if (elemY.isMap(key)) {
                ITreeNode objC = elemC.createObject(key);
                YMap objY = elemY.getMap(key);
                fill(objC, objY, level + 1);
            } else {
                elemC.put(key, elemY.getObject(key));
            }
        }
    }

    private void fill(TreeNodeList listC, YList listY, int level) {
        if (level > 100) throw new TooDeepStructuresException();

        for (YElement itemY : listY) {
            ITreeNode itemC = listC.createObject();
            if (itemY.isMap()) {
                fill(itemC, itemY.asMap(), level + 1);
            } else if (itemY.isList()) {
                // nameless list in list - not really supported - but ...
                TreeNodeList arrayY2 = itemC.createArray(ITreeNode.NAMELESS_VALUE);
                fill(arrayY2, itemY.asList(), level + 1);
            } else {
                itemC.put(ITreeNode.NAMELESS_VALUE, itemY.getObject());
            }
        }
    }

    @Override
    public void write(ITreeNode node, OutputStream os) throws MException {
        YElement elemY = create(node, 0);
        try {
            MYaml.write(elemY, os);
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, e);
        }
    }

    private YElement create(ITreeNode elemC, int level) {

        if (level > 100) throw new TooDeepStructuresException();

        if (elemC.containsKey(ITreeNode.NAMELESS_VALUE)) {
            if (elemC.isArray(ITreeNode.NAMELESS_VALUE)) {
                YList out = MYaml.createList();
                for (ITreeNode itemC : elemC.getArray(ITreeNode.NAMELESS_VALUE).orElse(null)) {
                    YElement itemY = create(itemC, level + 1);
                    out.add(itemY);
                }
                return out;
            } else if (elemC.isObject(ITreeNode.NAMELESS_VALUE)) {
                return create(elemC.getObject(ITreeNode.NAMELESS_VALUE).orElse(null), level + 1);
            } else return new YElement(elemC.get(ITreeNode.NAMELESS_VALUE));
        }

        YMap elemY = MYaml.createMap();
        for (String key : elemC.getPropertyKeys()) {
            elemY.put(key, new YElement(elemC.getProperty(key)));
        }

        for (String key : elemC.getArrayKeys()) {
            YList listY = MYaml.createList();
            for (ITreeNode itemC : elemC.getArray(key).orElse(null)) {
                YElement itemY = create(itemC, level + 1);
                listY.add(itemY);
            }
            elemY.put(key, listY);
        }

        for (String key : elemC.getObjectKeys()) {
            ITreeNode itemC = elemC.getObject(key).orElse(null);
            YElement itemY = create(itemC, level + 1);
            elemY.put(key, itemY);
        }

        return elemY;
    }
}
