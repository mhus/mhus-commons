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

import de.mhus.commons.MSystem;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;

public class MObjectInputStream extends ObjectInputStream {

    private ClassLoader cl = null;

    public MObjectInputStream() throws IOException, SecurityException {
        super();
    }

    public MObjectInputStream(InputStream in, ClassLoader cl) throws IOException {
        super(in);
        setClassLoader(cl);
    }

    public MObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    public void setClassLoader(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {
        String name = desc.getName();

        try {
            if (cl != null) return MSystem.newInstance(cl, name);
        } catch (Exception ex) {
        }

        try {
            return Class.forName(name, true, cl);
        } catch (ClassNotFoundException ex) {
            return super.resolveClass(desc);
        }
    }

    @Override
    protected Class<?> resolveProxyClass(String[] interfaces)
            throws IOException, ClassNotFoundException {
        ClassLoader latestLoader = cl;
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;

        // define proxy in class loader of non-public interface(s), if any
        @SuppressWarnings("rawtypes")
        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            @SuppressWarnings("rawtypes")
            Class cl = Class.forName(interfaces[i], false, latestLoader);
            if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
                if (hasNonPublicInterface) {
                    if (nonPublicLoader != cl.getClassLoader()) {
                        throw new IllegalAccessError(
                                "conflicting non-public interface class loaders");
                    }
                } else {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                }
            }
            classObjs[i] = cl;
        }
        try {
            @SuppressWarnings("deprecation")
            Class<?> proxyClass =
                    Proxy.getProxyClass(
                            hasNonPublicInterface ? nonPublicLoader : latestLoader, classObjs);
            return proxyClass;
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }
}
