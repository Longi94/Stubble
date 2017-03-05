package com.tlongdev.stubble.steam;

import com.tlongdev.stubble.StubbleApplication;
import com.tlongdev.stubble.util.Util;

import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamchat.SteamChat;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.SteamFriends;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.SteamUser;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
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
            public void call(ConnectedCallback connectedCallback) {
                mConnected = connectedCallback.getResult() == EResult.OK;
            }
        });

        message.handle(DisconnectedCallback.class, new ActionT<DisconnectedCallback>() {
            @Override
            public void call(DisconnectedCallback disconnectedCallback) {
                mConnected = false;
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
}
