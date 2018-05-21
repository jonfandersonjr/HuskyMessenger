package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String mUsername;
    private ArrayList<String> mChatTimes = new ArrayList<>();
    private ArrayList<String> mChatNames = new ArrayList<>();

    private Button[] mButtons = new Button[5];

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
        tv.setText("Welcome, " + mUsername + "!");

        initButtons(v);

        //getRecentChats();

        return v;
    }

    private void initButtons(View v) {
        mButtons[0] = v.findViewById(R.id.chat0);
        mButtons[1] = v.findViewById(R.id.chat1);
        mButtons[2] = v.findViewById(R.id.chat2);
        mButtons[3] = v.findViewById(R.id.chat3);
        mButtons[4] = v.findViewById(R.id.chat4);
    }


    private void getRecentChats() {

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }
        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_all_chats))
                .build();

        Log.e("CONTENT",retrieveRequests.toString());

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(retrieveRequests.toString(), msg)
                .onPreExecute(this::getRecentChatsOnPre)
                .onPostExecute(this::getRecentChatsOnPost)
                .onCancelled(this::handleError)
                .build().execute();
    }

    private void getRecentChatsOnPre() {

    }

    private void getRecentChatsOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_chats))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_chats));
                        for (int i = 0; i < jReqs.length(); i++) {

                            String time = "1970-01-01 00:00:00";

                            int chatid = jReqs.getJSONObject(i).getInt("chatid");
                            mChatNames.add(jReqs.getJSONObject(i).getString("name"));

                            //build the web service URL
                            Uri uri = new Uri.Builder()
                                    .scheme("https")
                                    .appendPath(getString(R.string.ep_base_url))
                                    .appendPath(getString(R.string.ep_post_get_messages))
                                    .build();

                            JSONObject msg = new JSONObject();
                            try {
                                msg.put("chatId", chatid);
                                msg.put("after", time);
                            } catch (JSONException e) {
                                Log.wtf("JSON EXCEPTION", e.toString());
                            }

                            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                                    .onPreExecute(this::handleGetMessagesOnPre)
                                    .onPostExecute(this::handleGetMessagesOnPost)
                                    .onCancelled(this::handleError)
                                    .build().execute();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }

    }


    public void handleGetMessagesOnPre() {

    }

    /**
     * Read through the list of chats and see if the most recent is from
     * somebody else and came recently.
     * @param result of finding chats
     */
    public void handleGetMessagesOnPost(String result) {

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_messages))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_messages));
                        String messageTime = jReqs.getJSONObject(jReqs.length()-1).getString("timestamp");
                        mChatTimes.add(messageTime);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    private String getMinChats() {
        int i = 0;
        int index = 0;
        for (String s: mChatTimes) {
            //mChatTimes.i
        }
        return "";
    }


    public boolean inLastMinute(String time, String currentTime) {

        String date = time.substring(0,10);
        String hour = time.substring(14,16);
        String minute = time.substring(17,19);

        String date1 = currentTime.substring(0,10);
        String hour1 = currentTime.substring(11,13);
        String minute1 = currentTime.substring(14,16);

        if(date.equals(date1) && hour.equals(hour1)) {
            int currentMinute = Integer.valueOf(minute1);
            int msgMinute = Integer.valueOf(minute);
            if (currentMinute == msgMinute || (currentMinute == (msgMinute + 1)) ) {
                return true;
            }
        }
        return false;
    }

    private void handleError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

}
