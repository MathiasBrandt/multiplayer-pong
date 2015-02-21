package com.mathiasbrandt.android.multiplayerpong;

import android.content.Intent;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import java.util.List;

/**
 * Created by brandt on 20/02/15.
 */
public class RoomListener
        implements
            RoomUpdateListener,
            RealTimeMessageReceivedListener,
            RoomStatusUpdateListener {

    // tag for debug logging
    private final String TAG = "RoomListener";

    private MainActivity context;

    public RoomListener(MainActivity context) {
        this.context = context;
    }

    /**
     * Called to notify the client that a reliable or unreliable message was received for a room.
     * @param realTimeMessage
     */
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        Log.d(TAG, "onRealTimeMessageReceived");

        String message = new String(realTimeMessage.getMessageData());
        Log.d(TAG, "Message received: " + message);
    }

    /**
     * Called when one or more participants have joined the room and have started the process of establishing peer connections.
     * @param room
     */
    @Override
    public void onRoomConnecting(Room room) {
        Log.d(TAG, "onRoomConnecting");

        context.setRoom(room);
    }

    /**
     * Called when the server has started the process of auto-matching.
     * @param room
     */
    @Override
    public void onRoomAutoMatching(Room room) {
        Log.d(TAG, "onRoomAutoMatching");

        context.setRoom(room);
    }

    /**
     * Called when one or more peers are invited to a room.
     * @param room
     * @param participantIds
     */
    @Override
    public void onPeerInvitedToRoom(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeerInvitedToRoom");

        context.setRoom(room);
    }

    /**
     * Called when one or more peers decline the invitation to a room.
     * @param room
     * @param participantIds
     */
    @Override
    public void onPeerDeclined(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeerDeclined");
        // a player that was previously invited has declined the invite

        context.setRoom(room);
    }

    /**
     * Called when one or more peer participants join a room.
     * @param room
     * @param participantIds
     */
    @Override
    public void onPeerJoined(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeerJoined");

        context.setRoom(room);
    }

    /**
     * Called when one or more peer participant leave a room.
     * @param room
     * @param participantIds
     */
    @Override
    public void onPeerLeft(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeerLeft");

        context.setRoom(room);
    }

    /**
     * Called when the client is connected to the connected set in a room.
     * @param room
     */
    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom");

        context.setRoom(room);
    }

    /**
     * Called when the client is disconnected from the connected set in a room.
     * @param room
     */
    @Override
    public void onDisconnectedFromRoom(Room room) {
        Log.d(TAG, "onDisconnectedFromRoom");

        context.setRoom(room);

        // leave the room
        Games.RealTimeMultiplayer.leave(context.getGoogleApiClient(), this, room.getRoomId());

        // clear the flag that keeps the screen on
        context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // show error message and return to main screen
    }

    /**
     * Called when one or more peer participants are connected to a room.
     * @param room
     * @param participantIds
     */
    @Override
    public void onPeersConnected(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeersConnected");

        context.setRoom(room);
    }

    /**
     * Called when one or more peer participants are disconnected from a room.
     * @param room
     * @param participantIds
     */
    @Override
    public void onPeersDisconnected(Room room, List<String> participantIds) {
        Log.d(TAG, "onPeersDisconnected");

        context.setRoom(room);
    }

    /**
     * Called when the client is successfully connected to a peer participant.
     * @param participantId
     */
    @Override
    public void onP2PConnected(String participantId) {
        Log.d(TAG, "onP2PConnected");
    }

    /**
     * Called when client gets disconnected from a peer participant.
     * @param participantId
     */
    @Override
    public void onP2PDisconnected(String participantId) {
        Log.d(TAG, "onP2PDisconnected");
    }

    /**
     * Called when the client attempts to create a real-time room.
     * @param statusCode
     * @param room
     */
    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Log.d(TAG, "onRoomCreated");

        context.setRoom(room);

        if(statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
            Toast.makeText(context, "Could not create room :(", Toast.LENGTH_LONG).show();

            return;
        }

        // launch waiting room
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(context.getGoogleApiClient(), room, Integer.MAX_VALUE);
        context.startActivityForResult(intent, MainActivity.RC_WAITING_ROOM);
    }

    /**
     * Called when the client attempts to join a real-time room.
     * @param statusCode
     * @param room
     */
    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Log.d(TAG, "onJoinedRoom");

        context.setRoom(room);

        if(statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
            Toast.makeText(context, "Could not join room :(", Toast.LENGTH_LONG).show();

            return;
        }

        // launch waiting room
        Intent intent = Games.RealTimeMultiplayer.getWaitingRoomIntent(context.getGoogleApiClient(), room, Integer.MAX_VALUE);
        context.startActivityForResult(intent, MainActivity.RC_WAITING_ROOM);
    }

    /**
     * Called when the client attempts to leaves the real-time room.
     * @param statusCode
     * @param roomId
     */
    @Override
    public void onLeftRoom(int statusCode, String roomId) {
        Log.d(TAG, String.format("onLeftRoom: statusCode: %d, roomId: %s", statusCode, roomId));
    }

    /**
     * Called when all the participants in a real-time room are fully connected.
     * @param statusCode
     * @param room
     */
    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Log.d(TAG, "onRoomConnected");

        context.setRoom(room);

        if(statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            context.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
            Toast.makeText(context, "Could not connect to room :(", Toast.LENGTH_LONG).show();

            return;
        }
    }


}
