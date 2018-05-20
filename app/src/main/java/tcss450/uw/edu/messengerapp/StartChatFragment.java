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
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.messengerapp.model.ListenManager;
import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class StartChatFragment extends Fragment {


    private String mUsername;
    private String mSendUrl;
    private TextView mOutputTextView;
    private ListenManager mListenManager;
    private int currentMessages = 0;
    private int mUserchatID;
    private int chatId;


    public StartChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        v.findViewById(R.id.createChat).setOnClickListener(this::createChat);
        return v;
    }

    private void createChat(View view) {

        JSONObject messageJson = new JSONObject();
        String msg = ((EditText) getView().findViewById(R.id.chatInput))
                .getText().toString();

        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);
            messageJson.put(getString(R.string.keys_json_message), msg);
            messageJson.put(getString(R.string.keys_json_chat_id), chatId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(this::handleError)
                .build().execute();

    }

    private void handleError(String s) {
    }

    private void endOfSendMsgTask(String messages)  {
        //S is chatID returned

        String newChatId = "";
//        try {
//             //newChatId = messages.get("chatid").toString();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }



        String msg = ((EditText) getView().findViewById(R.id.userNames))
                .getText().toString();

        //Split into usernames array

    }


    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");

//        mSendUrl = new Uri.Builder()
//                .scheme("https")
//                .appendPath(getString(R.string.ep_base_url))
//                .appendPath(getString(R.string.ep_post_createChat))
//                .build()
//                .toString();


//        Uri retrieve = new Uri.Builder()
//                .scheme("https")
//                .appendPath(getString(R.string.ep_base_url))
//                .appendPath(getString(R.string.ep_get_message))
//                .appendQueryParameter("chatId", chatId+"")
//                .build();
//        Log.i("A",retrieve.toString());
//
//
    }

}
