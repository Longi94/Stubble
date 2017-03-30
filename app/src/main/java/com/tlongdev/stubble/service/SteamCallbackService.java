package com.tlongdev.stubble.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.tlongdev.stubble.R;
import com.tlongdev.stubble.StubbleApplication;
import com.tlongdev.stubble.service.callback.SteamConnectionCallback;
import com.tlongdev.stubble.service.callback.SteamLogonCallback;
import com.tlongdev.stubble.steam.SentryManager;
import com.tlongdev.stubble.steam.SteamConnection;
import com.tlongdev.stubble.steam.SteamCredentials;
import com.tlongdev.stubble.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import uk.co.thomasc.steamkit.base.generated.steamlanguage.EChatEntryType;
import uk.co.thomasc.steamkit.base.generated.steamlanguage.EResult;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgEchoCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamfriends.callbacks.FriendMsgHistoryCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamnotifications.callbacks.NotificationOfflineMsgCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoggedOnCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.LoginKeyCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.callbacks.UpdateMachineAuthCallback;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.MachineAuthDetails;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.CallbackMsg;
import uk.co.thomasc.steamkit.steam3.steamclient.callbackmgr.JobCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.ConnectedCallback;
import uk.co.thomasc.steamkit.steam3.steamclient.callbacks.DisconnectedCallback;
import uk.co.thomasc.steamkit.util.cSharp.events.ActionT;

public class SteamCallbackService extends Service {

    private static final String LOG_TAG = SteamCallbackService.class.getSimpleName();

    public static final int SERVICE_NOTIFICATION_ID = 100;

    public static SteamConnectionCallback connectionCallback;
    public static SteamLogonCallback logonCallback;

    public static boolean running = false;

    @Inject SteamConnection mSteamConnection;
    @Inject SentryManager mSentryManager;

    private Timer mCallbackTimer;

    private boolean mTimerRunning = false;

    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        ((StubbleApplication) getApplication()).getInjector().inject(this);

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

        message.handle(JobCallback.class, new ActionT<JobCallback>() {
            @Override
            public void call(JobCallback callback) {
                if (callback.getCallbackType() == UpdateMachineAuthCallback.class) {
                    UpdateMachineAuthCallback authCallback =
                            (UpdateMachineAuthCallback) callback.getCallback();

                    try {
                        Log.i(LOG_TAG, "Received sentry file: " + authCallback.getFileName());

                        FileOutputStream fos = openFileOutput(authCallback.getFileName(),
                                Context.MODE_PRIVATE);
                        fos.write(authCallback.getData());
                        fos.close();

                        mSentryManager.saveSentryFileName(
                                mSteamConnection.getLogOnDetails().username,
                                authCallback.getFileName()
                        );

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    MachineAuthDetails auth = new MachineAuthDetails();
                    auth.jobId = callback.getJobId().getValue();
                    auth.fileName = authCallback.getFileName();
                    auth.bytesWritten = authCallback.getBytesToWrite();
                    auth.fileSize = authCallback.getData().length;
                    auth.offset = authCallback.getOffset();
                    auth.result = EResult.OK;
                    auth.lastError = 0;
                    auth.oneTimePassword = authCallback.getOneTimePassword();
                    auth.sentryFileHash = Util.sha1(authCallback.getData());

                    mSteamConnection.sendMachineAuthResponse(auth);
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
                Log.d(LOG_TAG, "Got loginkey " + callback.getLoginKey() + "| uniqueid: " + callback.getUniqueId());

                LogOnDetails logOnDetails = mSteamConnection.getLogOnDetails();
                if (logOnDetails.shouldRememberPassword) {
                    Log.d(LOG_TAG, "Saving login credentials");

                    SteamCredentials credentials = new SteamCredentials();
                    credentials.setUsername(logOnDetails.username);
                    credentials.setLoginKey(callback.getLoginKey());
                    credentials.setUniqueId(callback.getUniqueId());
                    SteamCredentials.saveCredentials(getApplicationContext(), credentials);
                }
            }
        });

        message.handle(FriendMsgCallback.class, new ActionT<FriendMsgCallback>() {
            @Override
            public void call(FriendMsgCallback callback) {
                final EChatEntryType type = callback.getEntryType();

                if (!callback.getSender().equals(mSteamConnection.getSteamClient().getSteamId())) {
                    if (type == EChatEntryType.ChatMsg) {

                    }
                }
            }
        });

        // echoed message from another instance
        message.handle(FriendMsgEchoCallback.class, new ActionT<FriendMsgEchoCallback>() {
            @Override
            public void call(FriendMsgEchoCallback callback) {
                // we log it:
                if (callback.getEntryType() == EChatEntryType.ChatMsg) {
                    /*chatManager.broadcastMessage(
                            System.currentTimeMillis(),
                            steamClient.getSteamId(),
                            callback.getRecipient(),
                            true,
                            SteamChatManager.CHAT_TYPE_CHAT,
                            callback.getMessage()
                    );*/
                }
            }
        });

        message.handle(FriendMsgHistoryCallback.class, new ActionT<FriendMsgHistoryCallback>() {
            @Override
            public void call(FriendMsgHistoryCallback callback) {
                // add all messages that are "unread" to our internal database
                // problem though... Steam send us *all messages* as unread since we last
                // requested history... perhaps we should request history
                // when we get a message. that way we confirm we read the message
                // SOLUTION: record the time that we log in. If this time is after that, ignore it
                if (callback.getSuccess() > 0) {
                    /*SteamID otherId = callback.getSteamId();
                    SteamID ourId = steamClient.getSteamId();
                    for (FriendMsg msg : callback.getMessages()) {
                        if (!msg.isUnread())
                            continue;
                        if ((msg.getTimestamp() * 1000L) > timeLogin)
                            continue;
                        boolean sent_by_us = !msg.getSender().equals(otherId);
                        // potentially check for if it's been read already
                        chatManager.broadcastMessage(
                                msg.getTimestamp() * 1000, // seconds --> millis
                                ourId,
                                otherId,
                                sent_by_us,
                                SteamChatManager.CHAT_TYPE_CHAT,
                                msg.getMessage()
                        );
                    }*/
                }
            }
        });

        message.handle(NotificationOfflineMsgCallback.class, new ActionT<NotificationOfflineMsgCallback>() {
            @Override
            public void call(NotificationOfflineMsgCallback callback) {
                Log.d("SteamService", "Notification offline msg: " + callback.getOfflineMessages());

                //chatManager.unreadMessages.addAll(callback.getFriendsWithOfflineMessages());
            }
        });
    }

    private class CheckCallbacksTask extends TimerTask {
        @Override
        public void run() {
            if (mSteamConnection == null) {
                return;
            }
            while (true) {
                final CallbackMsg msg = mSteamConnection.getSteamClient().getCallback(true);
                if (msg == null) {
                    break;
                }
                mSteamConnection.handleCallback(msg);
                handleSteamMessage(msg);
            }
        }
    }
}
