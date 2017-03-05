package com.tlongdev.stubble.service.callback;

/**
 * @author lngtr
 * @since 2/23/2017
 */

public interface SteamLogonCallback {
    void onLogonSuccessful();

    void onLogonInvalidPassword();

    void onLogonSteamGuardRequired(boolean twoFactor);

    void onLogonSteamGuardFailed(boolean twoFactor);
}
