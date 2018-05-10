package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tcss450.uw.edu.messengerapp.utils.ListenManager;


public class ConnectionsFragment extends Fragment {

    private TextView mRequestsTextView;
    private String mUsername;
    private ListenManager mListenerManager;
    private ArrayList<String> mRequests;

    public ConnectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connections, container, false);

        mRequestsTextView = v.findViewById(R.id.requestTextView);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        mRequests = new ArrayList<>();

        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_requests))
                .appendQueryParameter("username", mUsername)
                .build();

        if (prefs.contains(getString(R.string.keys_prefs_time_stamp))) {
            mListenerManager = new ListenManager.Builder(retrieve.toString(), this::publishRequests)
                    .setTimeStamp(prefs.getString(getString(R.string.keys_prefs_time_stamp), "0"))
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        } else {
            mListenerManager = new ListenManager.Builder(retrieve.toString(), this::publishRequests)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mListenerManager.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        String latestMessage = mListenerManager.stopListening();
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(getString(R.string.keys_shared_prefs), Context.MODE_PRIVATE);

        prefs.edit().putString(getString(R.string.keys_prefs_time_stamp), latestMessage).apply();
    }

    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    private void getUserInfoFromId(int memberId) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_credentials_id))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("memberId", memberId);
        } catch (JSONException e) {
            Log.wtf("getUserInfoFromId", "Error reading JSON" + e.getMessage());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleOnPre)
                .onPostExecute(this::handleOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleOnPre() {
        ProgressBar progBar = getView().findViewById(R.id.connectionsProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void handleOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                String username = resultsJSON.getString(getString(R.string.keys_json_username));
                String firstName = resultsJSON.getString(getString(R.string.keys_json_firstname));
                String lastName = resultsJSON.getString(getString(R.string.keys_json_lastname));

                if (!mRequests.contains(username)) {
                    mRequestsTextView.append(username + " (" + lastName + ", " +
                            firstName + ") has requested you as a connection!");
                    mRequestsTextView.append(System.lineSeparator());
                    mRequestsTextView.append(System.lineSeparator());

                    mRequests.add(username);

                }
            } else {
                Toast.makeText(getActivity(), "Something went wrong in handleOnPost",
                        Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }

    }

    private void handleOnError() {
        ProgressBar progBar = getView().findViewById(R.id.loginProgressBar);
        progBar.setVisibility(ProgressBar.GONE);
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    private void publishRequests(JSONObject requests) {
        final String[] reqs;
        if (requests.has(getString(R.string.keys_json_requests))) {
            try {
                JSONArray jReqs = requests.getJSONArray(getString(R.string.keys_json_requests));
                reqs = new String[jReqs.length()];
                for (int i = 0; i < jReqs.length(); i++) {
                    JSONObject req = jReqs.getJSONObject(i);
                    String username = req.get(getString(R.string.keys_json_username))
                            .toString();
                    String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                            .toString();
                    String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                            .toString();
                    String str = username + " (" + lastName + ", " +
                            firstName + ") has requested you as a connection!";

                    //helperAppendMethod(reqs[i], username);

                    if (!mRequests.contains(username)) {
                        getActivity().runOnUiThread(() -> {
                            mRequestsTextView.append(str);
                            mRequestsTextView.append(System.lineSeparator());
                            mRequestsTextView.append(System.lineSeparator());
                        });

                        mRequests.add(username);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
//
//            getActivity().runOnUiThread(() -> {
//                for (String req : reqs) {
//
//                        mRequestsTextView.append(req);
//                        mRequestsTextView.append(System.lineSeparator());
//                        mRequestsTextView.append(System.lineSeparator());
//                }
//            });
        }
    }

    private void helperAppendMethod(String str, String username) {

        if (!mRequests.contains(username)) {
            mRequestsTextView.append(str);
            mRequestsTextView.append(System.lineSeparator());
            mRequestsTextView.append(System.lineSeparator());

            mRequests.add(username);

        }
    }

}
