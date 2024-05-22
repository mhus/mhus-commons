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

import de.mhus.commons.errors.InternalRuntimeException;
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Builder()
public class Zip {

    /**
     * Source file or directory.
     */
    private File src;
    /**
     * Source stream as alternative to src. Has priority before src.
     */
    private InputStream srcStream;
    /**
     * Destination file or directory.
     */
    private File dst;
    /**
     * Destination stream as alternative to dst. Has priority before dst.
     */
    private OutputStream dstStream;
    /**
     * Filter to exclude files from the zip.
     */
    private FileFilter filter;
    private final List<String> errors = new ArrayList<>(); // make final to avoid handled by builder
    /**
     * If true an InternalRuntimeException will be thrown if an error occurs. if false the error will be stored in the
     * error list.
     */
    private boolean throwException;
    /**
     * If true no log output will be generated.
     */
    private boolean quiet = false;

    private Consumer<Event> consumer;

    public Zip gzip() {
        gzipInternal();
        if (throwException && !errors.isEmpty())
            throw new InternalRuntimeException("Failed to gzip file " + src + " to " + dst + " " + errors);
        return this;
    }

    private void gzipInternal() {
        errors.clear();
        if (src == null && srcStream == null) {
            errors.add("Source is null");
            return;
        }
        if (dst == null && dstStream == null) {
            errors.add("Destination is null");
            return;
        }
        if (srcStream == null && !src.exists() && !src.isFile()) {
            errors.add("Source does not exist");
            return;
        }
        if (srcStream == null && !src.isFile()) {
            errors.add("Source is not a file");
            return;
        }
        if (dstStream == null && dst.exists() && !dst.isFile()) {
            errors.add("Destination is not a file");
            return;
        }
        try {
            if (srcStream == null)
                src.getParentFile().mkdirs();
            var is = srcStream != null ? srcStream : new FileInputStream(src);
            try (GZIPOutputStream zip = new GZIPOutputStream(
                    dstStream != null ? dstStream : new FileOutputStream(dst))) {
                MFile.copyFile(is, zip);
            } finally {
                if (srcStream == null)
                    is.close();
            }
        } catch (Exception e) {
            if (!quiet)
                LOGGER.error("Failed to gzip file {} to {}", src, dst, e);
            errors.add(e.toString());
        }
    }

    public Zip ungzip() {
        ungzipInternal();
        if (throwException && !errors.isEmpty())
            throw new InternalRuntimeException("Failed to gzip file " + src + " to " + dst + " " + errors);
        return this;
    }

    private void ungzipInternal() {
        errors.clear();
        if (src == null && srcStream == null) {
            errors.add("Source is null");
            return;
        }
        if (dst == null && dstStream == null) {
            errors.add("Destination is null");
            return;
        }
        if (srcStream == null && !src.exists()) {
            errors.add("Source does not exist");
            return;
        }
        if (srcStream == null && !src.isFile()) {
            errors.add("Source is not a file");
            return;
        }
        if (dstStream == null && dst.exists() && !dst.isFile()) {
            errors.add("Destination is not a file");
            return;
        }
        try {
            if (dstStream == null)
                dst.getParentFile().mkdirs();
            var os = dstStream != null ? dstStream : new FileOutputStream(dst);
            try (GZIPInputStream zip = new GZIPInputStream(srcStream != null ? srcStream : new FileInputStream(src))) {
                MFile.copyFile(zip, os);
            } finally {
                if (dstStream == null)
                    os.close();
            }
        } catch (Exception e) {
            errors.add(e.toString());
            if (!quiet)
                LOGGER.error("Failed to ungzip file {} to {}", src, dst, e);
            if (consumer != null)
                consumer.accept(new Event(EVENT_TYPE.ERROR, e.toString(), null));
        }
    }

    public Zip zip() {
        zipInternal();
        if (throwException && !errors.isEmpty())
            throw new InternalRuntimeException("Failed to gzip file " + src + " to " + dst + " " + errors);
        return this;
    }

