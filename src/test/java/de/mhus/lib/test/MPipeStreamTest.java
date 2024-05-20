package de.mhus.lib.test;

import de.mhus.commons.io.PipedStream;
import de.mhus.lib.test.util.TestCase;
import org.junit.jupiter.api.Test;

import java.io.EOFException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MPipeStreamTest extends TestCase {

    @Test
    public void testSingleWriteRead() throws IOException {
        PipedStream pipe = new PipedStream();
        pipe.getOut().write(1);
        pipe.getOut().write(2);
        pipe.getOut().write(-1);
        pipe.getOut().write(127);
        pipe.getOut().write(255);

        assertEquals(1, pipe.getIn().read());
        assertEquals(2, pipe.getIn().read());
        assertEquals(255, pipe.getIn().read());
        assertEquals(127, pipe.getIn().read());
        assertEquals(255, pipe.getIn().read());
    }

    @Test
    public void testBlockWriteRead() throws IOException {
        PipedStream pipe = new PipedStream(1000);
        for (int x = 0; x < 100; x++) { // 100 x 800 = 80.000
            byte[] buffer = new byte[800];
            for (int i = 0; i < buffer.length; i++)
                buffer[i] = (byte) i;
            pipe.getOut().write(buffer);

            byte[] buffer2 = new byte[800];
            int len = pipe.getIn().read(buffer2);

            assertEquals(buffer.length, len);
            for (int i = 0; i < buffer.length; i++)
                assertEquals(buffer[i], buffer2[i]);
        }
    }

    @Test
    public void testEndOfOutputStream() throws IOException {
        PipedStream pipe = new PipedStream();
        pipe.getOut().write(1);
        pipe.getOut().write(2);
        pipe.getOut().close();

        assertEquals(1, pipe.getIn().read());
        assertEquals(2, pipe.getIn().read());
        assertEquals(-1, pipe.getIn().read());
    }

    @Test
    public void testEndOFInputStream() throws IOException {
        PipedStream pipe = new PipedStream();
        pipe.getOut().write(1);
        pipe.getOut().write(2);

        assertEquals(1, pipe.getIn().read());
        assertEquals(2, pipe.getIn().read());

        pipe.getIn().close();

        assertThrows(EOFException.class, () -> pipe.getOut().write(1));

    }

    @Test
    public void testReadTimeout() throws IOException {
        PipedStream pipe = new PipedStream();
        pipe.setReadTimeout(1);

        pipe.getOut().write(1);
        pipe.getOut().write(2);

        assertEquals(1, pipe.getIn().read());
        assertEquals(2, pipe.getIn().read());

        assertThrows(IOException.class, () -> pipe.getIn().read());
    }

    @Test
    public void testWriteTimeout() throws IOException {
        PipedStream pipe = new PipedStream(2);
        pipe.setWriteTimeout(1);

        pipe.getOut().write(1);
        pipe.getOut().write(2);

        assertThrows(IOException.class, () -> pipe.getOut().write(3));

        assertEquals(1, pipe.getIn().read());
        assertEquals(2, pipe.getIn().read());

    }

    @Test
    public void testWriteBlockTimeout() throws IOException {
        PipedStream pipe = new PipedStream(10);
        pipe.setWriteTimeout(1);
        pipe.setReadTimeout(1);

        byte[] buffer = new byte[8];
        for (int i = 0; i < buffer.length; i++)
            buffer[i] = (byte) i;
        pipe.getOut().write(buffer);
        assertThrows(IOException.class, () -> pipe.getOut().write(buffer));

        byte[] buffer2 = new byte[8];
        int len = pipe.getIn().read(buffer2);
        assertEquals(buffer.length, len);
        for (int i = 0; i < buffer.length; i++)
            assertEquals(buffer[i], buffer2[i]);

        len = pipe.getIn().read(buffer2);
        assertEquals(2, len);
        assertEquals(0, buffer2[0]);
        assertEquals(1, buffer2[1]);

        assertThrows(IOException.class, () -> pipe.getIn().read());

    }
}