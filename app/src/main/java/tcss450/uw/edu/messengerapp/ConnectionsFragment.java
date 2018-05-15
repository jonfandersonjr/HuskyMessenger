package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tcss450.uw.edu.messengerapp.model.MyRecyclerViewAdapter;
import tcss450.uw.edu.messengerapp.utils.ListenManager;


public class ConnectionsFragment extends Fragment implements AdapterView.OnItemSelectedListener,
        MyRecyclerViewAdapter.ItemClickListener {

    private String mUsername;
    private String mSearchBy;
    private ListenManager mListenerManager;
    private ListenManager mPendingListenManager;
    private ArrayList<String> mRequests;
    private ArrayList<String> mVerified;
    private TextView mPendingTextList;
    private RecyclerView mRequestList;
    private RecyclerView mVerifiedList;
    private MyRecyclerViewAdapter mRecyclerAdapter;
    private MyRecyclerViewAdapter mVerifiedRecyclerAdapter;
    public ConnectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_connections, container, false);

        Spinner spinner = (Spinner) v.findViewById(R.id.connectionsSearchSpinner);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.connections_search_filter,
                android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);


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
        mVerified = new ArrayList<>();

        getContacts();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mRequestList = getView().findViewById(R.id.connectionsRequestsRecycler);
        mRequestList.setLayoutManager(layoutManager);

        mRecyclerAdapter = new MyRecyclerViewAdapter(getActivity(), mRequests);
        mRecyclerAdapter.setClickListener(this);
        mRequestList.setAdapter(mRecyclerAdapter);

        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mRequestList.getContext(),
                        layoutManager.getOrientation());
        mRequestList.addItemDecoration(dividerItemDecoration);

        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_requests))
                .appendQueryParameter("username", mUsername)
                .build();

        mListenerManager = new ListenManager.Builder(retrieveRequests.toString(), this::publishRequests)
                .setExceptionHandler(this::handleError)
                .setDelay(5000)
                .build();

    }

    public void getContacts() {
        SharedPreferences prefs =
                getActivity().getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        String username = prefs.getString(getString(R.string.keys_prefs_username), "");

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_contacts))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", username);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleContactsOnPre)
                .onPostExecute(this::handleContactsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    public void handleContactsOnPre() {

    }

    public void handleContactsOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_connections_a))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_connections_a));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String username = req.get(getString(R.string.keys_json_username))
                                    .toString();
                            String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                                    .toString();
                            String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                                    .toString();
                            String str = username + " (" + lastName +", " + firstName + ")";
                            mVerified.add(str);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                if (resultsJSON.has(getString(R.string.keys_json_connections_b))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_connections_b));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String username = req.get(getString(R.string.keys_json_username))
                                    .toString();
                            String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                                    .toString();
                            String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                                    .toString();
                            String str = username + " (" + lastName +", " + firstName + ")";
                            mVerified.add(str);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                mVerified.sort(String::compareToIgnoreCase);

                LinearLayoutManager layoutManager2 = new LinearLayoutManager(getActivity());

                mVerifiedList = getView().findViewById(R.id.connectionsVerifiedRecycler);
                mVerifiedList.setLayoutManager(layoutManager2);

                mVerifiedRecyclerAdapter = new MyRecyclerViewAdapter(getActivity(), mVerified);
                mVerifiedRecyclerAdapter.setClickListener(this);
                mVerifiedList.setAdapter(mVerifiedRecyclerAdapter);

                DividerItemDecoration dividerItemDecoration2 =
                        new DividerItemDecoration(mVerifiedList.getContext(),
                                layoutManager2.getOrientation());
                mVerifiedList.addItemDecoration(dividerItemDecoration2);
            } else {
                Log.e("IT DOESN'T WORK", "WHY NOT");
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    public void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        String choice = (String) adapterView.getAdapter().getItem(i);
        if (choice.equals("Username")) {
            mSearchBy = "username";
        } else if (choice.equals("First Name")) {
            mSearchBy = "firstname";
        } else if (choice.equals("Last Name")) {
            mSearchBy = "lastname";
        } else {
            mSearchBy = "email";
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onItemClick(View v, int position) {

    }

    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
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

                    if (!mRequests.contains(str)) {
                        mRequests.add(str);
                        mRequests.sort(String::compareToIgnoreCase);
                        getActivity().runOnUiThread(() -> {
                            mRecyclerAdapter.notifyDataSetChanged();
                        });
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    }

}
