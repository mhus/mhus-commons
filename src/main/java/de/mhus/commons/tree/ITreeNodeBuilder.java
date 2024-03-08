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
package de.mhus.commons.tree;

import de.mhus.commons.errors.MException;
import de.mhus.commons.errors.RC;
import de.mhus.commons.tools.MString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ITreeNodeBuilder {

    public abstract ITreeNode read(InputStream is) throws MException;

    public abstract void write(ITreeNode node, OutputStream os) throws MException;

    public ITreeNode readFromFile(File file) throws MException {
        try (FileInputStream is = new FileInputStream(file)) {
            return read(is);
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, file, e);
        }
    }

    public ITreeNode readFromString(String content) throws MException {
        return read(new ByteArrayInputStream(MString.toBytes(content)));
    }

    public void writeToFile(ITreeNode node, File file) throws MException {
        try (FileOutputStream os = new FileOutputStream(file)) {
            write(node, os);
        } catch (IOException e) {
            throw new MException(RC.STATUS.ERROR, file, e);
        }
    }

    public String writeToString(ITreeNode node) throws MException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        write(node, os);
        return MString.byteToString(os.toByteArray());
    }
}
