package tcss450.uw.edu.messengerapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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

    private ArrayList<String> mRequests;
    private ArrayList<String> mVerified;
    private ArrayList<String> mPending;

    private RecyclerView mRequestList;
    private RecyclerView mVerifiedList;
    private RecyclerView mPendingList;

    private MyRecyclerViewAdapter mRecyclerAdapter;
    private MyRecyclerViewAdapter mVerifiedRecyclerAdapter;
    private MyRecyclerViewAdapter mPendingAdapter;

    private OnConnectionsInteractionListener mInteractionListener;

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

        ImageButton b = (ImageButton) v.findViewById(R.id.connectionsSearchButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSearchButtonClick();
            }
        });


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
        mPending = new ArrayList<>();

        getContacts();
        getPending();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mRequestList = getView().findViewById(R.id.connectionsRequestsRecycler);
        mRequestList.setLayoutManager(layoutManager);

        mRecyclerAdapter = new MyRecyclerViewAdapter(getActivity(), mRequests);
        mRecyclerAdapter.setClickListener(this::onItemClickRequests);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConnectionsInteractionListener) {
            mInteractionListener = (OnConnectionsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnConnectionsInteractionListener");
        }
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
        mSearchBy = "username";
    }

    @Override
    public void onItemClick(View v, int position) {
        String str = mVerifiedRecyclerAdapter.getItem(position);
        str = str.substring(0, str.indexOf(" "));

    }

    public void onItemClickRequests(View v, int position) {
        String connections = "connections";
        String str = mRecyclerAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Resolve Request").setMessage("Would you like to accept " + username
                + "'s connection request?")
                .setPositiveButton(getString(R.string.connections_decline_request_diaglog_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean accept = false;
                        mInteractionListener.onRequestInteractionListener(username, accept, connections);
                    }
                })
                .setNegativeButton(getString(R.string.connections_accept_request_dialog_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                boolean accept = true;
                                mInteractionListener.onRequestInteractionListener(username, accept, connections);
                            }
                        })
                .setIcon(R.drawable.ic_person_add)
                .show();

    }

    public void onItemClickPending(View v, int position) {
        String str = mPendingAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));

        AlertDialog.Builder builder;
        builder = new AlertDialog
                .Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Remove Pending Request")
                .setMessage("Would you like to cancel your connection request to " +
                        username + "?")
                .setPositiveButton(getString(R.string.searchConnections_remove_pending),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                builder.setTitle("Confirm Cancellation")
                                        .setMessage("Are you sure you want to cancel your connection request to " +
                                                username + "?")
                                        .setPositiveButton(getString(R.string.searchConnections_Yes),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        boolean accept = false;
                                                        final String frag = "connectionsPending";
                                                        mInteractionListener.onRequestInteractionListener(username, accept, frag);
                                                    }
                                                })
                                        .setNegativeButton(getString(R.string.searchConnections_Nah),
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {

                                                    }
                                                })
                                        .setIcon(R.drawable.ic_exclaimation)
                                        .show();
                            }
                        })
                .setNegativeButton(getString(R.string.searchConnections_nevermind),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                .setIcon(R.drawable.delete)
                .show();
    }

    public void onSearchButtonClick() {
        EditText search = (EditText) getView().findViewById(R.id.connectionsSearchEditText);
        String searchString = search.getText().toString();

        if (searchString.trim().length() == 0) {
            search.setError("Field cannot be empty");
        } else {
            mInteractionListener
                    .onSearchInteractionListener(mSearchBy, searchString,
                            mVerified, mRequests, mPending);
        }

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
                .onPreExecute(this::handleRequestOnPre)
                .onPostExecute(this::handleContactsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    public void getPending() {
        SharedPreferences prefs = getActivity().
                getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        String username = prefs.getString(getString(R.string.keys_prefs_username), "");

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_pending_requests))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", username);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleRequestOnPre)
                .onPostExecute(this::handlePendingOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

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

                            if (!mVerified.contains(str)) {
                                mVerified.add(str);
                            }
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

                ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

                enableDisableViewGroup(vg, true);

            } else {
                Log.e("IT DOESN'T WORK", "WHY NOT");
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    public void handlePendingOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_requests))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_requests));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject req = jReqs.getJSONObject(i);
                            String username = req.get(getString(R.string.keys_json_username))
                                    .toString();
                            String firstName = req.get(getString(R.string.keys_json_requests_firstname))
                                    .toString();
                            String lastName = req.get(getString(R.string.keys_json_requests_lastname))
                                    .toString();
                            String str = username + " (" + lastName + ", " + firstName + ")";

                            if (!mPending.contains(str)) {
                                mPending.add(str);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                mPending.sort(String::compareToIgnoreCase);

                LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

                mPendingList = getView().findViewById(R.id.connectionsPendingRecycler);
                mPendingList.setLayoutManager(layoutManager);

                mPendingAdapter = new MyRecyclerViewAdapter(getActivity(), mPending);
                mPendingAdapter.setClickListener(this::onItemClickPending);
                mPendingList.setAdapter(mPendingAdapter);

                DividerItemDecoration dividerItemDecoration =
                        new DividerItemDecoration(mPendingList.getContext(),
                                layoutManager.getOrientation());
                mVerifiedList.addItemDecoration(dividerItemDecoration);

                if (!mPending.isEmpty()) {
                    TextView tv = getView().findViewById(R.id.connectionsPendingHeaderTextView);
                    tv.setVisibility(TextView.VISIBLE);

                    View v = getView().findViewById(R.id.connectionsDividerThree);
                    v.setVisibility(View.VISIBLE);
                }

                ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

                enableDisableViewGroup(vg, true);

            } else {
                Log.wtf("IT'S NOT WORKING (PENDING ON POST)", "WHY NOT");
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    public void handleErrorsInTask(String result) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);

        Log.e("ASYNC_TASK_ERROR", result);
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

                        TextView tv = getView().findViewById(R.id.connectionsRequestsHeaderTextView);

                        mRequests.sort(String::compareToIgnoreCase);
                        getActivity().runOnUiThread(() -> {
                            mRecyclerAdapter.notifyDataSetChanged();
                            if (tv.getVisibility() == View.GONE) {
                                tv.setVisibility(TextView.VISIBLE);

                                View v = getView().findViewById(R.id.connectionsDividerTwo);
                                v.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void handleRequestOnPre() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, false);

    }

    public void handleRequestOnPost(boolean success, String username, boolean accept) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

        if (success) {
            for (int i = 0; i < mRequests.size(); i++) {
                String str = mRequests.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    String[] arr = str.split(" ");
                    String verified = arr[0] + " " + arr[1] + " " + arr[2];

                    if (accept) {
                        mVerified.add(verified);
                        mVerified.sort(String::compareToIgnoreCase);
                        mVerifiedRecyclerAdapter.notifyDataSetChanged();
                    }
                    mRequests.remove(i);
                    mRecyclerAdapter.notifyDataSetChanged();
                    break;
                }
            }

            if (mRequests.isEmpty()) {
                TextView tv = getView().findViewById(R.id.connectionsRequestsHeaderTextView);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.connectionsDividerTwo);
                v.setVisibility(View.GONE);
            }

        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);

    }

    public void handlePendingOnPost(boolean success, String username) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

        if (success) {
            for (int i = 0; i < mPending.size(); i++) {
                String str = mPending.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    mPending.remove(i);
                    mPendingAdapter.notifyDataSetChanged();
                    break;
                }
            }

            if (mPending.isEmpty()) {
                TextView tv = getView().findViewById(R.id.connectionsPendingHeaderTextView);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.connectionsDividerThree);
                v.setVisibility(View.GONE);
            }
        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);
    }

    public void handleSearchOnPost() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);
    }

    public void handleEmptySearch() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);

        Toast.makeText(getActivity(), "Search yielded no results", Toast.LENGTH_LONG).show();
    }

    public void handleSearchForSelf() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);
        enableDisableViewGroup(vg, true);

        Toast.makeText(getActivity(), "I think you just searched for yourself...",
                Toast.LENGTH_LONG).show();
    }

    private void enableDisableViewGroup(ViewGroup vg, boolean enabled) {
        int children = vg.getChildCount();
        for (int i = 0; i < children; i++) {
            View v = vg.getChildAt(i);
            v.setEnabled(enabled);
            if (v instanceof ViewGroup) {
                enableDisableViewGroup((ViewGroup) v, enabled);
            }
        }

        if (enabled) {
            ProgressBar pg = getView().findViewById(R.id.connectionsProgressBar);
            pg.setVisibility(ProgressBar.GONE);
        } else {
            ProgressBar pg = getView().findViewById(R.id.connectionsProgressBar);
            pg.setVisibility(ProgressBar.VISIBLE);
        }
    }

    public void setError(String err) {
        Toast.makeText(getActivity(), "Request unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();
    }

    public void handleOnError(String e) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.connectionsFrameLayout);

        Toast.makeText(getActivity(), "Request unsuccessful for reason: " + e,
                Toast.LENGTH_SHORT).show();

        enableDisableViewGroup(vg, true);
    }

    public interface OnConnectionsInteractionListener {
        void onConnectionsInteractionListener(String username);
        void onRequestInteractionListener(String username, boolean accept, String fragment);
        void onSearchInteractionListener(String searchBy, String searchString,
                                         ArrayList<String> contacts, ArrayList<String> requests,
                                         ArrayList<String> pending);
    }

}
