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

import java.util.Enumeration;
import java.util.Iterator;

public class IteratorEnumerator<T> implements Enumeration<T> {

    private Iterator<T> iter;

    public IteratorEnumerator(Iterator<T> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasMoreElements() {
        return iter.hasNext();
    }

    @Override
    public T nextElement() {
        return iter.next();
    }
}
