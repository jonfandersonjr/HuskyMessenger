package tcss450.uw.edu.messengerapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import tcss450.uw.edu.messengerapp.model.MyRecyclerViewAdapter;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchContactsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SearchContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchContactsFragment extends Fragment {

    private OnSearchFragmentInteractionListener mListener;

    private ArrayList<String> mContacts;
    private ArrayList<String> mRequests;
    private ArrayList<String> mPending;
    private ArrayList<String> mNewPeople;

    private RecyclerView mNewPeopleRecycler;
    private RecyclerView mContactsRecycler;
    private RecyclerView mRequestsRecycler;
    private RecyclerView mPendingRecycler;

    private MyRecyclerViewAdapter mNewPeopleAdapter;
    private MyRecyclerViewAdapter mContactsAdapter;
    private MyRecyclerViewAdapter mRequestsAdapter;
    private MyRecyclerViewAdapter mPendingAdapter;

    public SearchContactsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mContacts = bundle.getStringArrayList("contacts");
            mRequests = bundle.getStringArrayList("requests");
            mPending = bundle.getStringArrayList("pending");
            mNewPeople = bundle.getStringArrayList("newPeople");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_contacts, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        setUpNewPeople();
        setUpContacts();
        setUpRequests();
        setUpPending();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSearchFragmentInteractionListener) {
            mListener = (OnSearchFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSearchFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private void onItemClickNewPeople(View v, int position) {
        String str = mNewPeopleAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "Would you like to add " + username + " as a connection?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "Would you like to add Haylee Ryan as a connection?";
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Add as Connection").setMessage(msg)
                .setNegativeButton(getString(R.string.searchConnections_Yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mListener.onSearchAddInteraction(username);
                            }
                        })
                .setPositiveButton(getString(R.string.searchConnections_Nah),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
        if (ryan) {
            builder.setIcon(R.drawable.ryan);
        } else {
            builder.setIcon(R.drawable.contact);
        }
        builder.show();
    }

    private void onItemClickRequests(View v, int position) {
        String str = mRequestsAdapter.getItem(position);
        String fragment = "search";
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "Would you like to accept " + username
                + "'s connection request?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "Would you like to accept Haylee Ryan's connection request?";
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Resolve Request").setMessage(msg)
                .setPositiveButton(getString(R.string.connections_decline_request_diaglog_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean accept = false;
                        mListener.onSearchRequestInteraction(username, accept, fragment);
                    }
                })
                .setNegativeButton(getString(R.string.connections_accept_request_dialog_button),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                boolean accept = true;
                                mListener.onSearchRequestInteraction(username, accept, fragment);
                            }
                        });
        if (ryan) {
            builder.setIcon(R.drawable.ryan);
        } else {
            builder.setIcon(R.drawable.contactrequest);
        }
        builder.show();

    }

    private void onItemClickPending(View v, int position) {
        String str = mPendingAdapter.getItem(position);
        final String username = str.substring(0, str.indexOf(" "));
        String[] arr = str.split(" ");
        boolean ryan = false;
        String msg = "Would you like to cancel your connection request to " +
                username + "?";
        if (arr[1].equals("(Ryan,") && arr[2].equals("Haylee)")) {
            ryan = true;
            msg = "Would you like to cancel your connection request to Haylee Ryan?";
        }

        AlertDialog.Builder builder;
        builder = new AlertDialog
                .Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        builder.setTitle("Remove Pending Request")
                .setMessage(msg)
                .setNegativeButton(getString(R.string.searchConnections_remove_pending),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        builder.setTitle("Confirm Cancellation")
                                .setMessage("Are you sure you want to cancel your connection request to " +
                                        username + "?")
                                .setNegativeButton(getString(R.string.searchConnections_Yes),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        boolean accept = false;
                                        final String frag = "searchPending";
                                        mListener.onSearchRequestInteraction(username, accept, frag);
                                    }
                                })
                                .setPositiveButton(getString(R.string.searchConnections_Nah),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                            }
                                        })
                                .setIcon(R.drawable.alert)
                                .show();
                    }
                })
                .setPositiveButton(getString(R.string.searchConnections_nevermind),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
        if (ryan) {
            builder.setIcon(R.drawable.ryan);
        } else {
            builder.setIcon(R.drawable.delete);
        }
        builder.show();
    }

    private void setUpNewPeople() {
        if (mNewPeople.isEmpty()) {
            return;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mNewPeopleRecycler = getView().findViewById(R.id.searchConnectionsNewPeopleRecycler);
        mNewPeopleRecycler.setLayoutManager(layoutManager);

        mNewPeopleAdapter = new MyRecyclerViewAdapter(getActivity(), mNewPeople);
        mNewPeopleAdapter.setClickListener(this::onItemClickNewPeople);
        mNewPeopleRecycler.setAdapter(mNewPeopleAdapter);

    }

    private void setUpContacts() {
        if (mContacts.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsCurrentConnectionsHeader);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider2);
            v.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mContactsRecycler = getView().findViewById(R.id.searchConnectionsConnectionsRecycler);
        mContactsRecycler.setLayoutManager(layoutManager);

        mContactsAdapter = new MyRecyclerViewAdapter(getActivity(), mContacts);
        //mContactsAdapter.setClickListener(this::onItemClickNewPeople);
        mContactsRecycler.setAdapter(mContactsAdapter);

    }

    private void setUpRequests() {
        if (mRequests.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsRequestHeaderText);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider3);
            v.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mRequestsRecycler = getView().findViewById(R.id.searchConnectionsRequestsRecycler);
        mRequestsRecycler.setLayoutManager(layoutManager);

        mRequestsAdapter = new MyRecyclerViewAdapter(getActivity(), mRequests);
        mRequestsAdapter.setClickListener(this::onItemClickRequests);
        mRequestsRecycler.setAdapter(mRequestsAdapter);

    }

    private void setUpPending() {
        if (mPending.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsPendingHeaderText);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider4);
            v.setVisibility(View.GONE);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        mPendingRecycler = getView().findViewById(R.id.searchConnectionsPendingRecycler);
        mPendingRecycler.setLayoutManager(layoutManager);

        mPendingAdapter = new MyRecyclerViewAdapter(getActivity(), mPending);
        mPendingAdapter.setClickListener(this::onItemClickPending);
        mPendingRecycler.setAdapter(mPendingAdapter);

    }

    public void handleRequestOnPre() {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);
        enableDisableViewGroup(vg, false);
    }

    public void handleRequestOnPost(boolean success, String username, boolean accept) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

        if (success) {
            for (int i = 0; i < mRequests.size(); i++) {
                String str = mRequests.get(i);
                String subStr = str.substring(0, str.indexOf(" "));

                if (username.equals(subStr)) {
                    String[] arr = str.split(" ");
                    String verified = arr[0] + " " + arr[1] + " " + arr[2];

                    if (accept) {
                        mContacts.add(verified);
                        mContacts.sort(String::compareToIgnoreCase);
                        mContactsAdapter.notifyDataSetChanged();
                    }
                    mRequests.remove(i);
                    mRequestsAdapter.notifyDataSetChanged();
                    break;
                }
            }

            if (mRequests.isEmpty()) {
                TextView tv = getView().findViewById(R.id.searchConnectionsRequestHeaderText);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.searchConnectionsDivider3);
                v.setVisibility(View.GONE);
            }
        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);

    }

    public void handlePendingOnPost(boolean success, String username) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

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
                TextView tv = getView().findViewById(R.id.searchConnectionsPendingHeaderText);
                tv.setVisibility(TextView.GONE);

                View v = getView().findViewById(R.id.searchConnectionsDivider4);
                v.setVisibility(View.GONE);
            }
        } else {
            setError("Something happened on the back end I think...");
        }

        enableDisableViewGroup(vg, true);
    }

    public void handleAddOnPost(String username) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

        for (int i = 0; i < mNewPeople.size(); i++) {
            String str = mNewPeople.get(i);
            String subStr = str.substring(0, str.indexOf(" "));

            if (username.equals(subStr)) {
                String[] arr = str.split(" ");
                String verified = arr[0] + " " + arr[1] + " " + arr[2];

                mPending.add(verified);
                mPending.sort(String::compareToIgnoreCase);

                TextView tv = getView().findViewById(R.id.searchConnectionsPendingHeaderText);
                View v = getView().findViewById(R.id.searchConnectionsDivider4);

                if (tv.getVisibility() == View.GONE) {
                    tv.setVisibility(TextView.VISIBLE);
                    v.setVisibility(View.VISIBLE);
                }

                mPendingAdapter.notifyDataSetChanged();

                mNewPeople.remove(i);
                mNewPeopleAdapter.notifyDataSetChanged();
                break;
            }
        }

        enableDisableViewGroup(vg, true);

    }

    public void setError(String err) {
        Toast.makeText(getActivity(), "Request unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();
    }

    public void handleOnError(String e) {
        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.searchFrameLayout);

        Toast.makeText(getActivity(), "Request unsuccessful for reason: " + e,
                Toast.LENGTH_SHORT).show();

        enableDisableViewGroup(vg, true);
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
            ProgressBar pg = getView().findViewById(R.id.searchConnectionsProgressBar);
            pg.setVisibility(ProgressBar.GONE);
        } else {
            ProgressBar pg = getView().findViewById(R.id.searchConnectionsProgressBar);
            pg.setVisibility(ProgressBar.VISIBLE);
        }
    }

    public interface OnSearchFragmentInteractionListener {
        void onSearchRequestInteraction(String username, boolean accept, String fragment);
        void onSearchAddInteraction(String username);
    }


}
