package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

import tcss450.uw.edu.messengerapp.utils.ListenManager;
import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatManagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatManagerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mUsername;
    private Button chat1;
    private Button chat2;
    private int listSize = 0;
    private ArrayList<String> mChatnames = new ArrayList<String>();
    private ArrayList<String> addedNames = new ArrayList<String>();
    private LinearLayout mChatManagerLayout;// = new LinearLayout(this.getContext());


    private ListenManager mListenerManager;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public ChatManagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatManagerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatManagerFragment newInstance(String param1, String param2) {
        ChatManagerFragment fragment = new ChatManagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        //mChatManagerLayout = getView().findViewById(R.id.chatButtons);

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);



        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Button chat1 = (Button)




//        JSONObject messageJson = new JSONObject();
//        try {
//            messageJson.put("userId", 64);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        String mSendUrl = new Uri.Builder()
//                .scheme("https")
//                .appendPath(getString(R.string.ep_base_url))
//                .appendPath(getString(R.string.ep_get_chats))
//                .build()
//                .toString();
//
//
//
//        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
//                .onPostExecute(this::loadChats1)
//                .build().execute();
    }
    private void loadChats1(String result)  {
        // JSONObject res = new JSONObject(result);
        //res.getString("messages");
        // JSONObject message = result.getJSONObject("messages");
        //JSONArray ids = message.toJSONArray(
        System.out.println(result);
        JSONObject res = null;
        try {
            res = new JSONObject(result);
            String message = res.getString("message");
            JSONObject res2 = new JSONObject(message);
            System.out.println(message);

            JSONArray out = res2.getJSONArray("chatid");

        }

        catch (JSONException e) {
            e.printStackTrace();
        }

        //System.out.println(res.toString(1));
//            if(res.get(getString(R.string.keys_json_success)).toString()
//                    .equals(getString(R.string.keys_json_success_value_true))) {
//
//                ((EditText) getView().findViewById(R.id.chatInput))
//                        .setText("");
//            }
    }

    private void loadChat() {
        Intent j = new Intent(getActivity(), ChatFragment.class);
        startActivity(j);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat_manager, container, false);


        Log.e("BEFORE CREATING", "" +mChatnames.size());
        return rootView;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChatManagerLayout = (LinearLayout) view.findViewById(R.id.chatButtons);
        getAllChats();



        FloatingActionButton fab = (FloatingActionButton)view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//Create your Controls(UI widget, Button,TextView) and add into layout

                for(int i = 0; i < mChatnames.size(); i++) {

                    if(!(addedNames.contains(mChatnames.get(i)))){
                        Button b = new Button(getActivity());
                        b.setText(mChatnames.get(i));
                        mChatManagerLayout.addView(b);
                        listSize = mChatnames.size();
                        addedNames.add(mChatnames.get(i));

                    }



                }

                Log.e("VIEW CHATS", ""+ mChatnames.size());

            }
        });


    }


    private void getAllChats() {
        Log.e("CALL","Called get all chats");
        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        mUsername = "test1";
        //prefs.getString(getString(R.string.keys_prefs_username), "");

        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_all_chats))
                .appendQueryParameter("username", mUsername)
                .build();

        Log.e("CONTENT",retrieveRequests.toString());

        mListenerManager = new ListenManager.Builder(retrieveRequests.toString(), this::publishRequests)
                .setExceptionHandler(this::handleError)
                .setDelay(5000)
                .build();



    }
    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    private void publishRequests(JSONObject requests) {
        Log.e("ChatManager",requests.toString());
        final String[] reqs;
        if (requests.has("chats")) {
            Log.e("INSIDE","I got here!!!");

            try {
                JSONArray jReqs = requests.getJSONArray("chats");
                Log.e("SIZE", "" +jReqs.length());
                reqs = new String[jReqs.length()];
                for (int i = 0; i < jReqs.length(); i++) {
                    JSONObject req = jReqs.getJSONObject(i);
                    String chatname = req.get(getString(R.string.keys_json_chatname))
                            .toString();
                    Log.e("THE CHAT NAMES", chatname );
//                    chat1 = getView().findViewById(R.id.chat1);
//                    chat1.setVisibility(View.VISIBLE);
//                    chat1.setText(chatname);

                    if (!(mChatnames.contains(chatname))) {
                        mChatnames.add(chatname);
//                        Button b = new Button(getActivity());
//                        b = getView().findViewById(R.id.chat2);
//                        b.setVisibility(View.VISIBLE);
//                        b.setText(chatname);
                        //mChatManagerLayout.addView(b);
                    }




//                    String firstName = req.get(getString(R.string.keys_json_requests_firstname))
//                            .toString();
//                    String lastName = req.get(getString(R.string.keys_json_requests_lastname))
//                            .toString();
                    //String str = username + " (" + lastName + ", " +
                    //       firstName + ") has requested you as a connection!";



//                    if (!mRequests.contains(str)) {
//                        mRequests.add(str);
//                        mRequests.sort(String::compareToIgnoreCase);
//                        getActivity().runOnUiThread(() -> {
//                            mRecyclerAdapter.notifyDataSetChanged();
//                        });
//                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            Log.e("HOW MANY CHATS", ""+mChatnames.size());
        }
        //Log.e("HOW MANY CHATS", ""+mChatnames.size());
    }
    @Override
    public void onStart() {
        super.onStart();
        Log.e("HOW MANY CHATS", ""+mChatnames.size());

    }
    @Override
    public void onResume() {
        super.onResume();
        mListenerManager.startListening();
    }


}
