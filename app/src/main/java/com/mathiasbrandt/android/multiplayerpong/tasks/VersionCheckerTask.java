package com.mathiasbrandt.android.multiplayerpong.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mathiasbrandt.android.multiplayerpong.MainActivity;
import com.mathiasbrandt.android.multiplayerpong.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brandt on 01/04/15.
 */
public class VersionCheckerTask extends AsyncTask<Void, Void, Boolean> {
    private final String TAG = "VersionCheckerTask";


    private MainActivity context;

    public VersionCheckerTask(MainActivity context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {


        return null;
    }
}
