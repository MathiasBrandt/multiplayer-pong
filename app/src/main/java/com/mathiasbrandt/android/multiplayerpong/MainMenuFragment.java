package com.mathiasbrandt.android.multiplayerpong;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Player;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link com.mathiasbrandt.android.multiplayerpong.MainMenuFragment.MainMenuFragmentListener} interface
 * to handle interaction events.
 * Use the {@link MainMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainMenuFragment
        extends Fragment
        implements View.OnClickListener {

    private final String TAG = "MainMenuFragment";
    private MainMenuFragmentListener mListener;
    int[] buttonIds = {
            R.id.btn_quick_game,
            R.id.btn_invite_players,
            R.id.btn_view_invitations,
            R.id.btn_achievements,
            R.id.btn_leaderboards,
            R.id.btn_sign_in,
            R.id.btn_sign_out
    };

    public MainMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_main_menu, container, false);

        // register callbacks for buttons
        for(int buttonId : buttonIds) {
            v.findViewById(buttonId).setOnClickListener(this);
        }

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (MainMenuFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement MainMenuFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setPlayerInfo(Player player) {
        // download player icon
        if(player.hasIconImage()) {
            Uri iconUri = player.getIconImageUri();
            ImageManager imageManager = ImageManager.create(getActivity());
            imageManager.loadImage((ImageView) getActivity().findViewById(R.id.img_player_picture), iconUri, R.drawable.player_icon);
        }

        TextView playerName = (TextView) getActivity().findViewById(R.id.tv_player_name);
        playerName.setText(player.getDisplayName());

        TextView playerTitle = (TextView) getActivity().findViewById(R.id.tv_player_title);
        String playerTitleString = player.getTitle();

        if(playerTitleString != null) {
            playerTitle.setText(String.format("%s", playerTitleString));
        } else {
            playerTitle.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btn_quick_game:
                mListener.btnQuickGameClicked();
                break;
            case R.id.btn_invite_players:
                mListener.btnInvitePlayersClicked();
                break;
            case R.id.btn_view_invitations:
                mListener.btnViewInvitationsClicked();
                break;
            case R.id.btn_achievements:
                mListener.btnAchievementsClicked();
                break;
            case R.id.btn_leaderboards:
                mListener.btnLeaderboardsClicked();
                break;
            case R.id.btn_sign_in:
                mListener.btnSignInClicked();
                break;
            case R.id.btn_sign_out:
                mListener.btnSignOutClicked();
                break;
        }
    }

    public void modifyButtonStates(boolean shouldEnable) {
        for(int buttonId : buttonIds) {
            getActivity().findViewById(buttonId).setEnabled(shouldEnable);
        }
    }

    public void modifySignInOutButtonVisibility(boolean showSignIn) {
        SignInButton signInButton = (SignInButton) getActivity().findViewById(R.id.btn_sign_in);
        signInButton.setVisibility(showSignIn ? View.VISIBLE : View.GONE);

        Button signOutButton = (Button) getActivity().findViewById(R.id.btn_sign_out);
        signOutButton.setVisibility(!showSignIn ? View.VISIBLE : View.GONE);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface MainMenuFragmentListener {
        public void btnQuickGameClicked();
        public void btnInvitePlayersClicked();
        public void btnViewInvitationsClicked();
        public void btnAchievementsClicked();
        public void btnLeaderboardsClicked();
        public void btnSignInClicked();
        public void btnSignOutClicked();
    }

}
