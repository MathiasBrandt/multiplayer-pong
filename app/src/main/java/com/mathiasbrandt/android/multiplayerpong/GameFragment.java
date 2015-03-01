package com.mathiasbrandt.android.multiplayerpong;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.mathiasbrandt.android.multiplayerpong.models.GameState;
import com.mathiasbrandt.android.multiplayerpong.models.PongBall;
import com.mathiasbrandt.android.multiplayerpong.models.PongBat;
import com.mathiasbrandt.android.multiplayerpong.tasks.GameLoopAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment implements View.OnClickListener, CollisionDetector.CollisionListener {
    private final String TAG = "GameFragment";
    private GameFragmentListener mListener;
    private Room room;
    private Player player;
    private Participant opponent;
    private Boolean isHost;
    private TextView playerScore;
    private TextView opponentScore;
    private PongBall pongBall;
    private PongBat pongBat;
    private CollisionDetector collisionDetector;
    private GameLoopAsyncTask gameLoop;
    private FrameLayout bounds;

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_game, container, false);

        Bundle arguments = getArguments();
        this.room = arguments.getParcelable(MainActivity.PARCELABLE_ROOM);
        this.player = arguments.getParcelable(MainActivity.PARCELABLE_PLAYER);
        this.opponent = arguments.getParcelable(MainActivity.PARCELABLE_OPPONENT);
        this.isHost = arguments.getBoolean(MainActivity.PARCELABLE_IS_HOST);
        setPlayerNames(v);

        this.bounds = (FrameLayout) v.findViewById(R.id.game_container);

        collisionDetector = new CollisionDetector(getActivity(), this, bounds);
        PongBall.getInstance(getActivity()).initialize(collisionDetector);
        PongBat.getInstance(getActivity()).initialize();

        addGameElements(v);

        startGame();

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (GameFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement GameFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link android.app.Activity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();

        stopGame();
    }

    /**
     * Sets the player's and opponent's names in the gui
     */
    private void setPlayerNames(View context) {
        TextView tvPlayerName = (TextView) context.findViewById(R.id.tv_player_name);
        tvPlayerName.setText(player.getDisplayName());

        TextView tvOpponentName = (TextView) context.findViewById(R.id.tv_opponent_name);
        tvOpponentName.setText(opponent.getDisplayName());
    }

    private void addGameElements(View context) {
        pongBall = PongBall.getInstance(getActivity());
        pongBat = PongBat.getInstance(getActivity());

        FrameLayout gameContainer = (FrameLayout) context.findViewById(R.id.game_container);
        gameContainer.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        gameContainer.addView(pongBall);
        gameContainer.addView(pongBat);
    }

    private void startGame() {
        gameLoop = new GameLoopAsyncTask();
        gameLoop.execute();
        setMyTurn(isHost);
    }

    private void stopGame() {
        gameLoop.cancel(true);
    }

    public void receiveGameState(GameState gameState) {
        //pongBall.setX(gameState.getX());
        pongBall.setX(bounds.getRight());
        pongBall.setY(gameState.getY());
        pongBall.setVelocityX(gameState.getVelocityX());
        pongBall.setVelocityY(gameState.getVelocityY());

        setMyTurn(true);
    }

    private void setMyTurn(Boolean myTurn) {
        if(myTurn) {
            pongBall.show();
        } else {
            pongBall.hide();
        }

        gameLoop.setMyTurn(myTurn);
    }

    @Override
    public void onScreenLeftCollision() {
        pongBall.flipHorizontalVelocity();
    }

    @Override
    public void onScreenTopCollision() {

    }

    @Override
    public void onScreenRightCollision() {
        setMyTurn(false);

        // flip the direction of the ball, so the opponent will be attacked
        pongBall.flipHorizontalVelocity();

        // serialize game state before stopping the ball, otherwise velocity will be 0
        String gameState = GameState.serialize(getActivity());

        // send the game state to the opponent
        mListener.sendGameState(gameState);
    }

    @Override
    public void onScreenBottomCollision() {

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface GameFragmentListener {
        public void sendGameState(String gameState);
    }
}
