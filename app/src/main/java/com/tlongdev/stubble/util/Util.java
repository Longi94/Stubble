package com.tlongdev.stubble.util;

import android.content.Context;
import android.provider.Settings;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author lngtr
 * @since 2/23/2017
 */
public class Util {
    public static String getDeviceId(Context context) {
        // TODO: 2/23/2017 nono
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static byte[] sha1(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
