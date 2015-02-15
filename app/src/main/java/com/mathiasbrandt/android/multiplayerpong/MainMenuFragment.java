package com.mathiasbrandt.android.multiplayerpong;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Player;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.mathiasbrandt.android.multiplayerpong.MainMenuFragment.FragmentListener} interface
 * to handle interaction events.
 * Use the {@link MainMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMenuFragment extends Fragment {
    private final String TAG = "MainMenuFragment";
    private FragmentListener mListener;
    private Player player;

    public static MainMenuFragment newInstance(String param1, String param2) {
        MainMenuFragment fragment = new MainMenuFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MainMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main_menu, container, false);

        // register callback for Google+ Sign-in Button, since it does not work in XML
        SignInButton btnSignIn = (SignInButton) v.findViewById(R.id.btn_sign_in);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (FragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setPlayer(Player player) {
        // download player icon
        Uri iconUri = player.getIconImageUri();
        ImageManager imageManager = ImageManager.create(getActivity());
        imageManager.loadImage((ImageView) getActivity().findViewById(R.id.img_player_picture), iconUri);

        TextView playerName = (TextView) getActivity().findViewById(R.id.tv_player_name);
        playerName.setText(player.getDisplayName());

        TextView playerTitle = (TextView) getActivity().findViewById(R.id.tv_player_title);
        String playerTitleString = player.getTitle();

        if(playerTitleString != null) {
            playerTitle.setText(String.format("\"%s\"", playerTitleString));
        } else {
            playerTitle.setVisibility(View.GONE);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface FragmentListener {

    }

}
