package tcss450.uw.edu.messengerapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.messengerapp.model.ListenManager;
import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment newInstance} factory method to
 * create an instance of this fragment.
 */
@SuppressLint("ValidFragment")
public class ChatFragment extends Fragment {

    private String mUsername;
    private String mSendUrl;
    private TextView mOutputTextView;
    private ListenManager mListenManager;
    private int currentMessages;
    private String mUserchatID = "1";
    private String chatID;


    @SuppressLint("ValidFragment")
    public ChatFragment(String chatid) {
        chatID = chatid;
        currentMessages = 0;
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mUserchatID = getArguments().getString("CHAT_ID");
            Log.e("IN CHAT FRAGMENT", mUserchatID);
        }
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        v.findViewById(R.id.chatSendButton).setOnClickListener(this::sendMessage);
        mOutputTextView = v.findViewById(R.id.chatOutput);
        mOutputTextView.setMovementMethod(new ScrollingMovementMethod());


        return v;
    }

    @Override
    public void onStart() {

        super.onStart();

        currentMessages = 0;

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");

        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_send_message))
                .build()
                .toString();


        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_message))
                .appendQueryParameter("chatId", mUserchatID)
                .build();
        Log.e("GETTING MESSAGES FROM THIS ",retrieve.toString());

        //ADD FIRST CALL



        if (prefs.contains(getString(R.string.keys_prefs_time_stamp))) {
            //ignore all of the seen messages. You may want to store these messages locally
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setTimeStamp(prefs.getString(getString(R.string.keys_prefs_time_stamp), "0"))
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        } else {
            //no record of a saved timestamp. must be a first time login
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        currentMessages = 0;

        mListenManager.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        currentMessages = 0;

        String latestMessage = mListenManager.stopListening();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        // Save the most recent message timestamp
        prefs.edit().putString(
                getString(R.string.keys_prefs_time_stamp),
                latestMessage)
                .apply();
    }



    private void sendMessage(final View theButton) {
        JSONObject messageJson = new JSONObject();
        String msg = ((EditText) getView().findViewById(R.id.chatInput))
                .getText().toString();

        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);
            messageJson.put(getString(R.string.keys_json_message), msg);
            messageJson.put(getString(R.string.keys_json_chat_id), chatID); //
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(this::handleError)
                .build().execute();
    }

    private void handleError(final String msg) {
        Log.e("CHAT ERROR!!!", msg.toString());
    }

    private void endOfSendMsgTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);

            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {

                ((EditText) getView().findViewById(R.id.chatInput))
                        .setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    private void publishProgress(JSONObject messages) {
        final String[] msgs;

        if(messages.has(getString(R.string.keys_json_messages))) {
            try {

                JSONArray jMessages = messages.getJSONArray(getString(R.string.keys_json_messages));

                msgs = new String[jMessages.length()];

                //Log.e("LIST OF MESSAGES TO LOAD", msgs.length + "");
                for (int i = 0; i < jMessages.length(); i++) {
                    JSONObject msg = jMessages.getJSONObject(i);
                    String username = msg.get(getString(R.string.keys_json_username)).toString();
                    String userMessage = msg.get(getString(R.string.keys_json_message)).toString();
                    msgs[i] = username + ":" + userMessage;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            getActivity().runOnUiThread(() -> {

                Log.e("CURRENT MESSAGES", currentMessages + "");
                Log.e("MSGLENGTH", msgs.length + "");
                //check if new messages!
                mOutputTextView = getView().findViewById(R.id.chatOutput);

                if(msgs.length !=  0 && currentMessages!= msgs.length) {
                    Log.e("CHANGE","CHANGE");
                    mOutputTextView.setText("");
                    currentMessages = msgs.length;
                    for(int i = 0; i <msgs.length;i++) {
                        mOutputTextView.append(msgs[i]);
                        mOutputTextView.append(System.lineSeparator());
                    }
                }

//                if(currentMessages == 0) {
//                    for(int i = 0; i <msgs.length;i++) {
//                        mOutputTextView.append(msgs[i]);
//                        mOutputTextView.append(System.lineSeparator());
//                    }
//                    currentMessages = msgs.length;
//                }
//
//                else if(currentMessages < msgs.length) {
//                    int dif = msgs.length - currentMessages;
//
//                    for(int i = msgs.length-dif; i <msgs.length;i++) {
//                        mOutputTextView.append(msgs[i]);
//                        mOutputTextView.append(System.lineSeparator());
//                    }
//
//                    currentMessages = msgs.length;
//                }
            });
        }
    }



}
