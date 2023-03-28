package com.vcvnc.vpn.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeFormatUtil {
    private static DateFormat HHMMSSSFormat = new SimpleDateFormat("HH:mm:ss:s", Locale.getDefault());
    private static DateFormat formatYYMMDDHHMMSSFormat=new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss:s", Locale.getDefault());
    public static String formatHHMMSSMM(long time) {
        Date date = new Date(time);
        return HHMMSSSFormat.format(date);
    }
    public static String formatYYMMDDHHMMSS(long time) {
        Date date = new Date(time);
        return formatYYMMDDHHMMSSFormat.format(date);
    }
}
