package com.mathiasbrandt.android.multiplayerpong;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.Room;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameFragment extends Fragment {
    private final String TAG = "GameFragment";
    private GameFragmentListener mListener;
    private Room room;

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_game, container, false);

        Bundle arguments = getArguments();
        this.room = arguments.getParcelable("room");

        if(room != null) {
            Log.d(TAG, String.format("In game. Room: %s", room.getRoomId()));
            Log.d(TAG, "Participants:");
            for (Participant p : room.getParticipants()) {
                Log.d(TAG, String.format("%s (%s)", p.getDisplayName(), p.getParticipantId()));
            }
        }

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
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface GameFragmentListener {

    }
}
