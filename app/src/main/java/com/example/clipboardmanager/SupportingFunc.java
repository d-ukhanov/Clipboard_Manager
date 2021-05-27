package com.example.clipboardmanager;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;

public class SupportingFunc {
    public static String getFormatTime(Context context, Date date) {
        @SuppressLint("SimpleDateFormat")
        final SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.format_time));
        return dateFormat.format(date);
    }

    public static String getFormatDate(Context context, Date date) {
        @SuppressLint("SimpleDateFormat")
        final SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.format_date));
        return dateFormat.format(date);
    }

    public static String divideString(String string, int length) {
        string = string.trim();
        return  (string.length() > length) ? string.substring(0, length - 2).trim() + "…" : string.trim();
    }
}
