package com.mathiasbrandt.android.multiplayerpong.models;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.mathiasbrandt.android.multiplayerpong.CollisionDetector;

/**
 * Created by brandt on 01/03/15.
 */
public class PongBall extends View {
    private final String TAG = "PongBall";

    private static PongBall instance;
    private Context context;
    private CollisionDetector collisionDetector;

    private final int BALL_SIZE = 50;
    private final int BALL_HORIZONTAL_START_POS = 200;
    private final int BALL_VERTICAL_START_POS = 200;
    private final int BALL_INITIAL_VELOCITY = 2;

    private static Boolean isInitialized = false;

    private int velocityX;
    private int velocityY;

    private PongBall(Context context) {
        super(context);

        this.context = context;
    }

    public static PongBall getInstance(Context context) {
        if(instance == null) {
            instance = new PongBall(context);
        }

        return instance;
    }

    public void initialize(CollisionDetector collisionDetector) {
        if(isInitialized) {
            throw new ExceptionInInitializerError("Error: initialize() can only be called once.");
        }

        isInitialized = true;
        this.collisionDetector = collisionDetector;
        setBackgroundColor(Color.WHITE);
        setLayoutParams(new LinearLayout.LayoutParams(BALL_SIZE, BALL_SIZE));
        setPosition(BALL_HORIZONTAL_START_POS, BALL_VERTICAL_START_POS);
        velocityX = BALL_INITIAL_VELOCITY;
        velocityY = BALL_INITIAL_VELOCITY;
        // TODO: remove this
        velocityY = 0;
    }

    /**
     * Updates the position of the ball to the specified coordinates.
     * @param x the new x-coordinate of the ball
     * @param y the new y-coordinate of the ball
     */
    private void setPosition(float x, float y) {
        setX(x);
        setY(y);

        notifyPositionChanged();
    }

    /**
     * Moves the ball and updates its position according to the current velocity.
     */
    public void move() {
        setPosition(getX() + velocityX, getY() + velocityY);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    public void flipHorizontalVelocity() {
        Log.d(TAG, "Flipping horizontal velocity");
        velocityX *= -1;
    }

    public void flipVerticalVelocity() {
        Log.d(TAG, "Flipping vertical velocity");
        velocityY *= -1;
    }

    private void notifyPositionChanged() {
        collisionDetector.positionChanged();
    }

    public float getLeftEdge() {
        return getX();
    }

    public float getTopEdge() {
        return getY();
    }

    public float getRightEdge() {
        return getX() + BALL_SIZE;
    }

    public float getBottomEdge() {
        return getY() + BALL_SIZE;
    }
}