    private void zipInternal() {
        errors.clear();
        if (src == null && srcStream == null) {
            errors.add("Source is null");
            return;
        }
        if (dst == null && dstStream == null) {
            errors.add("Destination is null");
            return;
        }
        if (srcStream == null && !src.exists()) {
            errors.add("Source does not exist");
            return;
        }
        if (dstStream == null && dst.exists() && !dst.isFile()) {
            errors.add("Destination is not a file");
            return;
        }
        try {
            try (ZipOutputStream zip = new ZipOutputStream(dstStream != null ? dstStream : new FileOutputStream(dst))) {
                if (src.isFile()) {
                    addFile(zip, src, "");
                } else {
                    for (File file : src.listFiles()) {
                        addFile(zip, file, "");
                    }
                }
            }
        } catch (Exception e) {
            errors.add(e.toString());
            if (!quiet)
                LOGGER.error("Failed to zip file {} to {}", src, dst, e);
            if (consumer != null)
                consumer.accept(new Event(EVENT_TYPE.ERROR, e.toString(), dst));
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
                    if (consumer != null)
                        consumer.accept(new Event(EVENT_TYPE.ZIP, "Zip " + s + file.getName(), null));
                    ZipEntry entry = new ZipEntry(s + file.getName());
                    zip.putNextEntry(entry);
                    try (FileInputStream fin = new FileInputStream(file)) {
                        MFile.copyFile(fin, zip);
                    }
                    zip.closeEntry();
                }
            } catch (IOException e) {
                if (!quiet)
                    LOGGER.error("Failed to write file {} to zip {}", file, dst, e);
                errors.add(file.getPath());
            }
        }
    }

    public Zip unzip() {
        unzipInternal();
        if (throwException && !errors.isEmpty())
            throw new InternalRuntimeException("Failed to gzip file " + src + " to " + dst + " " + errors);
        return this;
    }

    private void unzipInternal() {
        errors.clear();
        if (src == null && srcStream == null) {
            errors.add("Source is null");
            return;
        }
        if (dst == null && dstStream == null) {
            errors.add("Destination is null");
            return;
        }
        if (srcStream == null && !src.exists()) {
            errors.add("Source does not exist");
            return;
        }
        if (srcStream == null && !src.isFile()) {
            errors.add("Source is not a file");
            return;
        }
        try {
            if (srcStream != null) {
                unzipStream();
            } else {
                unzipFile();
            }
        } catch (Exception e) {
            if (!quiet)
                LOGGER.error("Failed to unzip file {} to {}", src, dst, e);
            errors.add(e.toString());
        }
    }

    protected void unzipStream() throws ZipException, IOException {
        ZipInputStream zis = new ZipInputStream(srcStream);
        String fileName = null;
        while (true) {
            try {
                fileName = null;
                ZipEntry entry = zis.getNextEntry();
                if (entry == null)
                    break;

                fileName = MFile.normalizePath(entry.getName());
                if (entry.isDirectory()) {
                    File newFile = new File(dst, fileName);
                    newFile.mkdirs();
                } else {
                    File newFile = new File(dst, fileName);
                    if (!quiet)
                        LOGGER.trace("Unzipping to {}", newFile.getAbsolutePath());
                    if (consumer != null)
                        consumer.accept(new Event(EVENT_TYPE.UNZIP, "Unzip", newFile));
                    // create directories for sub directories in zip
                    newFile.getParentFile().mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    MFile.copyFile(zis, fos);
                    fos.close();
                    // close this ZipEntry
                    zis.closeEntry();
                }
            } catch (Exception e) {
                errors.add(e.toString());
                if (!quiet)
                    LOGGER.error("Failed to unzip file to {}", fileName, e);
                if (consumer != null)
                    consumer.accept(new Event(EVENT_TYPE.ERROR, e.toString(), null));
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
                if (!quiet)
                    LOGGER.trace("Unzip directory {} to {}", entry.getName(), name);
                (new File(dst, name)).mkdir();
                continue;
            }

            File dstFile = new File(dst, name);
            if (filter == null || !filter.accept(dstFile)) {
                if (!quiet)
                    LOGGER.trace("Unzip file {} to {}", entry.getName(), dstFile.getAbsolutePath());
                if (consumer != null)
                    consumer.accept(new Event(EVENT_TYPE.UNZIP, "Unzip", dstFile));
                try {
                    File parent = dstFile.getParentFile();
                    if (!parent.exists()) {
                        if (!quiet)
                            LOGGER.trace("  Create parent directory {} ", parent.getAbsolutePath());
                        parent.mkdirs();
                    }
                    BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(dstFile));
                    MFile.copyFile(zipFile.getInputStream(entry), os);
                    os.flush();
                    os.close();
                } catch (Exception e) {
                    errors.add(entry.getName() + " " + e.toString());
                    if (!quiet)
                        LOGGER.error("Failed to unzip file {} to {}", entry.getName(), dstFile.getAbsolutePath(), e);
                    if (consumer != null)
                        consumer.accept(new Event(EVENT_TYPE.ERROR, e.toString(), dstFile));
                }
            }
        }

        zipFile.close();
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public enum EVENT_TYPE {
        ZIP, UNZIP, ERROR
    }

    public record Event(EVENT_TYPE type, String message, File file) {
    }

}
