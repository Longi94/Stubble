package com.tlongdev.stubble.presentation.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.tlongdev.stubble.R;
import com.tlongdev.stubble.service.SteamCallbackService;
import com.tlongdev.stubble.service.callback.SteamConnectionCallback;
import com.tlongdev.stubble.service.callback.SteamLogonCallback;
import com.tlongdev.stubble.steam.SentryManager;
import com.tlongdev.stubble.steam.SteamConnection;
import com.tlongdev.stubble.steam.SteamCredentials;
import com.tlongdev.stubble.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;

public class LoginActivity extends StubbleActivity implements SteamLogonCallback,
        SteamConnectionCallback {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    @Inject SteamConnection mSteamConnection;
    @Inject SentryManager mSentryManager;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.input_username) EditText mInputUsername;
    @BindView(R.id.input_password) EditText mInputPassword;
    @BindView(R.id.input_remember) CheckBox mInputRemember;

    private ProgressDialog mProgressDialog;

    private String mGuardCode = null;
    private boolean mTwoFactor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mApplication.getInjector().inject(this);

        setSupportActionBar(mToolbar);
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart: ");
        super.onStart();
        SteamCallbackService.logonCallback = this;
        SteamCallbackService.connectionCallback = this;
        startService(new Intent(this, SteamCallbackService.class));
        if (SteamCredentials.areCredentialsSaved(this)) {
            if (mSteamConnection.isConnected()) {
                if (!mSteamConnection.isLoggedOn()) {
                    attemptLogonWithCredentials();
                    mProgressDialog = ProgressDialog.show(this, null, "Loggin in...", true, false);
                } else {
                    nextActivity();
                }
            } else {
                new ConnectTask().execute();
                mProgressDialog = ProgressDialog.show(this, null, "Connecting to steam...", true, false);
            }
        } else {
            if (mSteamConnection.isConnected()) {
                if (mSteamConnection.isLoggedOn()) {
                    nextActivity();
                }
            }
        }
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop: ");
        super.onStop();
        SteamCallbackService.logonCallback = null;
        SteamCallbackService.connectionCallback = null;
    }

    @OnClick(R.id.login)
    public void login() {
        Log.d(LOG_TAG, "login: ");
        startService(new Intent(this, SteamCallbackService.class));
        if (!mSteamConnection.isConnected()) {
            new ConnectTask().execute();
            mProgressDialog = ProgressDialog.show(this, null, "Connecting to steam...", true, false);
        } else {
            mProgressDialog = ProgressDialog.show(this, null, "Logging in...", true, false);
            attemptLogon();
        }
    }

    /*@OnClick(R.id.disconnect)
    public void disconnect() {
        Log.d(LOG_TAG, "disconnect: ");
        if (mSteamConnection.isConnected()) {
            new DisconnectTask().execute();
            mProgressDialog = ProgressDialog.show(this, null, "Disconnecting to steam...", true, false);
        }
    }*/

    private void nextActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void attemptLogon() {
        LogOnDetails details = new LogOnDetails()
                .username(mInputUsername.getText().toString())
                .password(mInputPassword.getText().toString());

        details.shouldRememberPassword = mInputRemember.isChecked();

        if (mGuardCode != null) {
            if (mTwoFactor) {
                details.twoFactorCode(mGuardCode);
            } else {
                details.authCode(mGuardCode);
            }
        }
        addSentryHash(details);
        mSteamConnection.setLogOnDetails(details);
        new LogonTask().execute(details);
    }

    private void attemptLogonWithCredentials() {
        SteamCredentials credentials = SteamCredentials.getCredentials(this);
        LogOnDetails details = new LogOnDetails().username(credentials.getUsername());

        details.shouldRememberPassword = true;
        details.loginkey = credentials.getLoginKey();

        addSentryHash(details);
        mSteamConnection.setLogOnDetails(details);
        new LogonTask().execute(details);
    }

    private void addSentryHash(LogOnDetails logOnDetails) {
        String sentryName = mSentryManager.getSentryFileName(logOnDetails.username);

        if (sentryName == null) {
            return;
        }

        try {
            File file = new File(getFilesDir(), sentryName);
            RandomAccessFile f = new RandomAccessFile(file, "r");
            byte[] bytes = new byte[(int) f.length()];
            f.readFully(bytes);
            f.close();

            logOnDetails.sentryFileHash = Util.sha1(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectedToSteam() {
        Log.d(LOG_TAG, "onConnectedToSteam: ");
        if (mProgressDialog != null) {
            mProgressDialog.setMessage("Logging in...");
        }
        if (SteamCredentials.areCredentialsSaved(this)) {
            attemptLogonWithCredentials();
        } else {
            attemptLogon();
        }
    }

    @Override
    public void onConnectionFailed() {
        Log.d(LOG_TAG, "onConnectionFailed: ");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        /*new AlertDialog.Builder(this)
                .setMessage("Failed to connect")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();*/
    }

    @Override
    public void onDisconnected() {
        Log.d(LOG_TAG, "onDisconnected: ");
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        /*new AlertDialog.Builder(this)
                .setMessage("Failed to connect")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();*/
    }

    @Override
    public void onLogonSuccessful() {
        Log.d(LOG_TAG, "onLogonSuccessful: ");
        Toast.makeText(this, "Login succesful", Toast.LENGTH_SHORT).show();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        nextActivity();
    }

    @Override
    public void onLogonInvalidPassword() {
        Log.d(LOG_TAG, "onLogonInvalidPassword: ");
        Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onLogonSteamGuardFailed(boolean twoFactor) {
        Log.d(LOG_TAG, "onLogonSteamGuardFailed: " + twoFactor);
    }

    @Override
    public void onLogonSteamGuardRequired(boolean twoFactor) {
        Log.d(LOG_TAG, "onLogonSteamGuardRequired: " + twoFactor);

        mTwoFactor = twoFactor;

        new DisconnectTask().execute();

        final EditText edittext = new EditText(this);
        AlertDialog.Builder alert = new AlertDialog.Builder(this)
                .setTitle("Steam Guard")
                .setView(edittext)
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        mProgressDialog = ProgressDialog.show(LoginActivity.this, null, "Connecting to steam...", true, false);
                        mGuardCode = edittext.getText().toString();

                        startService(new Intent(LoginActivity.this, SteamCallbackService.class));
                        new ConnectTask().execute();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // what ever you want to do with No option.
                    }
                });

        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        alert.show();
    }

    private class ConnectTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (!SteamCallbackService.running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mSteamConnection.connect();
            return null;
        }
    }

    private class DisconnectTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            mSteamConnection.disconnect();
            return null;
        }
    }

    private class LogonTask extends AsyncTask<LogOnDetails, Void, Void> {
        @Override
        protected Void doInBackground(LogOnDetails... logOnDetails) {
            mSteamConnection.logon(logOnDetails[0]);
            return null;
        }
    }
}
