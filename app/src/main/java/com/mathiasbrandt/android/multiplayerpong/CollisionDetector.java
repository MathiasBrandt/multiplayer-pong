package com.mathiasbrandt.android.multiplayerpong;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.mathiasbrandt.android.multiplayerpong.models.PongBall;

/**
 * Created by brandt on 01/03/15.
 */
public class CollisionDetector {
    private final String TAG = "CollisionDetector";

    private Context context;
    private CollisionListener listener;
    private FrameLayout bounds;


    public CollisionDetector(Context context, CollisionListener listener, FrameLayout bounds) {
        this.context = context;
        this.listener = listener;
        this.bounds = bounds;
    }

    public void positionChanged() {
        PongBall ball = PongBall.getInstance(context);

        float left = ball.getLeftEdge();
        float top = ball.getTopEdge();
        float right = ball.getRightEdge();
        float bottom = ball.getBottomEdge();

        checkBounds(left, top, right, bottom);
    }

    private void checkBounds(float ballLeft, float ballTop, float ballRight, float ballBottom) {
        PongBall ball = PongBall.getInstance(context);

        //Log.d(TAG, String.format("bl: %f, br: %f, bt: %f, bb: %f", ballLeft, ballRight, ballTop, ballBottom));
        //Log.d(TAG, String.format("left: %d, right: %d, top: %d, bottom: %d", bounds.getLeft(), bounds.getRight(), bounds.getTop(), bounds.getBottom()));

        if(ballLeft <= bounds.getLeft() && ball.getVelocityX() < 0) {
            // left
            Log.d(TAG, "Hit left");
            listener.onScreenLeftCollision();
        } else if(ballTop <= 0 && ball.getVelocityY() < 0) {
            // top
            Log.d(TAG, "Hit top");
            listener.onScreenTopCollision();
        } else if(ballLeft > bounds.getRight() && ball.getVelocityX() > 0) {
            // right
            Log.d(TAG, "Hit right");
            listener.onScreenRightCollision();
        } else if(ballBottom >= bounds.getHeight() && ball.getVelocityY() > 0) {
            // bottom
            Log.d(TAG, "Hit bottom");
            listener.onScreenBottomCollision();
        }
    }

    public interface CollisionListener {
        public void onScreenLeftCollision();
        public void onScreenTopCollision();
        public void onScreenRightCollision();
        public void onScreenBottomCollision();
    }
}
