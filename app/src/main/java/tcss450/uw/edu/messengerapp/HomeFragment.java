package tcss450.uw.edu.messengerapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import tcss450.uw.edu.messengerapp.model.PullService;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    String mUsername;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        TextView tv = v.findViewById(R.id.homeWelcome);

        SharedPreferences prefs = getActivity().
                getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");

        tv.setText("Welcome " + mUsername + "!");

        createButtons(v);

        return v;
    }

    private void createButtons(View v) {
        Button a = v.findViewById(R.id.chat0);
        setButtonFunctionality(a, v);
        Button b = v.findViewById(R.id.chat1);
        setButtonFunctionality(b, v);
        Button c = v.findViewById(R.id.chat2);
        setButtonFunctionality(c, v);
        Button d = v.findViewById(R.id.chat3);
        setButtonFunctionality(d, v);
        Button e = v.findViewById(R.id.chat4);
        setButtonFunctionality(e, v);
    }

    private void setButton0Listener() {Button button} {

    }




}
