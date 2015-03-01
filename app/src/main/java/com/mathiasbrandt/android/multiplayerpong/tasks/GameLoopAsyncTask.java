package com.mathiasbrandt.android.multiplayerpong.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.mathiasbrandt.android.multiplayerpong.models.PongBall;

/**
 * Created by brandt on 01/03/15.
 */
public class GameLoopAsyncTask extends AsyncTask<Void, Void, Void> {
    private final String TAG = "GameLoop";
    private static final int SLEEP_PERIOD = 10;
    private Context context;

    public GameLoopAsyncTask() {
    }

    @Override
    protected Void doInBackground(Void... params) {
        while(!isCancelled()) {
            try {
                Thread.sleep(SLEEP_PERIOD);
            } catch (InterruptedException e) {
                Log.d(TAG, "Sleep interrupted");
            }

            publishProgress();
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        PongBall.getInstance(context).move();
    }
}
