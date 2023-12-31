package com.android.fishbreadtycoon;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.security.Provider;

public class MusicService extends Service {
    MediaPlayer mp;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mp = MediaPlayer.create(this, R.raw.about_that_oldie);
        mp.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mp.start();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.stop();
        mp.release();
    }
}
