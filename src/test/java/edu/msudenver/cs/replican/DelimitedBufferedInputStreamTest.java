package edu.msudenver.cs.replican;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DelimitedBufferedInputStreamTest {

    @Test
    public void readArrayDoesNotCorruptBuffer() throws IOException {
        byte[] src = "hello".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src));
        byte[] dest = new byte[5];
        int n = dbis.read(dest, 0, 5);
        assertEquals(5, n);
        assertArrayEquals(src, dest);
    }

    @Test
    public void readArrayWithOffsetAndLen() throws IOException {
        byte[] src = "hello".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src));
        byte[] dest = new byte[10];
        int n = dbis.read(dest, 2, 5);
        assertEquals(5, n);
        assertEquals('h', dest[2]);
        assertEquals('e', dest[3]);
        assertEquals('l', dest[4]);
        assertEquals('l', dest[5]);
        assertEquals('o', dest[6]);
    }

    @Test
    public void readSingleByte() throws IOException {
        byte[] src = "ab".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src));
        assertEquals('a', dbis.read());
        assertEquals('b', dbis.read());
        assertEquals(-1, dbis.read());
    }
}
