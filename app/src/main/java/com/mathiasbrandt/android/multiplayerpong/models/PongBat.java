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

    public static PongBat getInstance(Context context) {
        if(instance == null) {
            instance = new PongBat(context);
        }

        return instance;
    }

    public void initialize() {
        if(isInitialized) {
            throw new ExceptionInInitializerError("Error: initialize() can only be called once.");
        }

        isInitialized = true;
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
}