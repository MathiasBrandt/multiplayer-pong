package com.mathiasbrandt.android.multiplayerpong;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.google.gson.Gson;
import com.mathiasbrandt.android.multiplayerpong.listeners.RoomListener;
import com.mathiasbrandt.android.multiplayerpong.models.GameState;
import com.mathiasbrandt.android.multiplayerpong.tasks.VersionCheckerTask;

import java.util.ArrayList;


public class MainActivity
        extends Activity
        implements
            GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener,
            MainMenuFragment.MainMenuFragmentListener,
            GameFragment.GameFragmentListener {

    // tag for debug logging
    private final boolean DEBUG = true;
    private final String TAG = "MainActivity";

    // request codes
    public static final int RC_SIGN_IN = 7001;
    public static final int RC_SELECT_PLAYERS = 7002;
    public static final int RC_WAITING_ROOM = 7003;
    public static final int RC_GOOGLE_PLAY_SERVICES_ERROR = 7004;

    public static final String PARCELABLE_ROOM = "room";
    public static final String PARCELABLE_PLAYER = "player";
    public static final String PARCELABLE_OPPONENT = "opponent";
    public static final String PARCELABLE_IS_HOST = "host";

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

    private Player player;
    private Participant opponent;
    private Boolean isHost = false;

    private AlertDialog loadingDialog;

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

        if(isOnline()) {
            performVersionCheckAsync();
            checkGooglePlayServicesAndConnect();
        } else {
            showErrorDialog(R.string.no_internet_connection);
            mMainMenuFragment.modifyButtonStates(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop(): Disconnecting from Google Play Games.");

        disconnectFromGooglePlayServices();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to Google Play Games");


        // set player info in main menu fragment
        player = Games.Players.getCurrentPlayer(googleApiClient);
        mMainMenuFragment.setPlayerInfo(player);

        // hide sign-in button, show sign-out button
        mMainMenuFragment.modifySignInOutButtonVisibility(false);

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
                preventScreenSleep(true);

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
        if(isResolvingConnectionFailure) {
            return;
        }

        int errorCode = connectionResult.getErrorCode();
        String reason = connectionResult.toString();

        if(errorCode == ConnectionResult.SIGN_IN_REQUIRED) {
            Log.d(TAG, String.format("Connection to Google Play Games failed: Sign-in required (%s)", connectionResult.toString()));

            // start sign-in flow
            isResolvingConnectionFailure = true;
            if (BaseGameUtils.resolveConnectionFailure(this,
                    googleApiClient,
                    connectionResult,
                    RC_SIGN_IN,
                    getString(R.string.sign_in_fallback_error))) {
                isResolvingConnectionFailure = false;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, String.format("onAR: req: %d, res: %d", requestCode, resultCode));

        // returned from sign in. Try to connect
        if(requestCode == RC_SIGN_IN) {
            isResolvingConnectionFailure = false;

            if(resultCode == RESULT_OK) {
                googleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(this, requestCode, resultCode, R.string.sign_in_failed);

                // show sign-in button
                mMainMenuFragment.modifySignInOutButtonVisibility(true);
            }
        // returned from select opponent activity. Get opponent and create a game room.
        } else if(requestCode == RC_SELECT_PLAYERS) {
            if(resultCode == RESULT_OK) {
                // get invitee
                ArrayList<String> inviteeList = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
                if(inviteeList == null) {
                    Log.d(TAG, "Invitee list is null, handle auto matching!");
                    return;
                }

                isHost = true;

                // prevent screen from sleeping during handshake
                preventScreenSleep(true);

                RoomConfig roomConfig = buildRoom(inviteeList);
                Games.RealTimeMultiplayer.create(googleApiClient, roomConfig);

            } else {
                // user canceled
                return;
            }
        // returned from the waiting room. All players are ready - start the game
        } else if(requestCode == RC_WAITING_ROOM) {
            if(resultCode == RESULT_OK) {
                // all invited players were successfully connected to the room
                // start the game!
                Log.d(TAG, "All players connected - start game!");

                Bundle arguments = new Bundle();
                arguments.putParcelable(PARCELABLE_ROOM, room);
                arguments.putParcelable(PARCELABLE_PLAYER, player);
                arguments.putBoolean(PARCELABLE_IS_HOST, isHost);

                // get the opponent and set name in gui
                for(Participant participant : room.getParticipants()) {
                    if(!participant.getPlayer().getPlayerId().equals(player.getPlayerId())) {
                        opponent = participant;
                        break;
                    }
                }
                arguments.putParcelable(PARCELABLE_OPPONENT, opponent);

                mGameFragment.setArguments(arguments);
                switchToFragment(mGameFragment);
            } else if(resultCode == RESULT_CANCELED) {
                // player dismissed the waiting room
                // it should be possible to minimize the waiting room and continue connecting in the background
                // however, for now, just leave the room
                Log.d(TAG, "Player dismissed the waiting room (back button)");

                leaveRoom();
            } else if(resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player selected the leave room option
                Log.d(TAG, "Player left the room");

                leaveRoom();
            }
        }

        if(resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
            Log.d(TAG, "Reconnect required");
        }
    }

    private RoomConfig buildRoom(ArrayList<String> inviteeList) {
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(roomListener)
                .setMessageReceivedListener(roomListener)
                .setRoomStatusUpdateListener(roomListener);

        if(inviteeList != null) {
            roomConfigBuilder.addPlayersToInvite(inviteeList);
        }

        return roomConfigBuilder.build();
    }

    public void preventScreenSleep(Boolean enable) {
        if(enable) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public void sendGameState(String gameState) {
        byte[] gameStateBytes = gameState.getBytes();
        Games.RealTimeMultiplayer.sendReliableMessage(googleApiClient, null, gameStateBytes, room.getRoomId(), opponent.getParticipantId());
    }

    public void receiveGameState(String json) {
        Gson gson = new Gson();
        GameState gameState = gson.fromJson(json, GameState.class);
        mGameFragment.receiveGameState(gameState);
    }

    public void showErrorDialog(int message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.an_error_occurred)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    public void showLoadingDialog(int title, int message) {
        if(!loadingDialog.isShowing()) {
            loadingDialog = new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(title)
                    .setCancelable(false)
                    .create();

            loadingDialog.show();
        }
    }

    public void dismissLoadingDialog() {
        loadingDialog.dismiss();
    }

    private boolean isOnline() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();

    }

    private void performVersionCheckAsync() {
        Log.d(TAG, "Performing version check.");
        new VersionCheckerTask(this).execute();
    }

    private void checkGooglePlayServicesAndConnect() {
        Log.d(TAG, "onStart(): Checking for Google Play Services availability.");

        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if(statusCode == ConnectionResult.SERVICE_MISSING ||
                statusCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED ||
                statusCode == ConnectionResult.SERVICE_DISABLED ||
                statusCode == ConnectionResult.SERVICE_INVALID) {

            Log.d(TAG, "Error: Google Play services APK missing, out of date, or disabled");

            GooglePlayServicesUtil.getErrorDialog(statusCode, this, RC_GOOGLE_PLAY_SERVICES_ERROR, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // TODO: disable buttons -- this obviously also requires some way to enable them again ...

                }
            }).show();
        } else {
            Log.d(TAG, "Connecting to Google Play Games.");
            googleApiClient.connect();
        }
    }

    public void switchToMainMenu() {
        switchToFragment(mMainMenuFragment);
    }

    public void leaveRoom() {
        Games.RealTimeMultiplayer.leave(googleApiClient, roomListener, room.getRoomId());
        preventScreenSleep(false);
    }

    private void disconnectFromGooglePlayServices() {
        Log.d(TAG, "Disconnecting from Google Play Games.");

        // leave the room
        if(room != null) {
            leaveRoom();
        }

        // disconnect from Google Play games
        if(isSignedIn()) {
            Games.signOut(googleApiClient);
            googleApiClient.disconnect();
        }

        mMainMenuFragment.modifySignInOutButtonVisibility(true);
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
                        preventScreenSleep(true);

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
        disconnectFromGooglePlayServices();
    }
}