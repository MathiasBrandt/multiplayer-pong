package com.mathiasbrandt.android.multiplayerpong.models;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Color;
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
    private CollisionDetector collisionListener;

    private final int BALL_SIZE = 50;
    private final int BALL_HORIZONTAL_START_POS = 200;
    private final int BALL_VERTICAL_START_POS = 200;
    private final int BALL_INITIAL_VELOCITY = 20;

    private int velocityX;
    private int velocityY;

    private PongBall(Context context, CollisionDetector collisionDetector) {
        super(context);

        this.context = context;
        initialize();
    }

    public static PongBall getInstance(Context context, CollisionDetector collisionDetector) {
        if(instance == null) {
            instance = new PongBall(context, collisionDetector);
        }

        return instance;
    }

    private void initialize() {
        setBackgroundColor(Color.WHITE);
        setLayoutParams(new LinearLayout.LayoutParams(BALL_SIZE, BALL_SIZE));
        setPosition(BALL_HORIZONTAL_START_POS, BALL_VERTICAL_START_POS);
        velocityX = BALL_INITIAL_VELOCITY;
        velocityY = BALL_INITIAL_VELOCITY;
    }

    public void setPosition(float x, float y) {
        setX(x);
        setY(y);

        notifyPositionChanged();
    }

    public void move() {
        setPosition(getX() + velocityX, getY() + velocityY);
    }

    public void hide() {
        setVisibility(GONE);
    }

    public void show() {
        setVisibility(VISIBLE);
    }

    private void notifyPositionChanged() {
        collisionListener.positionChanged(getX(), getY());
    }
}
