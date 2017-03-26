package com.tlongdev.stubble;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.tlongdev.stubble.component.DaggerInjector;
import com.tlongdev.stubble.component.Injector;
import com.tlongdev.stubble.module.AppModule;
import com.tlongdev.stubble.module.SteamModule;

import uk.co.thomasc.steamkit.util.logging.DebugLog;
import uk.co.thomasc.steamkit.util.logging.IDebugListener;

/**
 * @author lngtr
 * @since 2016. 08. 22.
 */
public class StubbleApplication extends Application {

    private static Context context;

    private Injector mInjector;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        AppModule appModule = new AppModule(this);
        SteamModule steamModule = new SteamModule();

        mInjector = DaggerInjector.builder()
                .appModule(appModule)
                .steamModule(steamModule)
                .build();

        DebugLog.addListener(new IDebugListener() {
            @Override
            public void writeLine(String category, String message) {
                Log.d(category, message);
            }
        });
    }

    public Injector getInjector() {
        return mInjector;
    }

    public static Context getAppContext() {
        return context;
    }
}
