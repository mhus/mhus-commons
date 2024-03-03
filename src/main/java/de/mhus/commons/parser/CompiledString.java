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
package de.mhus.commons.parser;

import de.mhus.commons.errors.MException;
import de.mhus.commons.lang.IValuesProvider;
import de.mhus.commons.util.MapValuesProvider;

import java.util.LinkedList;
import java.util.Map;

/**
 * Parsed and tree representated compiled variant of the original string. This is used to output the
 * changed representation of the string.
 *
 * @author mikehummel
 */
public class CompiledString {

    private StringPart[] compiled;

    public CompiledString(StringPart[] compiled) {
        this.compiled = compiled;
    }

    public CompiledString(LinkedList<StringPart> compiled) {
        this.compiled = compiled.toArray(new StringPart[compiled.size()]);
    }

    public String execute(Map<String, Object> attributes) throws MException {
        return execute(new MapValuesProvider(attributes));
    }

    /**
     * Return the new, compiled string.
     *
     * @param attributes
     * @return the resulting string
     * @throws MException
     */
    public String execute(IValuesProvider attributes) throws MException {
        StringBuilder out = new StringBuilder();
        execute(out, attributes);
        return out.toString();
    }

    public void execute(StringBuilder sb, IValuesProvider attributes) throws MException {
        for (StringPart part : compiled) {
            part.execute(sb, attributes);
        }
    }

    /**
     * Return a readable information about the tree structure.
     *
     * @param sb
     */
    public void dump(StringBuilder sb) {
        for (StringPart part : compiled) {
            part.dump(0, sb);
        }
    }
}
