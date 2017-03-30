package com.tlongdev.stubble.presentation.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tlongdev.stubble.R;
import com.tlongdev.stubble.steam.SteamConnection;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class MainActivity extends StubbleActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Inject SteamConnection mSteamConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mApplication.getInjector().inject(this);
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart: ");
        super.onStart();
        mSteamConnection.getFriends();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sign_out:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
