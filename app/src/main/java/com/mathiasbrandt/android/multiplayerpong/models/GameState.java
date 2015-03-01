package com.mathiasbrandt.android.multiplayerpong.models;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by brandt on 01/03/15.
 */
public class GameState {
    private static final String TAG = "GameState";

    private float x;
    private float y;
    private float velocityX;
    private float velocityY;

    private GameState(float x, float y, float velocityX, float velocityY) {
        this.x = x;
        this.y = y;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getVelocityX() {
        return velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public static String serialize(Context context) {
        PongBall ball = PongBall.getInstance(context);
        GameState gameState = new GameState(ball.getX(), ball.getY(), ball.getVelocityX(), ball.getVelocityY());

        Gson gson = new Gson();
        String json = gson.toJson(gameState);
        Log.d(TAG, String.format("Game state: %s (size: %d)", json, json.getBytes().length));

        return json;
    }
}
