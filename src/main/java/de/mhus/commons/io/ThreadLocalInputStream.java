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

import de.mhus.commons.lang.ICloseable;

import java.io.IOException;
import java.io.InputStream;

public class ThreadLocalInputStream extends InputStream {

    private InputStream defaultIn;
    private ThreadLocal<InputStream> input = new ThreadLocal<>();

    public ThreadLocalInputStream(InputStream defaultIn) {
        this.defaultIn = defaultIn;
    }

    @Override
    public int read() throws IOException {
        InputStream in = input.get();
        if (in == null)
            in = defaultIn;
        return in.read();
    }

    @Override
    public void close() throws IOException {
        InputStream in = input.get();
        if (in == null)
            in = defaultIn;
        in.close();
    }

    public ICloseable use(InputStream in) {
        InputStream current = input.get();
        input.set(in);
        return new InnerCloseable(current);
    }

    private class InnerCloseable implements ICloseable {

        private InputStream last;

        public InnerCloseable(InputStream last) {
            this.last = last;
        }

        @Override
        public void close() {
            input.set(last);
        }
    }

    public InputStream current() {
        return input.get();
    }
}
