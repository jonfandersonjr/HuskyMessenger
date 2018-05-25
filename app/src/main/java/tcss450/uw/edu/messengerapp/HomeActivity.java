package tcss450.uw.edu.messengerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import tcss450.uw.edu.messengerapp.model.PullService;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ConnectionsFragment.OnConnectionsInteractionListener,
        SearchContactsFragment.OnSearchFragmentInteractionListener {

    private static final String TAG = "HomeActivity";

    private MessageUpdateReceiver mMessagesUpdateReceiver;
    private ConnectionUpdateReceiver mConnectionsUpdateReceiver;
    private String mUsername;
    private String mDeleteConnectionUsername;

    private ArrayList<String> mIncomingMessages = new ArrayList<>();
    private ArrayList<String> mIncomingConnectionRequests = new ArrayList<>();
    private ArrayList<String> mContacts, mRequests, mPending;

    private TextView chatNotifications, connectionNotifications, mNotificationsBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Bundle bundle = getIntent().getExtras();

        if (savedInstanceState == null) {
            if (findViewById(R.id.homeFragmentContainer) != null && bundle == null) {
                loadFragment(new HomeFragment());
            }
        }

        //Start the service to wait for messages from database.
        SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.keys_sp_on), true);
        editor.putString("chatid", "1"); //defeault chatid
        editor.apply();

        mUsername = sharedPreferences.getString("username", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new StartChatFragment());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        chatNotifications = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_chatmanager));
        connectionNotifications = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.nav_connections));
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.homeFragmentContainer);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (!drawer.isDrawerOpen(GravityCompat.START)
                    && ((currentFragment instanceof HomeFragment)
                    || currentFragment instanceof SearchContactsFragment)) {
            super.onBackPressed();
        } else {
            loadFragment(new HomeFragment());
            updateNotificationsUI();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case (R.id.color_rugged):
                break;
            case (R.id.color_modern):
                //swap
                break;
            case (R.id.color_summer):
                //swap
                break;
            case (R.id.color_UW):
                //swap
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment frag) {
        if (frag instanceof ConnectionsFragment) {
            String tag = getString(R.string.keys_fragment_connections);

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.homeFragmentContainer, frag, tag);
            transaction.commit();
        } else {

            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.homeFragmentContainer, frag);
            transaction.commit();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_connections:
                loadFragment(new ConnectionsFragment());
                mIncomingConnectionRequests.clear();
                updateNotificationsUI();
                break;
            case R.id.nav_chatmanager:
                loadFragment(new ChatManagerFragment());
                mIncomingMessages.clear();
                updateNotificationsUI();
                break;
            case R.id.nav_weather:
                loadFragment(new WeatherFragment());
                break;
            case R.id.nav_home:
                loadFragment(new HomeFragment());
                mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
                break;
            case R.id.nav_logout:
                SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
                prefs.edit().remove(getString(R.string.keys_prefs_username));
                prefs.edit().putBoolean(getString(R.string.keys_prefs_stay_logged_in), false).apply();
                PullService.stopServiceAlarm(this);
                finishAndRemoveTask();
                break;
            default:
                Log.wtf("ISSUE:", "Problem in home activity item selected");
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        // Check to see if the service should already be running
        if (sharedPreferences.getBoolean(getString(R.string.keys_sp_on), false)) {
            //stop the service from the background
            PullService.stopServiceAlarm(this);
            //restart but in the foreground
            PullService.startServiceAlarm(this, true);

            PullService.setUsername(mUsername);

        }

        //Look to see if the intent has a result string for us.
        //If true, then this Activity was started from the notification bar
        if (getIntent().hasExtra(getString(R.string.keys_chat_notification))) {
            //load new chat activity with this person
            loadFragment(new ChatManagerFragment());
            mIncomingMessages.clear();
            updateNotificationsUI();
        } else if (getIntent().hasExtra(getString(R.string.keys_connection_notification))) {
            loadFragment(new ConnectionsFragment());
            mIncomingConnectionRequests.clear();
            updateNotificationsUI();
        }

        if (mMessagesUpdateReceiver == null) {
            mMessagesUpdateReceiver = new MessageUpdateReceiver();
        }
        if (mConnectionsUpdateReceiver == null) {
            mConnectionsUpdateReceiver = new ConnectionUpdateReceiver();
        }
        IntentFilter iFilter = new IntentFilter(PullService.MESSAGE_UPDATE);
        registerReceiver(mMessagesUpdateReceiver, iFilter);

        IntentFilter iFilter2 = new IntentFilter(PullService.CONNECTION_UPDATE);
        registerReceiver(mConnectionsUpdateReceiver, iFilter2);

        updateNotificationsUI();
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(getString(R.string.keys_sp_on), false)) {
            //stop the service from the foreground
            PullService.stopServiceAlarm(this);
            //restart but in the background
            PullService.startServiceAlarm(this, false);
        }
        if (mMessagesUpdateReceiver != null){
            unregisterReceiver(mMessagesUpdateReceiver);
        }
        if (mConnectionsUpdateReceiver != null) {
            unregisterReceiver(mConnectionsUpdateReceiver);
        }

    }

    @Override
    public void onSearchAddInteraction(String username) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_request_contact))
                .build();

        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        String myName = prefs.getString(getString(R.string.keys_prefs_username), "");

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", myName);
            msg.put("usernameB", username);
        } catch (JSONException e) {
            Log.wtf("SearchAddInteraction", "Error reading JSON" + e.getMessage());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleSearchRequestOnPre)
                .onPostExecute(this::handleSearchAddOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    @Override
    public void onRequestInteractionListener(String username, boolean accept, String fragment) {
        String endpoint;

        if (accept) {
            endpoint = getString(R.string.ep_verify_contact_request);
        } else {
           endpoint = getString(R.string.ep_decline_contact_request);
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(endpoint)
                .build();

        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        String myName = prefs.getString(getString(R.string.keys_prefs_username), "");

        JSONObject msg = new JSONObject();
        if (!fragment.equals("searchPending") && !fragment.equals("connectionsPending")) {
            try {
                msg.put("usernameA", username);
                msg.put("usernameB", myName);
            } catch (JSONException e) {
                Log.wtf("Verify Contact", "Error reading JSON" + e.getMessage());
            }
        } else {
            try {
                msg.put("usernameB", username);
                msg.put("usernameA", myName);
            } catch (JSONException e) {
                Log.wtf("Verify Contact", "Error reading JSON" + e.getMessage());
            }
        }

        if (fragment.equals("connections")) {
            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleRequestOnPre)
                    .onPostExecute(this::handleRequestOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        } else if (fragment.equals("search")) {
            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleSearchRequestOnPre)
                    .onPostExecute(this::handleSearchRequestOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        } else if (fragment.equals("searchPending")) {
            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleSearchRequestOnPre)
                    .onPostExecute(this::handleSearchPendingOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        } else if (fragment.equals("connectionsPending")) {
            new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                    .onPreExecute(this::handleRequestOnPre)
                    .onPostExecute(this::handlePendingOnPost)
                    .onCancelled(this::handleErrorsInTask)
                    .build().execute();
        }

    }

    @Override
    public void onSearchRequestInteraction(String username, boolean accept, String fragment) {
        onRequestInteractionListener(username, accept, fragment);
    }

    @Override
    public void onSearchInteractionListener(String searchBy, String searchString,
                                            ArrayList<String> contacts, ArrayList<String> requests,
                                            ArrayList<String> pending) {
        mContacts = contacts;
        mRequests = requests;
        mPending = pending;

        searchString = searchString.toUpperCase();
        String endpoint;

        if (searchBy.equals("firstname")) {
            endpoint = getString(R.string.ep_get_credentials_first);
        } else if (searchBy.equals("lastname")) {
            endpoint = getString(R.string.ep_get_credentials_last);
        } else if (searchBy.equals("username")) {
            endpoint = getString(R.string.ep_get_credentials_username);
        } else {
            endpoint = getString(R.string.ep_get_credentials_email);
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(endpoint)
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put(searchBy, searchString);
        } catch (JSONException e) {
            Log.wtf("Search Interaction", "Error reading JSON" + e.getMessage());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleRequestOnPre)
                .onPostExecute(this::handleSearchOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    public void onConnectionsDeleteInteractionListener(String username) {

        mDeleteConnectionUsername = username;

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_contacts))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
        } catch (JSONException e) {
            Log.wtf("Connections Delete json message", "Error reading JSON" +
                    e.getMessage());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleRequestOnPre)
                .onPostExecute(this::handleContactsOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleRequestOnPre() {
        ConnectionsFragment frag = (ConnectionsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_connections));
        frag.handleRequestOnPre();
    }

    private void handleSearchRequestOnPre() {
        SearchContactsFragment frag = (SearchContactsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_searchConnections));
        frag.handleRequestOnPre();
    }

    private void handleRequestOnPost(String result) {
        ConnectionsFragment frag = (ConnectionsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_connections));

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            String username = resultsJSON.getString("username");
            boolean accept = resultsJSON.getBoolean("accept");
            if (success) {
                frag.handleRequestOnPost(success, username, accept);
            } else {
                Toast.makeText(this, "Your action for contact request was not valid",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    private void handlePendingOnPost(String result) {
        ConnectionsFragment frag = (ConnectionsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_connections));

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            String username = resultsJSON.getString("usernameB");

            if (success) {
                frag.handlePendingOnPost(success, username);
            } else {
                Toast.makeText(this, "Your action for contact request was not valid",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    private void handleSearchRequestOnPost(String result) {
        SearchContactsFragment frag = (SearchContactsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_searchConnections));

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            String username = resultsJSON.getString("username");
            boolean accept = resultsJSON.getBoolean("accept");

            if (success) {
                frag.handleRequestOnPost(success, username, accept);
            } else {
                Toast.makeText(this, "Your action for contact request was not valid",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    private void handleSearchPendingOnPost(String result) {
        SearchContactsFragment frag = (SearchContactsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_searchConnections));

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            String username = resultsJSON.getString("usernameB");

            if (success) {
                frag.handlePendingOnPost(success, username);
            } else {
                Toast.makeText(this, "Your action for contact request was not valid",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    private void handleSearchAddOnPost(String result) {
        SearchContactsFragment frag = (SearchContactsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_searchConnections));

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            String username = resultsJSON.getString("username");

            if (success) {
                frag.handleAddOnPost(username);
            } else {
                frag.handleOnError("WTF!");
            }
        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    private void handleSearchOnPost(String result) {
        ConnectionsFragment frag = (ConnectionsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_connections));

        boolean inContacts;
        boolean inRequests;
        boolean inPending;

        ArrayList<String> newPeople = new ArrayList<String>();
        ArrayList<String> contactList = new ArrayList<String>();
        ArrayList<String> requestList = new ArrayList<String>();
        ArrayList<String> pendingList = new ArrayList<String>();

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_results))) {
                    try {
                        JSONArray jRes = resultsJSON
                                .getJSONArray(getString(R.string.keys_json_results));
                        if (jRes.length() == 0) {
                            frag.handleEmptySearch();
                            return;
                        } else {
                            for (int i = 0; i < jRes.length(); i++) {
                                JSONObject res = jRes.getJSONObject(i);
                                String username = res.getString(getString(R.string.keys_json_username));
                                String firstname = res.getString(getString(R.string.keys_json_requests_firstname));
                                String lastname = res.getString(getString(R.string.keys_json_requests_lastname));
                                String str = username + " (" + lastname + ", " + firstname + ")";

                                inContacts = searchName(username, mContacts);
                                inRequests = searchName(username, mRequests);
                                inPending = searchName(username, mPending);

                                if (!username.equals(mUsername)) {

                                    if (inContacts) {
                                        contactList.add(str);
                                    } else if (inRequests) {
                                        requestList.add(str);
                                    } else if (inPending) {
                                        Toast.makeText(this, "inPending", Toast.LENGTH_LONG);
                                        pendingList.add(str);
                                    } else {
                                        newPeople.add(str);
                                    }
                                }

                            }

                            if (contactList.isEmpty() && requestList.isEmpty() &&
                                    pendingList.isEmpty() && newPeople.isEmpty()) {
                                frag.handleSearchForSelf();
                                return;
                            }

                            frag.handleSearchOnPost();

                            Bundle extras = new Bundle();
                            extras.putStringArrayList("contacts", contactList);
                            extras.putStringArrayList("requests", requestList);
                            extras.putStringArrayList("pending", pendingList);
                            extras.putStringArrayList("newPeople", newPeople);

                            SearchContactsFragment fragment = new SearchContactsFragment();
                            fragment.setArguments(extras);

                            getSupportFragmentManager().beginTransaction()
                                    .setCustomAnimations(R.anim.enter, R.anim.exit,
                                            R.anim.pop_enter, R.anim.pop_exit)
                                    .replace(R.id.homeFragmentContainer, fragment,
                                            getString(R.string.keys_fragment_searchConnections))
                                    .addToBackStack(null).commit();
                            getSupportFragmentManager().executePendingTransactions();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        frag.handleErrorsInTask(e.toString());
                        frag.setError(e.toString());
                    }
                }
            }
        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    private void handleContactsOnPost(String result) {
        boolean listA = false;

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                if (resultsJSON.has(getString(R.string.keys_json_connections_a))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_connections_a));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject obj = jReqs.getJSONObject(i);
                            String username = obj.get(getString(R.string.keys_json_username))
                                    .toString();
                            if (username.equals(mDeleteConnectionUsername)) {
                                listA = true;
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (resultsJSON.has(getString(R.string.keys_json_connections_b))) {
                    try {
                        JSONArray jReqs = resultsJSON.getJSONArray(getString(R.string.keys_json_connections_b));
                        for (int i = 0; i < jReqs.length(); i++) {
                            JSONObject obj = jReqs.getJSONObject(i);
                            String username = obj.get(getString(R.string.keys_json_username))
                                    .toString();
                            if (username.equals(mDeleteConnectionUsername)) {
                                listA = false;
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.wtf("Get Contacts in handleContactsOnPost", "Back end screw up");
            }
        } catch (JSONException e) {
            Log.e("JSON PARSE ERROR", e.getMessage());
        }

        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_decline_contact_request))
                .build();

        JSONObject msg = new JSONObject();
        if (listA) {
            try {
                msg.put("usernameA", mDeleteConnectionUsername);
                msg.put("usernameB", mUsername);
            } catch (JSONException e) {
                Log.e("JSON put message error", e.getMessage());
            }
        } else {
            try {
                msg.put("usernameA", mUsername);
                msg.put("usernameB", mDeleteConnectionUsername);
            } catch (JSONException e) {
                Log.e("JSON put message error", e.getMessage());
            }
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleRequestOnPre)
                .onPostExecute(this::handleContactDeletedOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    private void handleContactDeletedOnPost(String result) {
        ConnectionsFragment frag = (ConnectionsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_connections));
        String username;

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            String usernameA = resultsJSON.getString("username");
            String usernameB = resultsJSON.getString("usernameB");


            if (usernameA.equals(mUsername)) {
                username = usernameB;
            } else {
                username = usernameA;
            }

            if (success) {
                frag.handleContactDeletedOnPost(success, username);
            } else {
                Toast.makeText(this, "Your action for contact request was not valid",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    private boolean searchName(String username, ArrayList<String> existingList) {
        int low = 0;
        int high = existingList.size() - 1;
        int mid;

        while (low <= high) {
            mid = (low + high) / 2;
            String name = existingList.get(mid);
            String substr = name.substring(0, name.indexOf(" "));

            if (substr.compareToIgnoreCase(username) < 0) {
                low = mid + 1;
            } else if (substr.compareToIgnoreCase(username) > 0) {
                high = mid - 1;
            } else {
                return true;
            }

        }

        return false;
    }


    private void updateNotificationsUI (){

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.homeFragmentContainer);

        if (currentFragment instanceof HomeFragment) {
            StringBuilder sb = new StringBuilder();
            if (!mIncomingMessages.isEmpty()) {
                for (String s : mIncomingMessages) {
                    sb.append(s);
                    if (mIncomingMessages.indexOf(s) < mIncomingMessages.size() - 1)
                        sb.append(", ");
                    if (mIncomingMessages.indexOf(s) == mIncomingMessages.size() - 2)
                        sb.append("and ");
                    if (mIncomingMessages.indexOf(s) == mIncomingMessages.size() - 1)
                        sb.append(" sent a new a message!");
                }
            }
            if (!mIncomingConnectionRequests.isEmpty()) {
                sb.append(" ");
                for (String s : mIncomingConnectionRequests) {
                    sb.append(s);
                    if (mIncomingConnectionRequests.indexOf(s) < mIncomingConnectionRequests.size() - 1)
                        sb.append(", ");
                    if (mIncomingConnectionRequests.indexOf(s) == mIncomingConnectionRequests.size() - 2)
                        sb.append("and ");
                    if (mIncomingConnectionRequests.indexOf(s) == mIncomingConnectionRequests.size() - 1)
                        sb.append(" sent you a connection request!");
                }
            } else if (mIncomingMessages.isEmpty() && mIncomingConnectionRequests.isEmpty()) {
                sb.append("You have no new notifications");
            }

            mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
            mNotificationsBar.setText(sb.toString());
        }

        if (!mIncomingMessages.isEmpty()) {
            chatNotifications.setGravity(Gravity.CENTER_VERTICAL);
            chatNotifications.setTypeface(null, Typeface.BOLD);
            chatNotifications.setTextColor(getResources().getColor(R.color.colorAccent));
            chatNotifications.setText(String.valueOf(mIncomingMessages.size())); } else chatNotifications.setText("");
        if (!mIncomingConnectionRequests.isEmpty()) {
            connectionNotifications.setGravity(Gravity.CENTER_VERTICAL);
            connectionNotifications.setTypeface(null, Typeface.BOLD);
            connectionNotifications.setTextColor(getResources().getColor(R.color.colorAccent));
            connectionNotifications.setText(String.valueOf(mIncomingConnectionRequests.size())); } else connectionNotifications.setText("");

        //mIncomingMessages.clear();
        //mIncomingConnectionRequests.clear();

    }


    //**********NOTIFICATION RECEIVERS************//
    private class MessageUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PullService.MESSAGE_UPDATE)) {
                String s = intent.getStringExtra(getString(R.string.keys_extra_results));
                if (!mIncomingMessages.contains(s)) {
                    mIncomingMessages.add(s);
                }
                updateNotificationsUI();
            }
        }
    }
    private class ConnectionUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String s = intent.getStringExtra("newConnect");
            if (intent.getAction().equals(PullService.CONNECTION_UPDATE)) {
                if (!mIncomingConnectionRequests.contains(s)) {
                    if (s != null) mIncomingConnectionRequests.add(s);
                }
                updateNotificationsUI();
            }
        }
    }

}
