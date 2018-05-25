package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tcss450.uw.edu.messengerapp.ChatFragment;
import tcss450.uw.edu.messengerapp.ChatManagerFragment;
import tcss450.uw.edu.messengerapp.ConnectionsFragment;
import tcss450.uw.edu.messengerapp.HomeFragment;
import tcss450.uw.edu.messengerapp.R;
import tcss450.uw.edu.messengerapp.WeatherFragment;

public class ChatActivity extends AppCompatActivity {
    Bundle extras;
    String mUsername;
    String mChatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);

        //FIX WHERE IT GETS CHAT ID FROM

        extras = getIntent().getExtras();
        String value = "1";
        if (extras != null) {
            value = extras.getString("CHAT_ID");
            mChatId = value;
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
            Log.i("THE CHAT", "ID IS " + chatid);
        }

        if (savedInstanceState == null) {
            if (findViewById(R.id.chatContainer) != null) {
                loadFragment(new ChatFragment(value));
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
//        AlertDialog.Builder adb = new AlertDialog.Builder(this);
//        CharSequence items[] = new CharSequence[] {"First", "Second", "Third"};
//        adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface d, int n) {
//                // ...
//            }
//
//        });
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Who do you wan't to remove?");
        alertDialog.setMessage("Enter the Username");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);
        //alertDialog.setIcon(R.drawable.key);

        alertDialog.setPositiveButton("Remove",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mUsername = input.getText().toString();
                        //removeFromChat();
                        addToChat();
//                        password = input.getText().toString();
//                        if (password.compareTo("") == 0) {
//                            if (pass.equals(password)) {
//                                Toast.makeText(getApplicationContext(),
//                                        "Password Matched", Toast.LENGTH_SHORT).show();
//                                Intent myIntent1 = new Intent(view.getContext(),
//                                        Show.class);
//                                startActivityForResult(myIntent1, 0);
//                            } else {
//                                Toast.makeText(getApplicationContext(),
//                                        "Wrong Password!", Toast.LENGTH_SHORT).show();
//                            }
//                        }
                    }
                });

//        alertDialog.setNegativeButton("NO",
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });


        //}

//);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.chatFab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.show();

//                adb.setNegativeButton("Cancel", null);
//                adb.setTitle("Which one?");
//                adb.show();
                //loadFragment(new StartChatFragment());
            }
        });

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

    public void removeFromChat() {
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        //mUsername = "test1";
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
            msg.put("chatId",mChatId);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }
        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("removeUserFromChat")
                .build();

        Log.e("CONTENT", retrieveRequests.toString());

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(retrieveRequests.toString(), msg)
                .onPreExecute(this::handleRemoveOnPre)
                .onPostExecute(this::publishRequests)
                .onCancelled(this::handleError)
                .build().execute();


    }

    public void handleRemoveOnPre() {

    }

    private void handleError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

    public void publishRequests(String result) {
        try {
            JSONObject requests = new JSONObject(result);
            boolean success = requests.getBoolean("success");
            if (success) {
                Log.e("CHAT","USER SUCCESFULLY DELETED");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.e("HOW MANY CHATS", ""+mChatnames.size());
    }
    public void addToChat() {
        SharedPreferences prefs = this.getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }

        //mUsername = "test1";
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", mUsername);
            msg.put("chatId",mChatId);
        } catch (JSONException e) {
            Log.wtf("JSON EXCEPTION", e.toString());
        }
        Uri retrieveRequests = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath("addUserToChat")
                .build();

        Log.e("CONTENT", retrieveRequests.toString());

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(retrieveRequests.toString(), msg)
                .onPreExecute(this::handleAddingOnPre)
                .onPostExecute(this::publishAddingRequests)
                .onCancelled(this::handleError)
                .build().execute();


    }

    public void handleAddingOnPre() {

    }

    private void handleAddingError(String e) {
        Log.e("LISTEN ERROR!!!", e);
    }

    public void publishAddingRequests(String result) {
        try {
            JSONObject requests = new JSONObject(result);
            boolean success = requests.getBoolean("success");
            if (success) {
                Log.e("CHAT","USER SUCCESFULLY ADDED");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //Log.e("HOW MANY CHATS", ""+mChatnames.size());
    }







}
