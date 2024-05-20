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

import de.mhus.commons.tools.MMath;
import de.mhus.commons.tools.MPeriod;
import de.mhus.commons.tools.MThread;

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PipedStream implements Closeable {

    private final CircularByteBuffer byteBuffer;
    private final Out out = new Out();
    private final In in = new In();
    private long writeTimeout = -1;
    private long readTimeout = -1;
    private boolean outputClosed = false;
    private boolean inputClosed = false;

    public PipedStream() {
        this(10000);
    }

    public PipedStream(int i) {
        byteBuffer = new CircularByteBuffer(i);
    }

    public OutputStream getOut() {
        return out;
    }

    public InputStream getIn() {
        return in;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public int getBufferedSize() {
        return byteBuffer.length();
    }

    public boolean isInputClosed() {
        return inputClosed;
    }

    private class Out extends OutputStream {

        @Override
        public void write(int b) throws IOException {

            if (outputClosed) throw new EOFException();
            long start = System.currentTimeMillis();
            while (byteBuffer.isFull()) {
                MThread.sleep(200);
                if (MPeriod.isTimeOut(start, writeTimeout))
                    throw new IOException("write timeout");
            }
            synchronized (byteBuffer) {
                // System.out.println("Write: " + (char)b + " (" + b + ")");
                byteBuffer.put((byte)b);
            }
        }

        @Override
        public void close() {
            outputClosed = true;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {

            while (true) {

                if (outputClosed) throw new EOFException();

                var free = byteBuffer.free();
                if (free < len) {
                    synchronized (byteBuffer) {
                        write(b, off, byteBuffer.free());
                        off = off + free;
                        len = len - free;
                    }
                }

                long start = System.currentTimeMillis();
                while (byteBuffer.isFull()) {
                    MThread.sleep(200);
                    if (MPeriod.isTimeOut(start, writeTimeout))
                        throw new IOException("write timeout");
                }

                if (byteBuffer.free() >= len) {
                    synchronized (byteBuffer) {
                        byteBuffer.put(b, off, len);
                        return;
                    }
                }
            }

        }



    }

    private class In extends InputStream {

        @Override
        public int read() throws IOException {

            if (inputClosed)
                return -1;

            if (outputClosed && byteBuffer.isEmpty()) {
                if (!inputClosed)
                    close();
                return -1;
            }

            long start = System.currentTimeMillis();
            while (byteBuffer.isEmpty()) {
                MThread.sleep(200);
                if (MPeriod.isTimeOut(start, readTimeout))
                    throw new IOException("read timeout");
            }
            synchronized (byteBuffer) {
                byte o = byteBuffer.get();
                // System.err.println("Read: " + (char)o + "(" + o + ")");
                return MMath.unsignetByteToInt(o);
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (outputClosed && byteBuffer.isEmpty())
                return -1;

            long start = System.currentTimeMillis();
            while (byteBuffer.isEmpty()) {
                MThread.sleep(200);
                if (MPeriod.isTimeOut(start, readTimeout))
                    throw new IOException("read timeout");
            }
            synchronized (byteBuffer) {
                return byteBuffer.get(b, off, len);
            }
        }

        @Override
        public int available() throws IOException {
            return byteBuffer.length();
        }

        @Override
        public void close() {
            outputClosed = true;
            inputClosed = true;
        }
    }

    public boolean isOutputClosed() {
        return outputClosed;
    }

    @Override
    public void close() {
        outputClosed = true;
    }
}
