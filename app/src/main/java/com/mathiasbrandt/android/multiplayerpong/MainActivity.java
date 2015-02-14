package com.mathiasbrandt.android.multiplayerpong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.example.games.basegameutils.BaseGameUtils;


public class MainActivity
        extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String TAG = "MainActivity";
    private GoogleApiClient googleApiClient;
    private boolean isResolvingConnectionFailure = false;
    private static int RESOLVE_CONNECTION_ERROR = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();

        // register callback for Google+ Sign-in Button, since it does not work in XML
        SignInButton btnSignIn = (SignInButton) findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSignInClicked(v);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to Google Play Games");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection to Google Play Games suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, String.format("Connection to Google Play Games failed (%s): %s", connectionResult.getErrorCode(), connectionResult.toString()));

        if(isResolvingConnectionFailure) { return; }

        // start sign-in flow
        isResolvingConnectionFailure = true;
        if(BaseGameUtils.resolveConnectionFailure(this,
                                                googleApiClient,
                                                connectionResult,
                                                RESOLVE_CONNECTION_ERROR,
                                                getString(R.string.sign_in_fallback_error))) {
            isResolvingConnectionFailure = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RESOLVE_CONNECTION_ERROR) {
            isResolvingConnectionFailure = false;

            if(resultCode == RESULT_OK) {
                googleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.sign_in_failed);
            }
        }

        if(resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            Log.d(TAG, "Reconnect required");
        }
    }

    public void btnSignInClicked(View v) {
        if(googleApiClient.isConnected()) {
            Log.d(TAG, "Already connected to Google Play Games");
        } else {
            googleApiClient.connect();
        }
    }

    public void btnSignOutClicked(View v) {
        if(googleApiClient != null && googleApiClient.isConnected()) {
            Log.d(TAG, "Disconnecting from Google Play Games.");
            Games.signOut(googleApiClient);
            googleApiClient.disconnect();
        } else {
            Toast.makeText(this, R.string.already_signed_out, Toast.LENGTH_SHORT).show();
        }
    }
}