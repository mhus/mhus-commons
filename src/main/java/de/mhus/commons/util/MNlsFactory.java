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

import de.mhus.commons.MString;
import de.mhus.commons.MSystem;
import de.mhus.commons.directory.ClassLoaderResourceProvider;
import de.mhus.commons.directory.MResourceProvider;
import de.mhus.commons.node.INode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class MNlsFactory extends MNlsBundle {

    private static MNlsFactory instance;
    @SuppressWarnings("unused")
    private INode config;

    public MNlsFactory() {
        this(null);
        //		forkBase();
    }

    public MNlsFactory(INode config) {
        this.config = config;
    }

    public MNls create(Object owner) {
        return load(null, null, toResourceName(owner), null);
    }

    public MNls load(Class<?> owner) {
        return load(null, owner, null, null);
    }

    public static String toResourceName(Object owner) {
        if (owner == null) return null;
        if (owner instanceof String) return (String) owner;
        return MSystem.getClassName(owner).replace('.', '/');
    }

    public MNls load(Class<?> owner, Locale locale) {
        return load(null, owner, null, locale == null ? null : locale.toString());
    }

    public MNls load(MResourceProvider res, Class<?> owner, String resourceName, String locale) {
        return load(res, owner, resourceName, locale, true);
    }

    public MNls load(
            MResourceProvider res,
            Class<?> owner,
            String resourceName,
            String locale,
            boolean searchAlternatives) {
        return load(res, owner, resourceName, locale, searchAlternatives, 0);
    }

    protected MNls load(
            MResourceProvider res,
            Class<?> owner,
            String resourceName,
            String locale,
            boolean searchAlternatives,
            int level) {
        if (level > 50) return null;
        try {
            // if (res == null) res = base(MDirectory.class);

            if (resourceName == null) {
                if (owner.getCanonicalName() != null)
                    resourceName = owner.getCanonicalName().replace('.', '/');
                else resourceName = owner.getEnclosingClass().getCanonicalName().replace('.', '/');
            }

            if (res == null) {
                res = findResourceProvider(owner);
            }

            if (locale == null) locale = getDefaultLocale();

            InputStream is = null;
            Properties properties = new Properties();

            is = res.getInputStream(locale.toString() + "/" + resourceName + ".properties");
            String prefix = getResourcePrefix();

            if (searchAlternatives) {

                if (prefix != null && is == null)
                    is =
                            res.getInputStream(
                                    prefix
                                            + "/"
                                            + getDefaultLocale()
                                            + "/"
                                            + resourceName
                                            + ".properties");
                if (is == null)
                    is =
                            res.getInputStream(
                                    getDefaultLocale() + "/" + resourceName + ".properties");
                if (prefix != null && is == null)
                    is = res.getInputStream(prefix + "/" + resourceName + ".properties");
                if (is == null) is = res.getInputStream(resourceName + ".properties");
            }

            if (is != null) {
                LOGGER.trace("Load Resource {} {}", resourceName, locale);
                InputStreamReader r = new InputStreamReader(is, MString.CHARSET_UTF_8);
                properties.load(r);
                is.close();

                for (String include : properties.getProperty(".include", "").split(",")) {
                    include = include.trim();
                    MNls parent = load(null, null, include, locale, false, level + 1);
                    if (parent != null) {
                        for (Map.Entry<Object, Object> entry : parent.properties.entrySet()) {
                            if (!properties.containsKey(entry.getKey()))
                                properties.put(entry.getKey(), entry.getValue());
                        }
                    }
                }

                return new MNls(properties, "");
            } else {
                LOGGER.debug("Resource not found {} {}", resourceName, locale);
            }

        } catch (Throwable e) {
            LOGGER.error("Error", e);
        }

        return new MNls();
    }

    protected String getResourcePrefix() {
        return null;
    }

    protected MResourceProvider findResourceProvider(Class<?> owner) {
        if (owner != null) return new ClassLoaderResourceProvider(owner.getClassLoader());
        else return new ClassLoaderResourceProvider();
    }

    public String getDefaultLocale() {
        return Locale.getDefault().toString();
    }

    public MNls load(InputStream is) {
        Properties properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            LOGGER.error("Error", e);
        }
        return new MNls(properties, "");
    }

    public synchronized static MNlsFactory lookup(Object owner) {
        if (instance == null)
            instance= new MNlsFactory();
        return instance;
    }
    public static void setFactory(MNlsFactory inst) {
        if (instance != null)
            LOGGER.info("Overload MNlsFactory");
        instance = inst;
    }

    @Override
    public MNls createNls(String locale) {
        return load(null, null, getPath(), locale, false);
    }
}
