package com.tlongdev.stubble.presentation.presenter;

import com.tlongdev.stubble.presentation.ui.view.BaseView;

public interface Presenter<V extends BaseView> {
    void attachView(V view);
    void detachView();
}