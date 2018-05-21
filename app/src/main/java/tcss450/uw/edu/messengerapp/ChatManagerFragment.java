package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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
import java.util.HashMap;
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
    private ArrayList<String> chatIdList = new ArrayList<String>();
    private HashMap<String, String> mChatMap = new HashMap<>();
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
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mChatManagerLayout = (LinearLayout) view.findViewById(R.id.chatButtons);
        getAllChats();
    }


    private void getAllChats() {
        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
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
                .onPreExecute(this::handleGetChatsOnPre)
                .onPostExecute(this::publishRequests)
                .onCancelled(this::handleError)
                .build().execute();



    }
    public void handleGetChatsOnPre() {

    }
    private void handleError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

    public void publishRequests(String result) {
        try {
            JSONObject requests = new JSONObject(result);
            boolean success = requests.getBoolean("success");
            if (success) {
                Log.e("ChatManager", requests.toString());
                final String[] reqs;
                if (requests.has("chats")) {
                    try {
                        JSONArray jReqs = requests.getJSONArray("chats");
                        reqs = new String[jReqs.length()];
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String chatname = req.get(getString(R.string.keys_json_chatname))
                                    .toString();
                            String chatid = req.get(getString(R.string.keys_json_chatid))
                                    .toString();
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
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.e("HOW MANY CHATS", ""+mChatnames.size());
    }
    public void loadFragment(Fragment theFragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.chatContainer, theFragment);
        // Commit the transaction
        transaction.commit();
    }
    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onResume() {
        super.onResume();
        //mListenerManager.startListening();
    }


}