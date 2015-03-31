package com.mathiasbrandt.android.multiplayerpong.models;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.mathiasbrandt.android.multiplayerpong.Common;

/**
 * Created by brandt on 01/03/15.
 */
public class PongBat extends View {
    private final String TAG = "PongBat";

    private static PongBat instance;

    private Context context;

    private final int BAT_HEIGHT_DP = 100;
    private final int BAT_WIDTH_DP = 25;
    private final int BAT_HORIZONTAL_START_POS = 100;
    private final int BAT_VERTICAL_START_POS = 100;

    private static Boolean isInitialized = false;

    private PongBat(Context context) {
        super(context);

        this.context = context;
    }

    public static PongBat getInstance() {
        if(instance == null) {
            throw new ExceptionInInitializerError("Error: PongBall must be initialized before calling getInstance.");
        }

        return instance;
    }

    public void initialize(Context context) {
        if(isInitialized) {
            throw new ExceptionInInitializerError("Error: initialize() can only be called once.");
        }

        isInitialized = true;
        instance = new PongBat(context);
        setBackgroundColor(Color.WHITE);
        setLayoutParams(new LinearLayout.LayoutParams((int) Common.toPixels(context, BAT_WIDTH_DP), (int) Common.toPixels(context, BAT_HEIGHT_DP)));
        setPosition(BAT_HORIZONTAL_START_POS, BAT_VERTICAL_START_POS);
        setTouchListener();
    }

    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    private void setTouchListener() {
        setOnTouchListener(new OnTouchListener() {
            float delta = 0;

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        delta = motionEvent.getRawY() - getY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        setY(motionEvent.getRawY() - delta);
                        return true;
                }

                return false;
            }
        });
    }

    public float getLeftEdge() {
        return getX();
    }

    public float getTopEdge() {
        return getY();
    }

    public float getRightEdge() {
        return getX() + Common.toPixels(context, BAT_WIDTH_DP);
    }

    public float getBottomEdge() {
        return getY() + Common.toPixels(context, BAT_HEIGHT_DP);
    }
}
