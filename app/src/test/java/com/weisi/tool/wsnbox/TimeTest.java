package com.weisi.tool.wsnbox;

import android.util.Log;

import com.weisi.tool.wsnbox.util.Tag;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2017/12/14.
 */

public class TimeTest {

    @Test
    public void getMillisecondsAt0AMAndNextDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss");
        System.out.println("today: " + dateFormat.format(calendar.getTime()) + ", mills: " + calendar.getTimeInMillis());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        System.out.println("tomorrow: " + dateFormat.format(calendar.getTime()) + ", mills: " + calendar.getTimeInMillis());
        //Log.d(Tag.LOG_TAG_D_TEST, );
    }

    @Test
    public void getIntradayStartTime() {
        Calendar calendar = Calendar.getInstance();
        System.out.println("current time mills: " + calendar.getTimeInMillis());
        long actual = TimeUnit.DAYS.toMillis(TimeUnit.MILLISECONDS.toDays(calendar.getTimeInMillis()));
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long expect = calendar.getTimeInMillis();
        assertEquals(expect, actual);
    }

    @Test
    public void getOneDayMilliseconds() {
        assertEquals(TimeUnit.DAYS.toMillis(1), 86400000);
    }

    @Test
    public void stringFormatTime() {
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(Calendar.HOUR, 5);
//        calendar.set(Calendar.MINUTE, 13);
//        calendar.set(Calendar.SECOND, 14);
//        calendar.set(Calendar.MILLISECOND, 222);
//        String actual = String.format("%tF", calendar.getTime());
        String actual = String.format("%tF", new Date(System.currentTimeMillis()));
        String expect = "2017-12-18";
        assertEquals(expect, actual);
    }

    @Test
    public void testGetPreviousDayTime() {
        String actual = String.format("%tF", new Date(getPreviousDayTime(System.currentTimeMillis())));
        String expect = "2017-12-17";
        assertEquals(expect, actual);
    }

    @Test
    public void testGetNextDayTime() {
        String actual = String.format("%tF", new Date(getNextDayTime(System.currentTimeMillis())));
        String expect = "2017-12-19";
        assertEquals(expect, actual);
    }

    private long getPreviousDayTime(long sourceDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sourceDate);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return calendar.getTimeInMillis();
    }

    private long getNextDayTime(long sourceDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(sourceDate);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }
}
