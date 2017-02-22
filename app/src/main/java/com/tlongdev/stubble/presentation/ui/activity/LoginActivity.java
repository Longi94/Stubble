package com.tlongdev.stubble.presentation.ui.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.tlongdev.stubble.R;
import com.tlongdev.stubble.service.SteamCallbackService;
import com.tlongdev.stubble.service.callback.SteamConnectionCallback;
import com.tlongdev.stubble.steam.SteamConnection;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends StubbleActivity implements SteamConnectionCallback {

    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    @BindView(R.id.toolbar) Toolbar mToolbar;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.login)
    public void login() {
        startService(new Intent(this, SteamCallbackService.class));
        if (!SteamConnection.getInstance().isConnected()) {
            SteamCallbackService.connectionCallback = this;

            new ConnectTask().execute();

            mProgressDialog = ProgressDialog.show(this, null, "Connecting to steam...", true, false);
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
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
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
}
