package com.tlongdev.stubble.service.callback;

/**
 * @author lngtr
 * @since 2/16/2017
 */

public interface SteamConnectionCallback {
    void onConnectedToSteam();

    void onConnectionFailed();

    void onDisconnected();
}
