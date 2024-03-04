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
package de.mhus.commons.util;

import de.mhus.commons.errors.MException;
import de.mhus.commons.node.ITreeNode;

import java.util.Collection;
import java.util.TreeMap;

public class ParameterDefinitions extends TreeMap<String, ParameterDefinition> {

    private static final long serialVersionUID = 1L;

    /**
     * Format of lines: name,key:value,key:value...
     *
     * <p>Keys: type:[type] format:[format] mandatory:[bool] default:[default]
     *
     * @param definitions
     * @return Parameter definition
     */
    public static ParameterDefinitions create(Collection<String> definitions) {
        ParameterDefinitions out = new ParameterDefinitions();
        for (String line : definitions) {
            ParameterDefinition def = new ParameterDefinition(line);
            out.put(def.getName(), def);
        }
        return out;
    }

    public static ParameterDefinitions create(ITreeNode form) throws MException {
        ParameterDefinitions out = new ParameterDefinitions();
        collect(form, out);
        return out;
    }

    private static void collect(ITreeNode form, ParameterDefinitions out) throws MException {

        for (ITreeNode node : form.getObjects()) {
            collect(node, out);
        }
    }
}
