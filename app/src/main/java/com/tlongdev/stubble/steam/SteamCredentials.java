package com.tlongdev.stubble.steam;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

/**
 * @author lngtr
 * @since 3/6/2017
 */
public class SteamCredentials {

    private static final String PREF_KEY = "steam_credentials";

    private String username;

    private String loginKey;

    private int uniqueId;

    public static void saveCredentials(Context context, SteamCredentials credentials) {
        Gson gson = new Gson();
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(PREF_KEY, gson.toJson(credentials))
                .apply();
    }

    public static SteamCredentials getCredentials(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(PREF_KEY, null);
        return gson.fromJson(json, SteamCredentials.class);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLoginKey() {
        return loginKey;
    }

    public void setLoginKey(String loginKey) {
        this.loginKey = loginKey;
    }

    public int getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(int uniqueId) {
        this.uniqueId = uniqueId;
    }

    public static boolean areCredentialsSaved(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).contains(PREF_KEY);
    }
}
