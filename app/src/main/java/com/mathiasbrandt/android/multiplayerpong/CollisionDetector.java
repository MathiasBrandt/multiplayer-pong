package com.mathiasbrandt.android.multiplayerpong;

import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.mathiasbrandt.android.multiplayerpong.models.PongBall;
import com.mathiasbrandt.android.multiplayerpong.models.PongBat;

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
        PongBall ball = PongBall.getInstance();
        PongBat bat = PongBat.getInstance();

        float ballLeft = ball.getLeftEdge();
        float ballTop = ball.getTopEdge();
        float ballBottom = ball.getBottomEdge();
        PongBall.Direction horizontalDirection = ball.getHorizontalDirection();
        PongBall.Direction verticalDirection = ball.getVerticalDirection();
        float batTop = bat.getTopEdge();
        float batRight = bat.getRightEdge();
        float batBottom = bat.getBottomEdge();

        checkBounds(horizontalDirection, verticalDirection, ballLeft, ballTop, ballBottom);
        checkBat(horizontalDirection, ballLeft, ballTop, ballBottom, batTop, batRight, batBottom);
    }

    /**
     * Checks if the ball has collided with any of the screen edges.
     * @param ballLeft
     * @param ballTop
     * @param ballBottom
     */
    private void checkBounds( PongBall.Direction horizontalDirection, PongBall.Direction verticalDirection, float ballLeft, float ballTop, float ballBottom) {
        if(horizontalDirection == PongBall.Direction.LEFT && ballLeft <= bounds.getLeft()) {
            // left
            Log.d(TAG, "Hit left");
            listener.onScreenLeftCollision();
        } else if(verticalDirection == PongBall.Direction.UP && ballTop <= 0) {
            // top
            Log.d(TAG, "Hit top");
            listener.onScreenTopCollision();
        } else if(horizontalDirection == PongBall.Direction.RIGHT && ballLeft > bounds.getRight()) {
            // right
            Log.d(TAG, "Hit right");
            listener.onScreenRightCollision();
        } else if(verticalDirection == PongBall.Direction.DOWN && ballBottom >= bounds.getHeight()) {
            // bottom
            Log.d(TAG, "Hit bottom");
            listener.onScreenBottomCollision();
        }
    }

    /**
     * Checks for collision between ball and bat.
     * @param ballLeft
     */
    private void checkBat(PongBall.Direction horizontalDirection, float ballLeft, float ballTop, float ballBottom, float batTop, float batRight, float batBottom) {
        /*if(horizontalDirection == PongBall.Direction.LEFT) {
            Log.d(TAG, String.format("ballLeft (%f) <= batRight (%f)", ballLeft, batRight));
            Log.d(TAG, String.format("ballBottom (%f) > batTop (%f)", ballBottom, batTop));
            Log.d(TAG, String.format("ballTop (%f) < batBottom (%f)", ballTop, batBottom));
        }*/

        if(horizontalDirection == PongBall.Direction.LEFT && ballLeft <= batRight) {
            // ball is moving towards bat and collided with right edge
            Log.d(TAG, String.format("ballLeft (%f) <= batRight (%f)", ballLeft, batRight));
            if(ballBottom > batTop && ballTop < batBottom) {
                // ball is actually hitting the bat, i.e., the ball's y-coordinate is within bat bounds
                Log.d(TAG, String.format("ballBottom (%f) > batTop (%f)", ballBottom, batTop));
                Log.d(TAG, String.format("ballTop (%f) < batBottom (%f)", ballTop, batBottom));

                listener.onBallCollisionWithBat();
            }
        }

        /*
        // if ball hits bat
        float ballLeft = ball.getPositionX();
        float ballBottom = ball.getPositionY() + ball.getLayoutParams().height;
        float ballTop = ball.getPositionY();
        else if(ballLeft <= bat.getRight() &&
                ballBottom >= bat.getTop() &&
                ballTop <= bat.getBottom() &&
                velocityX <= 0) {
            Log.d(TAG, "Pong!");
            velocityX *= -1;
            bat.setColor(Color.RED);
        }
        */
    }

    public interface CollisionListener {
        public void onScreenLeftCollision();
        public void onScreenTopCollision();
        public void onScreenRightCollision();
        public void onScreenBottomCollision();

        public void onBallCollisionWithBat();
    }
}
