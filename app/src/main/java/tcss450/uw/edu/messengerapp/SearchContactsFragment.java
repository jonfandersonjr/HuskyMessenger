package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public interface OnSearchFragmentInteractionListener {

    }

    private void onItemClickNewPeople(View v, int position) {

    }

    private void setUpNewPeople() {
        if (mNewPeople.isEmpty()) {
            return;
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

            mNewPeopleRecycler = getView().findViewById(R.id.searchConnectionsNewPeopleRecycler);
            mNewPeopleRecycler.setLayoutManager(layoutManager);

            mNewPeopleAdapter = new MyRecyclerViewAdapter(getActivity(), mNewPeople);
            mNewPeopleAdapter.setClickListener(this::onItemClickNewPeople);
            mNewPeopleRecycler.setAdapter(mNewPeopleAdapter);
        }
    }

    private void setUpContacts() {
        if (mContacts.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsCurrentConnectionsHeader);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider2);
            v.setVisibility(View.GONE);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

            mContactsRecycler = getView().findViewById(R.id.searchConnectionsConnectionsRecycler);
            mContactsRecycler.setLayoutManager(layoutManager);

            mContactsAdapter = new MyRecyclerViewAdapter(getActivity(), mContacts);
            mContactsAdapter.setClickListener(this::onItemClickNewPeople);
            mContactsRecycler.setAdapter(mContactsAdapter);
        }
    }

    private void setUpRequests() {
        if (mRequests.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsRequestHeaderText);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider3);
            v.setVisibility(View.GONE);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

            mRequestsRecycler = getView().findViewById(R.id.searchConnectionsRequestsRecycler);
            mRequestsRecycler.setLayoutManager(layoutManager);

            mRequestsAdapter = new MyRecyclerViewAdapter(getActivity(), mRequests);
            mRequestsAdapter.setClickListener(this::onItemClickNewPeople);
            mRequestsRecycler.setAdapter(mRequestsAdapter);
        }
    }

    private void setUpPending() {
        if (mPending.isEmpty()) {
            TextView tv = getView().findViewById(R.id.searchConnectionsPendingHeaderText);
            tv.setVisibility(TextView.GONE);

            View v = getView().findViewById(R.id.searchConnectionsDivider4);
            v.setVisibility(View.GONE);
        } else {
            LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

            mPendingRecycler = getView().findViewById(R.id.searchConnectionsPendingRecycler);
            mPendingRecycler.setLayoutManager(layoutManager);

            mPendingAdapter = new MyRecyclerViewAdapter(getActivity(), mPending);
            mPendingAdapter.setClickListener(this::onItemClickNewPeople);
            mPendingRecycler.setAdapter(mPendingAdapter);
        }
    }

    public interface onSearchConnectionsFragmentInteraction


}
