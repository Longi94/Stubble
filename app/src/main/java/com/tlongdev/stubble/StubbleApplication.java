package com.tlongdev.stubble;

import android.app.Application;

import com.tlongdev.stubble.presentation.module.AppModule;

/**
 * @author lngtr
 * @since 2016. 08. 22.
 */
public class StubbleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AppModule appModule = new AppModule(this);
    }
}
