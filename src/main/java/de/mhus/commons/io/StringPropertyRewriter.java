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
package de.mhus.commons.io;

import de.mhus.commons.tools.MFile;
import de.mhus.commons.tools.MString;
import de.mhus.commons.parser.StringPropertyReplacer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Rewrite the incoming stream using the string replacer. The rewriter need to load the full content of the stream in
 * memory twice.
 *
 * @author mikehummel
 */
public class StringPropertyRewriter implements StreamRewriter {

    private StringPropertyReplacer replacer;

    public StringPropertyRewriter(StringPropertyReplacer replacer) {
        this.replacer = replacer;
    }

    @Override
    public InputStream rewriteContent(String name, InputStream in) {
        String content = MFile.readFile(in);
        content = replacer.process(content);
        return new ByteArrayInputStream(MString.toBytes(content));
    }
}
