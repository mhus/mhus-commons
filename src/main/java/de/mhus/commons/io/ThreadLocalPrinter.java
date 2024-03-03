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
package de.mhus.commons.io;

import de.mhus.commons.lang.ICloseable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ThreadLocalPrinter extends PrintStream {

    public ThreadLocalPrinter(OutputStream parent) {
        super(new InnerOutputStream(parent));
    }

    public ICloseable use(OutputStream stream) {
        OutputStream current = ((InnerOutputStream) out).output.get();
        ((InnerOutputStream) out).output.set(stream);
        return new InnerCloseable(current);
    }

    private static class InnerOutputStream extends OutputStream {

        private OutputStream defaultOutput;
        private ThreadLocal<OutputStream> output = new ThreadLocal<>();

        public InnerOutputStream(OutputStream defaultOutput) {
            this.defaultOutput = defaultOutput;
        }

        @Override
        public void write(int b) throws IOException {
            OutputStream out = output.get();
            if (out == null) out = defaultOutput;
            out.write(b);
        }

        @Override
        public void flush() throws IOException {
            OutputStream out = output.get();
            if (out == null) out = defaultOutput;
            out.flush();
        }

        @Override
        public void close() throws IOException {
            OutputStream out = output.get();
            if (out == null) out = defaultOutput;
            out.close();
        }
    }

    private class InnerCloseable implements ICloseable {

        private OutputStream last;

        public InnerCloseable(OutputStream last) {
            this.last = last;
        }

        @Override
        public void close() {
            ((InnerOutputStream) out).output.set(last);
        }
    }

    public OutputStream current() {
        return ((InnerOutputStream) out).output.get();
    }
}
