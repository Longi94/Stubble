package com.tlongdev.stubble.steam;

import android.util.Log;

import com.tlongdev.stubble.StubbleApplication;
import com.tlongdev.stubble.util.Util;

import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamchat.SteamChat;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.types.steamid.SteamID;
import uk.co.thomasc.steamkit.util.cSharp.events.ActionT;

/**
 * @author lngtr
 * @since 2/17/2017
 */
public class SteamConnection {

    private static final String LOG_TAG = SteamConnection.class.getSimpleName();

    private static SteamConnection ourInstance;

    public static SteamConnection getInstance() {
        if (ourInstance == null) {
            ourInstance = new SteamConnection();
        }
        return ourInstance;
    }

    private SteamClient mSteamClient;

    private SteamUser mSteamUser;
    private SteamFriends mSteamFriends;
    private SteamChat mSteamChat;

    private boolean mConnected = false;
    private boolean mLoggedOn = false;

    private LogOnDetails mLogOnDetails;

    private SteamConnection() {
        mSteamClient = new SteamClient();

        mSteamUser = mSteamClient.getHandler(SteamUser.class);
        mSteamFriends = mSteamClient.getHandler(SteamFriends.class);
        mSteamChat = mSteamClient.getHandler(SteamChat.class);
    }

    public SteamClient getSteamClient() {
        return mSteamClient;
    }

    public void handleCallback(CallbackMsg message) {
        message.handle(ConnectedCallback.class, new ActionT<ConnectedCallback>() {
            @Override
            public void call(ConnectedCallback callback) {
                mConnected = callback.getResult() == EResult.OK;
            }
        });

        message.handle(DisconnectedCallback.class, new ActionT<DisconnectedCallback>() {
            @Override
            public void call(DisconnectedCallback callback) {
                mConnected = false;
                mLoggedOn = false;
            }
        });

        message.handle(LoggedOnCallback.class, new ActionT<LoggedOnCallback>() {
            @Override
            public void call(LoggedOnCallback callback) {
                mLoggedOn = callback.getResult() == EResult.OK;
            }
        });
    }

    public void connect() {
        mSteamClient.connect();
    }

    public void disconnect() {
        mSteamClient.disconnect();
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void logon(LogOnDetails logOnDetails) {
        mSteamUser.logOn(logOnDetails, Util.getDeviceId(StubbleApplication.getAppContext()));
    }

    public boolean isLoggedOn() {
        return mLoggedOn;
    }

    public void setLogOnDetails(LogOnDetails logOnDetails) {
        mLogOnDetails = logOnDetails;
    }

    public LogOnDetails getLogOnDetails() {
        return mLogOnDetails;
    }

    public Object getFriends() {
        for (SteamID steamID : mSteamFriends.getFriendList()) {
            Log.d(LOG_TAG, "steam id: " + steamID.render());
        }
        return null;
    }
}
