package com.weisi.tool.wsnbox;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by CJQ on 2018/4/16.
 */

public class ParseTest {

    @Test
    public void parseByte16() {
        byte expect = (byte) 0xA8;
        byte actual = Byte.parseByte("A8", 16);
        assertEquals(expect, actual);
    }

    @Test
    public void parseByte16ByIntegerGT0() {
        byte expect = (byte) 0xA8;
        byte actual = (byte) Integer.parseInt("A8", 16);
        assertEquals(expect, actual);
    }

    @Test
    public void parseByte16ByIntegerLT0() {
        byte expect = (byte) 0x20;
        byte actual = (byte) Integer.parseInt("20", 16);
        assertEquals(expect, actual);
    }

    @Test
    public void parseInteger16() {
        int expect = 0x64FF04;
        int actual = Integer.parseInt("64FF04", 16);
        assertEquals(expect, actual);
    }

    @Test
    public void longToObject() {
        Object o = -1L;
        long l = (long) o;
        System.out.println("l = " + l);
        assertEquals(true, o instanceof Long);
    }
}
