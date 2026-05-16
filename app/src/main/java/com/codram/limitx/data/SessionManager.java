package com.codram.limitx.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "LimiTxSession";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_KEEP_LOGGED_IN = "keep_logged_in";
    private static final String KEY_SORT_ORDER = "sort_order";
    private static final String KEY_SUBSCRIPTION_ACTIVE = "subscription_active";
    private static final String KEY_PHONE_NUMBER = "phone_number";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void savePhoneNumber(String phoneNumber) {
        prefs.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply();
    }

    public String getPhoneNumber() {
        return prefs.getString(KEY_PHONE_NUMBER, "");
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
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

    public void saveSortOrder(String order) {
        prefs.edit().putString(KEY_SORT_ORDER, order).apply();
    }

    public String getSortOrder() {
        return prefs.getString(KEY_SORT_ORDER, "saldo"); // "saldo" is default
    }

    public void setSubscriptionActive(boolean isActive) {
        prefs.edit().putBoolean(KEY_SUBSCRIPTION_ACTIVE, isActive).apply();
    }

    public boolean isSubscriptionActive() {
        return prefs.getBoolean(KEY_SUBSCRIPTION_ACTIVE, true); // Default to true or handle carefully
    }
}
