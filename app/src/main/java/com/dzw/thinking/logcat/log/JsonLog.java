package com.dzw.thinking.logcat.log;

import android.util.Log;


import com.dzw.thinking.logcat.LogDzw;
import com.dzw.thinking.logcat.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonLog {

    public static void printJson(String tag, String msg, String headString) {

        String message;

        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(LogDzw.JSON_INDENT);
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(LogDzw.JSON_INDENT);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }

        LogUtil.printLine(tag, true);
        message = headString + LogDzw.LINE_SEPARATOR + message;
        String[] lines = message.split(LogDzw.LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "║ " + line);
        }
        LogUtil.printLine(tag, false);
    }
}
