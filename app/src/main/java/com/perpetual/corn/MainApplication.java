package com.perpetual.corn;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

/**
 * Created by mbarcelona on 1/1/18.
 */

public class MainApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Helper.install(MainApplication.this);
    }
}
