package com.devarshukani.clearmindlauncher;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "ClearMindSettings"; // Change this to your preference name

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void saveData(Context context, String key, Object value) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        }

        editor.apply();
    }

    public static Object getData(Context context, String key, Object defaultValue) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        if (defaultValue instanceof String) {
            return sharedPreferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return sharedPreferences.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            return sharedPreferences.getBoolean(key, (Boolean) defaultValue);
        }

        return null;
    }
}