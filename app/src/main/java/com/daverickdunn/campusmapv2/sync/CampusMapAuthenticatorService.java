package com.daverickdunn.campusmapv2.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class CampusMapAuthenticatorService extends Service {

    private CampusMapAuthenticator mAuthenticator;

    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new CampusMapAuthenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}