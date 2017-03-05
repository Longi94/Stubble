package com.tlongdev.stubble.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tlongdev.stubble.R;
import com.tlongdev.stubble.service.callback.SteamConnectionCallback;
import com.tlongdev.stubble.service.callback.SteamLogonCallback;
import com.tlongdev.stubble.steam.SteamConnection;

import java.util.Timer;
import java.util.TimerTask;

import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoginKeyCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.SteamClient;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.util.cSharp.events.ActionT;

public class SteamCallbackService extends Service {

    private static final String LOG_TAG = SteamCallbackService.class.getSimpleName();

    public static final int SERVICE_NOTIFICATION_ID = 100;

    public static SteamConnectionCallback connectionCallback;
    public static SteamLogonCallback logonCallback;

    public static boolean running = false;

    private SteamClient mSteamClient;
    private Timer mCallbackTimer;

    private boolean mTimerRunning = false;

    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mSteamClient = SteamConnection.getInstance().getSteamClient();

        mHandler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (running) {
            Log.v(LOG_TAG, "Service already running...");
            return START_STICKY;
        }

        if (!mTimerRunning) {
            mCallbackTimer = new Timer();
            mCallbackTimer.scheduleAtFixedRate(new CheckCallbacksTask(), 0, 500);
            mTimerRunning = true;
        }

        //Intent notificationIntent = new Intent(this, MainActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Steam Bubble");
        builder.setTicker("Steam Bubble");
        builder.setWhen(System.currentTimeMillis());
        builder.setContentText("Service running...");
        //builder.setContentIntent(pendingIntent);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        startForeground(SERVICE_NOTIFICATION_ID, builder.build());

        Log.v(LOG_TAG, "Service started...");
        running = true;

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);

        if (mCallbackTimer != null) {
            mCallbackTimer.cancel();
        }
        mTimerRunning = false;

        Log.v(LOG_TAG, "Service stopped...");
        running = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleSteamMessage(CallbackMsg message) {
        Log.d(LOG_TAG, "handleSteamMessage: " + message.getClass().getSimpleName());

        message.handle(ConnectedCallback.class, new ActionT<ConnectedCallback>() {
            @Override
            public void call(ConnectedCallback callback) {
                Log.d(LOG_TAG, "Connected to steam, status: " + callback.getResult());
                if (callback.getResult() == EResult.OK) {
                    if (connectionCallback != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                connectionCallback.onConnectedToSteam();
                            }
                        });
                    }
                } else {
                    if (connectionCallback != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                connectionCallback.onConnectionFailed();
                            }
                        });
                    }
                }
            }
        });

        message.handle(DisconnectedCallback.class, new ActionT<DisconnectedCallback>() {
            @Override
            public void call(DisconnectedCallback callback) {
                Log.d(LOG_TAG, "Disconnected from steam network: ");
                if (connectionCallback != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            connectionCallback.onDisconnected();
                        }
                    });
                }
                stopSelf();
            }
        });

        message.handle(LoggedOnCallback.class, new ActionT<LoggedOnCallback>() {
            @Override
            public void call(final LoggedOnCallback callback) {
                if (logonCallback != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            EResult result = callback.getResult();
                            switch (result) {
                                case OK:
                                    logonCallback.onLogonSuccessful();
                                    break;
                                case InvalidPassword:
                                    logonCallback.onLogonInvalidPassword();
                                    break;
                                case AccountLogonDenied:
                                case AccountLogonDeniedNoMail:
                                case AccountLogonDeniedVerifiedEmailRequired:
                                case AccountLoginDeniedNeedTwoFactor:
                                    logonCallback.onLogonSteamGuardRequired(result == EResult.AccountLoginDeniedNeedTwoFactor);
                                    break;
                                case InvalidLoginAuthCode:
                                case TwoFactorCodeMismatch:
                                    logonCallback.onLogonSteamGuardFailed(result == EResult.TwoFactorCodeMismatch);
                                default:
                            }
                        }
                    });
                }
            }
        });

        message.handle(LoginKeyCallback.class, new ActionT<LoginKeyCallback>() {
            @Override
            public void call(LoginKeyCallback callback) {

            }
        });
    }

    private class CheckCallbacksTask extends TimerTask {
        @Override
        public void run() {
            if (mSteamClient == null) {
                return;
            }
            while (true) {
                final CallbackMsg msg = mSteamClient.getCallback(true);
                if (msg == null) {
                    break;
                }
                SteamConnection.getInstance().handleCallback(msg);
                handleSteamMessage(msg);
            }
        }
    }
}
