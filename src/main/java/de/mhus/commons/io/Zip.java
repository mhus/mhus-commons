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
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

@Slf4j
@Getter
@Builder
public class Zip {

    private File src;
    private File dst;
    private FileFilter filter;

    public void zip() throws IOException {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(dst));
        for (File file : src.listFiles()) {
            addFile(zip, file, "");
        }
    }

    private void addFile(ZipOutputStream zip, File file, String s) {
        if (filter == null || !filter.accept(file)) {
            try {
                if (file.isDirectory()) {
                    for (File f : file.listFiles()) {
                        addFile(zip, f, s + file.getName() + "/");
                    }
                } else {
                    ZipEntry entry = new ZipEntry(s + file.getName());
                    zip.putNextEntry(entry);
                    try (FileInputStream fin = new FileInputStream(file)) {
                        MFile.copyFile(fin, zip);
                    }
                    zip.closeEntry();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to write file {} to zip {}", file, dst, e);
            }
        }
    }


    public void unzip() throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(src);

        Enumeration<?> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            final var name = MFile.normalizePath(entry.getName());

            if (entry.isDirectory()) {
                // Assume directories are stored parents first then children.
                // System.err.println("Extracting directory: " + entry.getName());
                LOGGER.trace("Unzip directory {} to {}", entry.getName(), name);
                // This is not robust, just for demonstration purposes.
                (new File(dst, name)).mkdir();
                continue;
            }

            File dstFile = new File(dst, name);
            if (filter == null || !filter.accept(dstFile)) {
                LOGGER.trace("Unzip file {} to {}", entry.getName(), dstFile.getAbsolutePath());
                File parent = dstFile.getParentFile();
                if (!parent.exists()) {
                    LOGGER.trace("  Create parent directory {} ", parent.getAbsolutePath());
                    parent.mkdirs();
                }
                BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dstFile));
                MFile.copyFile(zipFile.getInputStream(entry), os);
                os.flush();
                os.close();
            }
        }

        zipFile.close();
    }
}
