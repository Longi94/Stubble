package com.tlongdev.stubble.module;

import com.tlongdev.stubble.steam.SteamConnection;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author lngtr
 * @since 3/23/2017
 */
@Module
public class SteamModule {

    @Provides
    @Singleton
    public SteamConnection provideSteamConnection() {
        return SteamConnection.getInstance();
    }

}
