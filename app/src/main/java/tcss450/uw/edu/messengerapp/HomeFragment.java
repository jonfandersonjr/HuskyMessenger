package tcss450.uw.edu.messengerapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

        getRecentChats();

     //   createButtons(v);

        return v;
    }
/*
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
    */


    private void getRecentChats() {
        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        //mUsername = "test1";
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", prefs.getString(getString(R.string.keys_prefs_username), ""));
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
                .onPostExecute(this::getRecenthatsOnPost)
                .onCancelled(this::handleError)
                .build().execute();
    }

    private void getRecentChatsOnPre() {

    }

    private void getRecenthatsOnPost(String result) {
        /*
        try {
            JSONObject requests = new JSONObject(result);
            boolean success = requests.getBoolean("success");
            if (success) {
                final String[] reqs;
                if (requests.has("chats")) {
                    try {
                        JSONArray jReqs = requests.getJSONArray("chats");
                        Log.e("SIZE", "" + jReqs.length());
                        reqs = new String[jReqs.length()];
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String chatname = req.get(getString(R.string.keys_json_chatname))
                                    .toString();
                            String chatid = req.get(getString(R.string.keys_json_chatid))
                                    .toString();
                            Log.e("THE CHAT NAMES", chatname);
                            if (!(chatIdList.contains(chatid))) {
                                chatIdList.add(chatid);
                            }
                            if(!(mChatMap.containsKey(chatid))){
                                mChatMap.put(chatid,chatname);
                            }
                        }

                        for(int i = 0; i < chatIdList.size(); i++) {
                            //    if(!(addedNames.contains(mChatnames.get(i)))){
                            Button b = new Button(getActivity());
                            b.setText(chatIdList.get(i)); //Get chat name here!
                            int finalI = i;
                            b.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Log.e("BUTTON","" + b.getText());
                                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                                    intent.putExtra("CHAT_ID",chatIdList.get(finalI));
                                    startActivity(intent);
                                }
                            });
                            mChatManagerLayout.addView(b);
                            listSize = chatIdList.size();
                            addedNames.add(chatIdList.get(i));
                            //       }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    Log.e("HOW MANY CHATS", "" + chatIdList.size());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        */
    }


    private void handleError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

}
