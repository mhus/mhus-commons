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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Getter
@Builder
public class Zip {

    private File src;
    private InputStream srcStream;
    private File dst;
    private FileFilter filter;
    private List<String> errors;

    public Zip zip() throws IOException {
        errors = new ArrayList<>();
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(dst));
        for (File file : src.listFiles()) {
            addFile(zip, file, "");
        }
        return this;
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
                errors.add(file.getPath());
            }
        }
    }


    public Zip unzip() throws ZipException, IOException {
        errors = new ArrayList<>();
        if (srcStream != null) {
            unzipStream();
        } else {
            unzipFile();
        }
        return this;
    }

    protected void unzipStream() throws ZipException, IOException {
        ZipInputStream zis = new ZipInputStream(srcStream);
        String fileName = null;
        while (true) {
            try {
                fileName = null;
                ZipEntry entry = zis.getNextEntry();
                if (entry == null) break;

                fileName = MFile.normalizePath(entry.getName());
                if (entry.isDirectory()) {
                    File newFile = new File(dst, fileName);
                    newFile.mkdirs();
                } else {
                    File newFile = new File(dst, fileName);
                    LOGGER.trace("Unzipping to {}", newFile.getAbsolutePath());
                    //create directories for sub directories in zip
                    newFile.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    MFile.copyFile(zis, fos);
                    fos.close();
                    //close this ZipEntry
                    zis.closeEntry();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to unzip file to {}", fileName, e);
                errors.add(src.toString());
            }
        }
    }

    protected void unzipFile() throws IOException {
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
                try {
                    File parent = dstFile.getParentFile();
                    if (!parent.exists()) {
                        LOGGER.trace("  Create parent directory {} ", parent.getAbsolutePath());
                        parent.mkdirs();
                    }
                    BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dstFile));
                    MFile.copyFile(zipFile.getInputStream(entry), os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    LOGGER.error("Failed to unzip file {} to {}", entry.getName(), dstFile.getAbsolutePath(), e);
                    errors.add(entry.getName());
                }
            }
        }

        zipFile.close();
    }

}
