package com.tlongdev.stubble.presentation.ui.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.Toast;

import com.tlongdev.stubble.R;
import com.tlongdev.stubble.service.SteamCallbackService;
import com.tlongdev.stubble.service.callback.SteamConnectionCallback;
import com.tlongdev.stubble.service.callback.SteamLogonCallback;
import com.tlongdev.stubble.steam.SteamConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.thomasc.steamkit.steam3.handlers.steamuser.types.LogOnDetails;

public class LoginActivity extends StubbleActivity implements SteamConnectionCallback, SteamLogonCallback {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.input_username) EditText mInputUsername;
    @BindView(R.id.input_password) EditText mInputPassword;

    private ProgressDialog mProgressDialog;

    private String mGuardCode = null;
    private boolean mTwoFactor = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        SteamCallbackService.connectionCallback = this;
        SteamCallbackService.logonCallback = this;
    }

    @Override
    protected void onStop() {
        super.onStop();
        SteamCallbackService.connectionCallback = this;
        SteamCallbackService.logonCallback = this;
    }

    @OnClick(R.id.login)
    public void login() {
        startService(new Intent(this, SteamCallbackService.class));
        if (!SteamConnection.getInstance().isConnected()) {
            new ConnectTask().execute();

            mProgressDialog = ProgressDialog.show(this, null, "Connecting to steam...", true, false);
        } else {
            mProgressDialog = ProgressDialog.show(this, null, "Loggin in...", true, false);
            attemptLogon();
        }
    }

    @OnClick(R.id.disconnect)
    public void disconnect() {
        if (SteamConnection.getInstance().isConnected()) {
            new DisconnectTask().execute();
            mProgressDialog = ProgressDialog.show(this, null, "Disconnecting to steam...", true, false);
        }
    }

    @Override
    public void onConnectedToSteam() {
        Toast.makeText(this, "Connected to steam", Toast.LENGTH_SHORT).show();
        attemptLogon();
        if (mProgressDialog != null) {
            mProgressDialog.setMessage("Loggin in...");
        }
    }

    private void attemptLogon() {
        LogOnDetails details = new LogOnDetails()
                .username(mInputUsername.getText().toString())
                .password(mInputPassword.getText().toString());

        if (mGuardCode != null) {
            if (mTwoFactor) {
                details.twoFactorCode(mGuardCode);
            } else {
                details.authCode(mGuardCode);
            }
        }
        new LogonTask().execute(details);
    }

    @Override
    public void onConnectionFailed() {
        Toast.makeText(this, "Disconnected from steam", Toast.LENGTH_SHORT).show();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onDisconnected() {
        Toast.makeText(this, "Disconnected from steam", Toast.LENGTH_SHORT).show();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onLogonSuccessful() {
        Toast.makeText(this, "Login succesful", Toast.LENGTH_SHORT).show();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onLogonInvalidPassword() {
        Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    @Override
    public void onLogonSteamGuardFailed(boolean twoFactor) {

    }

    @Override
    public void onLogonSteamGuardRequired(boolean twoFactor) {

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
            SteamConnection.getInstance().connect();
            return null;
        }
    }

    private class DisconnectTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            SteamConnection.getInstance().disconnect();
            return null;
        }
    }

    private class LogonTask extends AsyncTask<LogOnDetails, Void, Void> {
        @Override
        protected Void doInBackground(LogOnDetails... logOnDetails) {
            SteamConnection.getInstance().logon(logOnDetails[0]);
            return null;
        }
    }
}
