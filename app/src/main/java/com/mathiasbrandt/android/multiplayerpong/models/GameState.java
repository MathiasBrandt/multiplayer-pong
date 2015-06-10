package com.mathiasbrandt.android.multiplayerpong.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by brandt on 01/03/15.
 */
public class GameState {
    private static final String TAG = "GameState";

    private MessageType mType;
    private float pX;
    private float pY;
    private float vX;
    private float vY;

    private GameState(MessageType mType, float pX, float pY, float vX, float vY) {
        this.mType = mType;
        this.pX = pX;
        this.pY = pY;
        this.vX = vX;
        this.vY = vY;
    }

    public MessageType getMType() { return mType; }

    public float getPositionX() {
        return pX;
    }

    public float getPositionY() {
        return pY;
    }

    public float getVelocityX() {
        return vX;
    }

    public float getVelocityY() {
        return vY;
    }

    public static String serialize(Context context, MessageType mType) {
        PongBall ball = PongBall.getInstance();
        GameState gameState = new GameState(mType, ball.getX(), ball.getY(), ball.getVelocityX(), ball.getVelocityY());

        Gson gson = new Gson();
        String json = gson.toJson(gameState);
        Log.d(TAG, String.format("Game state: %s (size: %d)", json, json.getBytes().length));

        return json;
    }

    public enum MessageType {
        SWITCH_TURN, UPDATE_SCORE, GAME_OVER, GHOST
    }
}
