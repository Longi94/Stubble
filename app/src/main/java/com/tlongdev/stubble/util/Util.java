package com.tlongdev.stubble.util;

import android.content.Context;
import android.provider.Settings;

/**
 * @author lngtr
 * @since 2/23/2017
 */
public class Util {
    public static String getDeviceId(Context context) {
        // TODO: 2/23/2017 nono
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
