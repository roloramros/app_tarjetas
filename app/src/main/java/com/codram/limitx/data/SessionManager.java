package com.codram.limitx.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "LimiTxSession";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_KEEP_LOGGED_IN = "keep_logged_in";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void setKeepLoggedIn(boolean keep) {
        prefs.edit().putBoolean(KEY_KEEP_LOGGED_IN, keep).apply();
    }

    public boolean isKeepLoggedIn() {
        return prefs.getBoolean(KEY_KEEP_LOGGED_IN, false);
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}
