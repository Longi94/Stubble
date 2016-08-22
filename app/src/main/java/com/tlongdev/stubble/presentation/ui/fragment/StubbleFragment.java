package com.tlongdev.stubble.presentation.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.tlongdev.stubble.StubbleApplication;

public abstract class StubbleFragment extends Fragment {

    protected StubbleApplication mApplication;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (StubbleApplication) getActivity().getApplication();
    }
}