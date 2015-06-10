package com.mathiasbrandt.android.multiplayerpong.models;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.mathiasbrandt.android.multiplayerpong.CollisionDetector;
import com.mathiasbrandt.android.multiplayerpong.Common;

/**
 * Created by brandt on 01/03/15.
 */
public class PongBall extends View {
    private final String TAG = "PongBall";

    private static PongBall instance;
    private Context context;
    private CollisionDetector collisionDetector;

    private final int BALL_SIZE_DP = 25;
    private final int BALL_HORIZONTAL_START_POS = 200;
    private final int BALL_VERTICAL_START_POS = 200;
    private final int BALL_INITIAL_VELOCITY = 10;

    private static Boolean isInitialized = false;

    private float velocityX;
    private float velocityY;

    private PongBall(Context context, CollisionDetector collisionDetector) {
        super(context);

        this.context = context;

        this.collisionDetector = collisionDetector;
        setBackgroundColor(Color.WHITE);
        setLayoutParams(new LinearLayout.LayoutParams((int) Common.toPixels(context, BALL_SIZE_DP), (int) Common.toPixels(context, BALL_SIZE_DP)));

        /* Don't use setPosition here, as it will trigger notifyPositionChanged.
           We're not interested in this trigger since the game has not started
           and we're still building stuff */
        //setPosition(BALL_HORIZONTAL_START_POS, BALL_VERTICAL_START_POS);
        setX(BALL_HORIZONTAL_START_POS);
        setY(BALL_VERTICAL_START_POS);

        velocityX = BALL_INITIAL_VELOCITY;
        velocityY = BALL_INITIAL_VELOCITY;
        // TODO: remove this
        velocityY = 0;
    }

    public static PongBall getInstance() {
        if(instance == null) {
            throw new ExceptionInInitializerError("Error: PongBall must be initialized before calling getInstance.");
        }

        return instance;
    }

    public static void initialize(Context context, CollisionDetector collisionDetector) {
        if(isInitialized) {
            throw new ExceptionInInitializerError("Error: initialize() can only be called once.");
        }

        isInitialized = true;
        instance = new PongBall(context, collisionDetector);
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

    /*public void start() {
        velocityY = BALL_INITIAL_VELOCITY;
        velocityX = BALL_INITIAL_VELOCITY;
    }*/

    /*public void stop() {
        velocityY = 0;
        velocityX = 0;
    }*/

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
        return getX() + (int) Common.toPixels(context, BALL_SIZE_DP);
    }

    public float getBottomEdge() {
        return getY() + (int) Common.toPixels(context, BALL_SIZE_DP);
    }

    public float getVelocityX() {
        return velocityX;
    }

    public void setVelocityX(float velocityX) {
        this.velocityX = velocityX;
    }

    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }

    public Direction getHorizontalDirection() {
        return velocityX > 0 ? Direction.RIGHT : Direction.LEFT;
    }

    public Direction getVerticalDirection() {
        return velocityY > 0 ? Direction.DOWN : Direction.UP;
    }

    public enum Direction {
        LEFT, RIGHT, UP, DOWN;
    }
}
