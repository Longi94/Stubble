package com.tlongdev.stubble.module;

import android.app.Application;

import com.tlongdev.stubble.StubbleApplication;
import com.tlongdev.stubble.steam.SentryManager;
import com.tlongdev.stubble.steam.SteamConnection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;

/**
 * @author lngtr
 * @since 3/23/2017
 */
@Module
public class SteamModule {

    @Provides
    @Singleton
    SteamConnection provideSteamConnection(Application application) {
        return SteamConnection.getInstance(((StubbleApplication) application).getInjector());
    }

    @Provides
    @Singleton
    SentryManager sentryManager(Application application) {
        return new SentryManager(((StubbleApplication) application).getInjector());
    }

    @Provides
    @Singleton
    SteamClient steamClient() {
        return new SteamClient();
    }
}
