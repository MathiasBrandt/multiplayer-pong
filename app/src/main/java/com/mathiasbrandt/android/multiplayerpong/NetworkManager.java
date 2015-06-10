package com.mathiasbrandt.android.multiplayerpong;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.gson.Gson;
import com.mathiasbrandt.android.multiplayerpong.models.GameState;
import com.mathiasbrandt.android.multiplayerpong.tasks.VersionCheckerTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by brandt on 10/06/15.
 */
public class NetworkManager {
    private final String TAG = "NetworkManager";
    private final String VERSION_CHECK_URL = "http://mathiasbrandt.com/apps/multiplayer-pong/version.json";
    private final String VERSION_KEY = "app-version";

    private Context context;
    private GoogleApiClient googleApiClient;

    public NetworkManager(Context context, GoogleApiClient googleApiClient) {
        this.context = context;
        this.googleApiClient = googleApiClient;
    }

    public void sendGameState(String gameState) {
//        byte[] gameStateBytes = gameState.getBytes();
//        Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, gameStateBytes, room.getRoomId(), opponent.getParticipantId());
    }

    public void receiveGameState(String json) {
//        Gson gson = new Gson();
//        GameState gameState = gson.fromJson(json, GameState.class);
//        mGameFragment.receiveGameState(gameState);
    }

    public boolean isDeviceOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void performVersionCheckAsync() {
        Log.d(TAG, "Performing version check.");
//        new VersionCheckerTask(this).execute();

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, VERSION_CHECK_URL, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String newestVersion = response.getString(VERSION_KEY);
                    String actualVersion = context.getString(R.string.app_version);

                    if(!newestVersion.equals(actualVersion)) {
                        versionCheckFailed();
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
    }

    private void versionCheckFailed() {
        Common.showErrorDialog(context, R.string.error_app_version);
    }

    public int checkGooglePlayServicesAvailability() {
        Log.d(TAG, "Checking for Google Play Services availability.");
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
    }

    public boolean isSignedIn() {
        return googleApiClient != null && googleApiClient.isConnected();
    }
}
