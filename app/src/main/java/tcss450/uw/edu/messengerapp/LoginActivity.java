package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity
        implements LoginFragment.OnLoginFragmentInteractionListener,
        RegisterFragment.OnRegisterFragmentInteractionListener {

    private instructor.tcss450.uw.edu.messengerapp.model.Credentials mCredentials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (!isTaskRoot()) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            if (findViewById(R.id.loginFragmentContainer) != null) {
                SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

                if (prefs.getBoolean(getString(R.string.keys_prefs_stay_logged_in), false)) {
                    loadHomePage();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.loginFragmentContainer, new LoginFragment(),
                                    getString(R.string.keys_fragment_login))
                            .commit();
                }
            }
        }
    }

    private void checkStayLoggedIn() {
        if (((CheckBox) findViewById(R.id.logCheckBox)).isChecked()) {
            SharedPreferences prefs =
                    getSharedPreferences(getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            //save the username for later usage
            prefs.edit().putString(getString(R.string.keys_prefs_username),
                    mCredentials.getUsername()).apply();
            //save the users "want" to stay logged in
            prefs.edit().putBoolean(getString(R.string.keys_prefs_stay_logged_in), true).apply();
        }
    }

    private void loadHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @Override
    public void onRegisterButtonInteraction() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit)
                .replace(R.id.loginFragmentContainer, new RegisterFragment(), getString(R.string.keys_fragment_register))
                .addToBackStack(null).commit();
        getSupportFragmentManager().executePendingTransactions();
    }

    @Override
    public void onLoginButtonInteraction(instructor.tcss450.uw.edu.messengerapp.model.Credentials credentials) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();

        //build the JSONObject
        JSONObject msg = credentials.asJSONObject();

        mCredentials = credentials;

        //instantiate and execute the AsyncTask
        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleLoginOnPre)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    @Override
    public void onSubmitButtonInteraction(instructor.tcss450.uw.edu.messengerapp.model.Credentials credentials) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .build();

        //build the JSONObject
        JSONObject msg = credentials.asJSONObject();

        mCredentials = credentials;

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleRegisterOnPre)
                .onPostExecute(this::handleRegisterOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();


//        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
//        startActivity(intent);
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    private void handleRegisterOnPre() {
        RegisterFragment frag = (RegisterFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_register));
        frag.handleOnPre();
    }

    private void handleLoginOnPre() {
        LoginFragment frag = (LoginFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_login));
        frag.handleOnPre();
    }

    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                loadHomePage();
            } else {
                RegisterFragment frag = (RegisterFragment) getSupportFragmentManager()
                        .findFragmentByTag(getString(R.string.keys_fragment_register));
                frag.setError("Register was unsuccessful");
                frag.handleOnError();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                checkStayLoggedIn();
                //Login was successful. Switch to the loadDisplayFragment
                loadHomePage();
            } else {
                //Login was unsuccessful. Don't switch fragments and inform user
                LoginFragment frag = (LoginFragment) getSupportFragmentManager()
                        .findFragmentByTag(getString(R.string.keys_fragment_login));
                frag.setError("Log in unsuccessful");
                frag.handleOnError();
            }
        } catch (JSONException e) {
            //It appears that the web service didn't return a JSON formatted string
            //or it didn't have what we expected in it
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }
}
