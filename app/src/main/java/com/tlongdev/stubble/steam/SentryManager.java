package com.tlongdev.stubble.steam;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tlongdev.stubble.component.Injector;

import java.util.Map;

import javax.inject.Inject;

/**
 * @author lngtr
 * @since 3/30/2017
 */
public class SentryManager {

    private static final String LOG_TAG = SentryManager.class.getSimpleName();

    private static final String KEY = "sentry_names";

    @Inject SharedPreferences prefs;
    @Inject SharedPreferences.Editor editor;
    @Inject Gson gson;

    public SentryManager(Injector injector) {
        injector.inject(this);
    }

    public void saveSentryFileName(String username, String sentryName) {
        Log.i(LOG_TAG, String.format("Saving sentry file name %s, username %s", sentryName, username));
        String json = prefs.getString(KEY, "{}");
        Map<String, String> map = gson.fromJson(json,
                new TypeToken<Map<String, String>>(){}.getType());

        map.put(username, sentryName);

        editor.putString(KEY, gson.toJson(map)).apply();

        Log.i(LOG_TAG, String.format("Saved sentry file name %s, username %s", sentryName, username));
    }

    public String getSentryFileName(String username) {
        String json = prefs.getString(KEY, "{}");
        Map<String, String> map = gson.fromJson(json,
                new TypeToken<Map<String, String>>(){}.getType());

        Log.i(LOG_TAG, String.format("Got sentry file name %s, username %s", map.get(username), username));
        return map.get(username);
    }
}
