package com.tlongdev.stubble;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.tlongdev.stubble.presentation.module.AppModule;

import uk.co.thomasc.steamkit.util.logging.DebugLog;
import uk.co.thomasc.steamkit.util.logging.IDebugListener;

/**
 * @author lngtr
 * @since 2016. 08. 22.
 */
public class StubbleApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        AppModule appModule = new AppModule(this);

        DebugLog.addListener(new IDebugListener() {
            @Override
            public void writeLine(String category, String message) {
                Log.d(category, message);
            }
        });
    }

    public static Context getAppContext() {
        return context;
    }
}
