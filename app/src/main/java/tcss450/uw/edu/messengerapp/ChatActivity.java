package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import tcss450.uw.edu.messengerapp.ChatFragment;
import tcss450.uw.edu.messengerapp.ChatManagerFragment;
import tcss450.uw.edu.messengerapp.ConnectionsFragment;
import tcss450.uw.edu.messengerapp.HomeFragment;
import tcss450.uw.edu.messengerapp.R;
import tcss450.uw.edu.messengerapp.WeatherFragment;

public class ChatActivity extends AppCompatActivity {
    Bundle extras;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //FIX WHERE IT GETS CHAT ID FROM

        extras = getIntent().getExtras();
        String value = "1";
        if (extras != null) {
             value = extras.getString("CHAT_ID");
            Log.e("VALUE", value);
        } else {
            SharedPreferences prefs = getSharedPreferences(
                    getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);
            if (!prefs.contains("chatid")) {
                throw new IllegalStateException("No chatid in prefs!");
            }
            String chatid;
            chatid = prefs.getString("chatid", "");
            Log.i("THE CHAT" , "ID IS "+ chatid);
        }

        if(savedInstanceState == null) {
            if (findViewById(R.id.chatContainer) != null) {
                loadFragment(new ChatFragment(value));
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    public void loadFragment(Fragment theFragment) {
        theFragment.setArguments(extras);
        FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.chatContainer, theFragment);
        // Commit the transaction
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
