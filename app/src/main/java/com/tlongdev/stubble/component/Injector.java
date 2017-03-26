package com.tlongdev.stubble.component;

import com.tlongdev.stubble.module.AppModule;
import com.tlongdev.stubble.module.SteamModule;
import com.tlongdev.stubble.presentation.ui.activity.LoginActivity;
import com.tlongdev.stubble.presentation.ui.activity.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * @author lngtr
 * @since 3/23/2017
 */
@Singleton
@Component(modules = {AppModule.class, SteamModule.class})
public interface Injector {
    void inject(LoginActivity loginActivity);

    void inject(MainActivity mainActivity);
}
