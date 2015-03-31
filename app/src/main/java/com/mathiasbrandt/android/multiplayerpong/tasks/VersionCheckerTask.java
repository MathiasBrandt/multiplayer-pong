package com.mathiasbrandt.android.multiplayerpong.tasks;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
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
    private final String VERSION_CHECK_URL = "http://mathiasbrandt.com/apps/multiplayer-pong/version.json";
    private final String VERSION_KEY = "app-version";

    private MainActivity context;

    public VersionCheckerTask(MainActivity context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, VERSION_CHECK_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String newestVersion = response.getString(VERSION_KEY);
                    String actualVersion = context.getString(R.string.app_version);

                    if(!newestVersion.equals(actualVersion)) {
                        context.showErrorDialog(R.string.error_app_version);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "JSON Exception: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Error: could not perform version check (" + error.getMessage() + ")");
            }
        });

        queue.add(request);

        return null;
    }
}
