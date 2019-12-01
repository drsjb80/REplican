package edu.msudenver.cs.replican;

import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {
    private final Utils utils = new Utils();

    @Test
    public void matches() throws Throwable {
        String[] REs1 = {"foo", "bar"};
        assertTrue((Boolean) P38.call("matches", utils, new Object[]{REs1, "foo"}));
        assertTrue((Boolean) P38.call("matches", utils, new Object[]{REs1, "bar"}));
        String[] REs2 = {"^f.*"};
        assertTrue((Boolean) P38.call("matches", utils, new Object[]{REs2, "foo"}));
        assertFalse((Boolean) P38.call("matches", utils, new Object[]{REs2, "bar"}));
    }

    @Test
    public void replaceAll() {
        String[] pairs = {"\\.wmv.*", ".wmv"};
        assertEquals(Utils.replaceAll("filename.wmvandotherstuff", pairs), "filename.wmv");

        String[] one = {"\\.wmv.*"};
        assertEquals(Utils.replaceAll("filename.wmvandotherstuff", one), "filename.wmvandotherstuff");

        try {
            Utils.replaceAll(null, null);
            fail("Expected a NullPointerException to be thrown");
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void blurf() {
        assertTrue(Utils.blurf(null, null, null, true));
        assertFalse(Utils.blurf(null, null, null, false));
        assertTrue(Utils.blurf(new String[]{"foo"}, null, "foo", true));
        assertFalse(Utils.blurf(new String[]{"bar"}, null, "foo", true));
        assertTrue(Utils.blurf(null, new String[]{"bar"}, "foo", true));
        assertFalse(Utils.blurf(null, new String[]{"foo"}, "foo", true));
    }

    @Test
    public void hostToDomain() {
        assertEquals(Utils.hostToDomain("www.foo.bar"), "foo.bar");
    }

    @Test
    public void combineArrays() {
        assertNull(Utils.combineArrays(null, null));
        assertArrayEquals(Utils.combineArrays(new String[]{"foo"}, null), new String[]{"foo"});
        assertArrayEquals(Utils.combineArrays(null, new String[]{"foo"}), new String[]{"foo"});
        assertArrayEquals(Utils.combineArrays(new String[]{"foo"}, new String[]{"bar"}), new String[]{"foo", "bar"});
    }

    @Test
    public void snooze() {
    }

    @Test
    public void speed() {
        assertEquals(Utils.speed(1, 1, 1), "1 Bps");
        assertEquals(Utils.speed(1, 2, 1), "1 Bps");
        assertEquals(Utils.speed(1, 2, 2), "2 Bps");
        assertEquals(Utils.speed(1, 2, 2000), "2 KBps");
        assertEquals(Utils.speed(1, 2, 2000000), "2 MBps");
        assertEquals(Utils.speed(1, 2, 2000000000), "2 GBps");
        assertEquals(Utils.speed(1, 2, 2000000000000L), "2 TBps");
        assertEquals(Utils.speed(1, 2, 2000000000000000L), "2 EBps");
    }

    @Test
    public void match() throws Throwable {
        assertEquals(P38.call("match", utils, new Object[]{".*", "foo"}),"foo");
        assertNull(P38.call("match", utils, new Object[]{"bar", "foo"}));
        assertEquals(P38.call("match", utils, new Object[]{"foo", "foo"}), "foo");
        assertEquals(P38.call("match", utils, new Object[]{"(f)(o)(o)", "foo"}), "foo");
    }

    @Test
    public void newBase() {
        // <[bB][aA][sS][eE].*[hH][rR][eE][fF]=["']?([^"'# ]*)
        assertNull(Utils.newBase("this is not a new base"));
    }

    @Test
    public void interesting() {
    }
}