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
    private TextView playerScore;
    private TextView opponentScore;
    private PongBall pongBall;
    private PongBat pongBat;
    private CollisionDetector collisionDetector;
    private GameLoopAsyncTask gameLoop;

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

        // get the opponent and set name in gui
        for(Participant participant : room.getParticipants()) {
            if(!participant.getPlayer().getPlayerId().equals(player.getPlayerId())) {
                this.opponent = participant;
                break;
            }
        }

        setPlayerNames(v);

        collisionDetector = new CollisionDetector(getActivity(), this, (FrameLayout) v.findViewById(R.id.game_container));
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
    }

    private void stopGame() {
        gameLoop.cancel(true);
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
        pongBall.flipHorizontalVelocity();
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

    }
}
