package com.mathiasbrandt.android.multiplayerpong;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;


public class MainActivity
        extends Activity
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            MainMenuFragment.MainMenuFragmentListener,
            GameFragment.GameFragmentListener
            //OnInvitationReceivedListener
            {

    // tag for debug logging
    private final boolean DEBUG = true;
    private final String TAG = "MainActivity";

    // request codes
    public static final int RC_SIGN_IN = 7001;
    public static final int RC_SELECT_PLAYERS = 7002;
    public static final int RC_WAITING_ROOM = 7003;

    // fragments
    private MainMenuFragment mMainMenuFragment;
    private GameFragment mGameFragment;

    // client used to interact with Google APIs
    private GoogleApiClient googleApiClient;

    // are we currently resolving a connection failure?
    private boolean isResolvingConnectionFailure = false;

    private RoomListener roomListener;
    private Room room;
    private Invitation storedInvitation;

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

        roomListener = new RoomListener(this);

        // create fragments
        mMainMenuFragment = new MainMenuFragment();
        mGameFragment = new GameFragment();

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
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to Google Play Games");

        // set player info in main menu fragment
        Player player = Games.Players.getCurrentPlayer(googleApiClient);
        mMainMenuFragment.setPlayer(player);

        // hide sign-in button
        SignInButton signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
        signInButton.setVisibility(View.GONE);

        // show sign-out button
        Button signOutButton = (Button) findViewById(R.id.btn_sign_out);
        signOutButton.setVisibility(View.VISIBLE);

        // add invitation listener
        //Games.Invitations.registerInvitationListener(googleApiClient, this);

        // check invitations
        if(connectionHint != null) {
            Invitation invitation = connectionHint.getParcelable(Multiplayer.EXTRA_INVITATION);

            if(invitation != null) {
                // accept invitation
                RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(roomListener)
                        .setMessageReceivedListener(roomListener)
                        .setRoomStatusUpdateListener(roomListener);
                roomConfigBuilder.setInvitationIdToAccept(invitation.getInvitationId());
                Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // go to game screen
                Log.d(TAG, "Invitation accepted, go to game screen");
            }
        }
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
        Log.d(TAG, String.format("onAR: req: %d, res: %d", requestCode, resultCode));

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
        } else if(requestCode == RC_SELECT_PLAYERS) {
            if(resultCode == RESULT_OK) {
                // get invitee
                Bundle extras = data.getExtras();

                //for(String id : data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS)) {
                //    Log.d(TAG, id);
                //}

                ArrayList<String> inviteeList = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
                if(inviteeList == null) {
                    Log.d(TAG, "Invitee list is null, handle auto matching!");
                    return;
                }

                RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(roomListener)
                        .setMessageReceivedListener(roomListener)
                        .setRoomStatusUpdateListener(roomListener);

                roomConfigBuilder.addPlayersToInvite(inviteeList);

                RoomConfig roomConfig = roomConfigBuilder.build();
                Games.RealTimeMultiplayer.create(googleApiClient, roomConfig);

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                // user canceled
                return;
            }
        } else if(requestCode == RC_WAITING_ROOM) {
            if(resultCode == RESULT_OK) {
                // all invited players were successfully connected to the room
                // start the game!
                Log.d(TAG, "All players connected - start game!");

                Bundle arguments = new Bundle();
                arguments.putParcelable("room", room);
                mGameFragment.setArguments(arguments);
                switchToFragment(mGameFragment);
            } else if(resultCode == RESULT_CANCELED) {
                // player dismissed the waiting room
                // it should be possible to minimize the waiting room and continue connecting in the background
                // however, for now, just leave the room
                Log.d(TAG, "Player dismissed the waiting room (back button)");

                Games.RealTimeMultiplayer.leave(googleApiClient, roomListener, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else if(resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player selected the leave room option
                Log.d(TAG, "Player left the room");

                Games.RealTimeMultiplayer.leave(googleApiClient, roomListener, room.getRoomId());
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        if(resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            Log.d(TAG, "Reconnect required");
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    /**
     * Callback invoked when a new invitation is received.
     * @param invitation
     */
    /* @Override
    public void onInvitationReceived(Invitation invitation) {
        final String invitationId = invitation.getInvitationId();

        // show in-game popup to let user know of pending invitation
        new AlertDialog.Builder(this)
                .setTitle(R.string.invitation_dialog_title)
                .setMessage(R.string.invitation_dialog_message)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(roomListener)
                                .setMessageReceivedListener(roomListener)
                                .setRoomStatusUpdateListener(roomListener);
                        roomConfigBuilder.setInvitationIdToAccept(invitationId);
                        Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());

                        // prevent screen from sleeping during handshake
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                        // now, go to game screen
                        Bundle arguments = new Bundle();
                        arguments.putParcelable("room", room);
                        mGameFragment.setArguments(arguments);
                        switchToFragment(mGameFragment);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Invitation declined", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();


        // store invitation for use when player accepts this invitation
        //mIncomingInvitationId = invitation.getInvitationId();
    } */

    /**
     * Callback invoked when a previously received invitation has been removed from the local device.
     * @param invitationId
     */
    /* @Override
    public void onInvitationRemoved(String invitationId) {

    } */

    @Override
    public void tempButtonClicked() {
        Player player = Games.Players.getCurrentPlayer(googleApiClient);
        String msg = "why hello there!";
        byte[] message = msg.getBytes();

        for(Participant p : room.getParticipants()) {
            if(p.getParticipantId() != player.getPlayerId()) {
                Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, message, room.getRoomId(), p.getParticipantId());
            }
        }
    }

    /**
     * Quick game button callback
     */
    @Override
    public void btnQuickGameClicked() {
        Toast.makeText(this, "Not supported", Toast.LENGTH_SHORT).show();
    }

    /**
     * Invite players button callback
     */
    @Override
    public void btnInvitePlayersClicked() {
        Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(googleApiClient, 1, 1);
        startActivityForResult(intent, RC_SELECT_PLAYERS);
    }

    /**
     * View invitations button callback
     */
    @Override
    public void btnViewInvitationsClicked() {

    }

    /**
     * Achievements button callback
     */
    @Override
    public void btnAchievementsClicked() {

    }

    /**
     * Leaderboards button callback
     */
    @Override
    public void btnLeaderboardsClicked() {

    }

    /**
     * G+ sign-in button callback
     */
    @Override
    public void btnSignInClicked() {
        if(googleApiClient.isConnected()) {
            Log.d(TAG, "Already connected to Google Play Games.");
        } else {
            Log.d(TAG, "btnSignIn: Connecting to Google Play Games.");
            googleApiClient.connect();
        }
    }

    /**
     * Sign-out button callback
     */
    @Override
    public void btnSignOutClicked() {
        if(googleApiClient != null && googleApiClient.isConnected()) {
            Log.d(TAG, "Disconnecting from Google Play Games.");
            Games.signOut(googleApiClient);
            googleApiClient.disconnect();

            // show sign-in button
            SignInButton signInButton = (SignInButton) findViewById(R.id.btn_sign_in);
            signInButton.setVisibility(View.VISIBLE);

            // hide sign-out button
            Button signOutButton = (Button) findViewById(R.id.btn_sign_out);
            signOutButton.setVisibility(View.GONE);
        } else {
            Toast.makeText(this, R.string.already_signed_out, Toast.LENGTH_SHORT).show();
        }
    }
}