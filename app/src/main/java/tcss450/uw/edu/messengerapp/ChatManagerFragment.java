package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

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
        return inflater.inflate(R.layout.fragment_chat_manager, container, false);
    }

}
