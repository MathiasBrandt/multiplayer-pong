package com.mathiasbrandt.android.multiplayerpong;

import android.app.Activity;
import android.app.Fragment;
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
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameUtils;


public class MainActivity
        extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
            MainMenuFragment.FragmentListener {

    // tag for debug logging
    private final boolean DEBUG = true;
    private final String TAG = "MainActivity";

    // request codes
    private static final int RC_SIGN_IN = 9001;

    // fragments
    MainMenuFragment mMainMenuFragment;

    // client used to interact with Google APIs
    private GoogleApiClient googleApiClient;

    // are we currently resolving a connection failure?
    private boolean isResolvingConnectionFailure = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // create the Google API client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();

        // create fragments
        mMainMenuFragment = new MainMenuFragment();

        // add initial fragment
        getFragmentManager().beginTransaction().add(R.id.fragment_container, mMainMenuFragment).commit();
    }

    private void switchToFragment(Fragment newFragment) {
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, newFragment).commit();
    }

    private boolean isSignedIn() {
        return googleApiClient != null && googleApiClient.isConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart(): Connecting to Google Play Games.");
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop(): Disconnecting from Google Play Games.");
        if(isSignedIn()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connected to Google Play Games");

        // set player info in main menu fragment
        Player player = Games.Players.getCurrentPlayer(googleApiClient);
        mMainMenuFragment.setPlayer(player);

        // hide sign-in button
        SignInButton signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
        signInButton.setVisibility(View.GONE);
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
                RC_SIGN_IN,
                                                getString(R.string.sign_in_fallback_error))) {
            isResolvingConnectionFailure = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_SIGN_IN) {
            isResolvingConnectionFailure = false;

            if(resultCode == RESULT_OK) {
                googleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.sign_in_failed);

                // show sign-in button
                SignInButton signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
                signInButton.setVisibility(View.VISIBLE);
            }
        }

        if(resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            Log.d(TAG, "Reconnect required");
        }
    }

    @Override
    public void btnAchievementsClicked() {

    }

    @Override
    public void btnLeaderboardsClicked() {

    }

    @Override
    public void btnSignInClicked() {
        if(googleApiClient.isConnected()) {
            Log.d(TAG, "Already connected to Google Play Games.");
        } else {
            Log.d(TAG, "btnSignIn: Connecting to Google Play Games.");
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