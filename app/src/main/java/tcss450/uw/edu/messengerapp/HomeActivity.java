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
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.messengerapp.model.PullService;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ConnectionsFragment.OnConnectionsInteractionListener {

    private static final String TAG = "HomeActivity";
    private DataUpdateReciever mDataUpdateReceiver;
    public int mTotalNotifacations = 0;
    public int mChatNotifacations = 0;
    public int mNumConnectionNotifacations = 0;
    TextView chatNotifications, connectionNotifications, mNotificationsBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        if (savedInstanceState == null) {
            if (findViewById(R.id.homeFragmentContainer) != null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.homeFragmentContainer, new HomeFragment())
                        .commit();
            }
        }

        //Start the service to wait for messages from database.
        SharedPreferences sharedPreferences =
                getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.keys_sp_on), true);
        editor.apply();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new ChatManagerFragment());
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
        } else if (!drawer.isDrawerOpen(GravityCompat.START) && currentFragment instanceof HomeFragment) {
            super.onBackPressed();
        } else {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.homeFragmentContainer, new HomeFragment());
            transaction.commit();
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
        }

        android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.homeFragmentContainer, frag);
        transaction.commit();
    }

    public void loadChatActivity() {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_connections:
                loadFragment(new ConnectionsFragment());
                mNumConnectionNotifacations = 0;
                mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
                updateNotificationsUI();
                break;
            case R.id.nav_chat:
                loadChatActivity();
                //loadFragment(new ChatFragment());
                break;
            case R.id.nav_chatmanager:
                loadFragment(new ChatManagerFragment());
                mChatNotifacations = 0;
                //mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
                //updateNotificationsUI();
                break;
            case R.id.nav_weather:
                loadFragment(new WeatherFragment());
                break;
            case R.id.nav_home:
                loadFragment(new HomeFragment());
                mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
                //updateNotificationsUI();
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
        // Check to see if the service should aleardy be running
        if (sharedPreferences.getBoolean(getString(R.string.keys_sp_on), false)) {
            //stop the service from the background
            PullService.stopServiceAlarm(this);
            //restart but in the foreground
            PullService.startServiceAlarm(this, true);
        }

        //Look to see if the intent has a result string for us.
        //If true, then this Activity was started fro the notification bar
        if (getIntent().hasExtra(getString(R.string.keys_extra_results))) {

            loadFragment(new HomeFragment());
            mChatNotifacations = 0;
            mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
        }

        if (mDataUpdateReceiver == null) {
            mDataUpdateReceiver = new DataUpdateReciever();
        }
        IntentFilter iFilter = new IntentFilter(PullService.RECEIVED_UPDATE);
        registerReceiver(mDataUpdateReceiver, iFilter);

        mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
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
        if (mDataUpdateReceiver != null){
            unregisterReceiver(mDataUpdateReceiver);
        }

    }

    /**
     * Gets some information from all this data.
     * The input is expected to use the format based on the documentation found :
     * https://phishnet.api-docs.io/v3/setlists/get-a-random-phish-setlist
     *
     * @param jsonResult a JSON String
     * @return the data and location of the show
     */
    private String parseJson(final String jsonResult) {
        String result = "";

        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("error_code") && json.getInt("error_code") == 0) {
                if (json.has("response")) {
                    JSONObject response = json.getJSONObject("response");
                    if (response.has("data")) {
                        JSONObject show = response.getJSONArray("data").getJSONObject(0);
                        if (show.has("long_date")) {
                            result = show.getString("long_date");
                        }
                        if (show.has("location")) {
                            result += System.lineSeparator();
                            result += show.getString("location");
                            result += System.lineSeparator();
                        }
                    }
                }
            } else {
                result = "Error from web service";
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        return result;
    }

    private void updateNotificationsUI (){
        StringBuilder sb = new StringBuilder();
        if (mTotalNotifacations % 2 == 0) {
            mChatNotifacations++;
        } else {
            mNumConnectionNotifacations++;
        }
        sb.append("You have ");
        //if (notification == chat) mChatNotifacations++;
        //if (notification == connection) mConnectionNotifacations++;
        mTotalNotifacations = mChatNotifacations + mNumConnectionNotifacations;
        sb.append(mTotalNotifacations);
        if (mTotalNotifacations == 1) {
            sb.append(" notification");
        } else {
            sb.append(" notifications");
        }

        mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
//        mNotificationsBar.setText(sb.toString());

        if (mChatNotifacations > 0) {
            chatNotifications.setGravity(Gravity.CENTER_VERTICAL);
            chatNotifications.setTypeface(null, Typeface.BOLD);
            chatNotifications.setTextColor(getResources().getColor(R.color.colorAccent));
            chatNotifications.setText(String.valueOf(mChatNotifacations)); } else chatNotifications.setText("");
        if (mNumConnectionNotifacations > 0) {
            connectionNotifications.setGravity(Gravity.CENTER_VERTICAL);
            connectionNotifications.setTypeface(null, Typeface.BOLD);
            connectionNotifications.setTextColor(getResources().getColor(R.color.colorAccent));
            connectionNotifications.setText(String.valueOf(mNumConnectionNotifacations)); } else connectionNotifications.setText("");

    }

    @Override
    public void onConnectionsInteractionListener(String username) {

    }

    @Override
    public void onRequestInteractionListener(String username) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_verify_contact_request))
                .build();

        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        String myName = prefs.getString(getString(R.string.keys_prefs_username), "");

        JSONObject msg = new JSONObject();
        try {
            msg.put("usernameA", username);
            msg.put("usernameB", myName);
        } catch (JSONException e) {
            Log.wtf("Verify Contact", "Error reading JSON" + e.getMessage());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleVerifyRequestOnPre)
                .onPostExecute(this::handleVerifyRequestOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleVerifyRequestOnPre() {
        ConnectionsFragment frag = (ConnectionsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_connections));
        frag.handleVerifyRequestOnPre();
    }

    private void handleVerifyRequestOnPost(String result) {
        ConnectionsFragment frag = (ConnectionsFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_connections));

        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            String username = resultsJSON.getString("username");

            frag.handleVerifyRequestOnPost(success, username);

        } catch (JSONException e) {
            frag.setError("Something strange happened");
            frag.handleOnError(e.toString());
        }
    }

    //**********DATA UPDATE RECEIVER************//
    private class DataUpdateReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(PullService.RECEIVED_UPDATE)) {
                Log.d("UpdateReceiver", "hey I just got your broadcast!");
                mNotificationsBar = (TextView) findViewById(R.id.notifacationBar);
                updateNotificationsUI();
                //mResultStrings.add(intent.getStringExtra(getString(R.string.keys_extra_results)));

                //TO-DO handle notifacations properly (display red symbols on items?)
            }
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }


}
