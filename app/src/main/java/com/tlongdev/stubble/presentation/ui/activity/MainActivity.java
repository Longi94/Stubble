package com.tlongdev.stubble.presentation.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import com.tlongdev.stubble.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends StubbleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.login)
    public void login() {
        startActivity(new Intent(this, LoginActivity.class));
    }
}
