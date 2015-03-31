package com.daverickdunn.campusmapv2.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CampusMapSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static CampusMapSyncAdapter sCampusMapSyncAdapter = null;

    @Override
    public void onCreate() {

        synchronized (sSyncAdapterLock) {
            if (sCampusMapSyncAdapter == null) {
                sCampusMapSyncAdapter = new CampusMapSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sCampusMapSyncAdapter.getSyncAdapterBinder();
    }
}