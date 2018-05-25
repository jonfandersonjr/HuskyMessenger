package tcss450.uw.edu.messengerapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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
    private String chatUsername;
    private String mSendUrl;
    private TextView mOutputTextView;
    private ListenManager mListenManager;
    private LinearLayout mFragment;
    private int currentMessages;
    private String mUserchatID = "1";
    private String chatID;
    public ArrayList<String> allUsernames = new ArrayList<>();


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
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        v.findViewById(R.id.chatSendButton).setOnClickListener(this::sendMessage);
        mOutputTextView = v.findViewById(R.id.chatOutput);
        mOutputTextView.setMovementMethod(new ScrollingMovementMethod());



        return v;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragment = (LinearLayout) view.findViewById(R.id.chatLayout);

//        scroll.post(new Runnable() {
//            @Override
//            public void run() {
//                scroll.fullScroll(View.FOCUS_DOWN);
//            }
//        });

//        Button b = new Button(getActivity());
//        b.setText("HI!!!!!");
//        mFragment.addView(b);
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
                    //chatUsername = username;
                    String userMessage = msg.get(getString(R.string.keys_json_message)).toString();
                    msgs[i] = username + ":" + userMessage;
                    allUsernames.add(username);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            getActivity().runOnUiThread(() -> {

                //Log.e("CURRENT MESSAGES", currentMessages + "");
                //Log.e("MSGLENGTH", msgs.length + "");
                //check if new messages!
                mOutputTextView = getView().findViewById(R.id.chatOutput);

                if(msgs.length !=  0 && currentMessages!= msgs.length) {
                    Log.e("CHANGE","CHANGE");
                   // mOutputTextView.setText("");
                    currentMessages = msgs.length;
                    for(int i = 0; i <msgs.length;i++) {
                        String [] sendUsername = msgs[i].split(":");



                        //smFragment.addView(b);
//                        ConstraintLayout c = (ConstraintLayout) getView().findViewById(R.id.chatFragment);
//                        TextView t = new TextView(getContext());
//                        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(
//                                ConstraintLayout.LayoutParams.WRAP_CONTENT, // Width of TextView
//                                ConstraintLayout.LayoutParams.WRAP_CONTENT);
//                        t.setLayoutParams(lp);
//                        t.setGravity(Gravity.CENTER);
//
//                        Log.e("MESSAGES", "ADDED");
//                        t.setText("HELLO");
                        if(mUsername.equals(sendUsername[0])) {
                            Button b = new Button(getActivity());
                            b.setText(msgs[i]);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.weight = 1.0f;
                            params.gravity = Gravity.RIGHT;
                            b.setLayoutParams(params);
                            //Log.e("TAG",""+test);
                            Log.e("Logged in", mUsername);
                            Log.e("Sender", sendUsername[0]);
                            b.setTextColor(Color.parseColor("#ffffff"));
                            b.setBackgroundResource(R.drawable.sent_messagee_box);
                            b.setPadding(8,8,8,8);
                            mFragment.addView(b);
                        } else {
                            Button b = new Button(getActivity());
                            b.setText(msgs[i]);
                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            b.setLayoutParams(params);
                            b.setTextColor(Color.parseColor("#ffffff"));
                            b.setBackgroundResource(R.drawable.message_box);


                            b.setPadding(8,8,8,8);
                            mFragment.addView(b);
                           // b.setGravity(Gravity.RIGHT);

                        }

//
//                        c.addView(t);

//                        mOutputTextView.append(msgs[i]);
//                        mOutputTextView.append(System.lineSeparator());
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
