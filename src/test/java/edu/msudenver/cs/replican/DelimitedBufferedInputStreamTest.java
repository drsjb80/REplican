package edu.msudenver.cs.replican;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Test
    public void getStringsReturnsEmptyListByDefault() throws IOException {
        byte[] src = "hello".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src));
        byte[] dest = new byte[5];
        dbis.read(dest, 0, 5);
        assertEquals(0, dbis.getStrings().size());
    }

    @Test
    public void getStringsCollectsDelimitedContent() throws IOException {
        byte[] src = "<tag>content</tag>".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src), '<', '>');
        byte[] dest = new byte[src.length];
        dbis.read(dest, 0, src.length);
        assertEquals(2, dbis.getStrings().size());
        assertEquals("<tag>", dbis.getStrings().get(0));
        assertEquals("</tag>", dbis.getStrings().get(1));
    }

    @Test
    public void getStringsWithMultipleDelimitedSections() throws IOException {
        byte[] src = "<a>1</a><b>2</b>".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src), '<', '>');
        byte[] dest = new byte[src.length];
        dbis.read(dest, 0, src.length);
        assertEquals(4, dbis.getStrings().size());
    }

    @Test
    public void readWithDefaultDelimiter() throws IOException {
        byte[] src = "before\"quoted\"after".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src));
        byte[] dest = new byte[src.length];
        dbis.read(dest, 0, src.length);
        // Default delimiter is quote, collects content between quotes
        assertEquals(0, dbis.getStrings().size());
    }

    @Test
    public void readWithCustomSingleDelimiter() throws IOException {
        byte[] src = "|start|content|".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src), '|');
        byte[] dest = new byte[src.length];
        dbis.read(dest, 0, src.length);
        // Only collects complete delimited sections
        assertEquals(1, dbis.getStrings().size());
        assertEquals("|start|", dbis.getStrings().get(0));
    }

    @Test
    public void readEmptyStream() throws IOException {
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(new byte[0]));
        byte[] dest = new byte[10];
        int n = dbis.read(dest, 0, 10);
        assertEquals(-1, n);
        assertEquals(0, dbis.getStrings().size());
    }

    @Test
    public void readPartialArray() throws IOException {
        byte[] src = "hello world".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src));
        byte[] dest = new byte[20];
        int n = dbis.read(dest, 5, 5);
        assertEquals(5, n);
        assertEquals('h', dest[5]);
        assertEquals('e', dest[6]);
    }

    @Test
    public void getStringsWithNoCompleteDelimiters() throws IOException {
        byte[] src = "<unclosed".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src), '<', '>');
        byte[] dest = new byte[src.length];
        dbis.read(dest, 0, src.length);
        assertEquals(0, dbis.getStrings().size());
    }

    @Test
    public void readWithDifferentStartEndDelimiters() throws IOException {
        byte[] src = "[start]content[end]".getBytes();
        DelimitedBufferedInputStream dbis =
            new DelimitedBufferedInputStream(new ByteArrayInputStream(src), '[', ']');
        byte[] dest = new byte[src.length];
        dbis.read(dest, 0, src.length);
        assertEquals(2, dbis.getStrings().size());
        assertEquals("[start]", dbis.getStrings().get(0));
        assertEquals("[end]", dbis.getStrings().get(1));
    }
}
