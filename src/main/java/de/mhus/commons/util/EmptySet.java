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

import de.mhus.commons.M;
import de.mhus.commons.lang.ReadOnly;
import de.mhus.commons.tools.MObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class EmptySet<T> implements Set<T>, ReadOnly {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public Iterator<T> iterator() {
        return new EmptyIterator<T>();
    }

    @Override
    public Object[] toArray() {
        return M.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public <E> E[] toArray(E[] a) {
        return (E[]) MObject.newArray(a.getClass().getComponentType(), 0);
    }

    @Override
    public boolean add(T e) {
        return false;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return false;
    }

    @Override
    public void clear() {
    }
}
