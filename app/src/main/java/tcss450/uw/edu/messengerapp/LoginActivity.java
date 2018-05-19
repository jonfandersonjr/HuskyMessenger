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

import tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask;

public class LoginActivity extends AppCompatActivity
        implements LoginFragment.OnLoginFragmentInteractionListener,
        RegisterFragment.OnRegisterFragmentInteractionListener,
        VerifyFragment.OnVerifyFragmentInteractionListener {

    private tcss450.uw.edu.messengerapp.model.Credentials mCredentials;

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
        SharedPreferences prefs =
                getSharedPreferences(getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        //save the username for later usage
        prefs.edit().putString(getString(R.string.keys_prefs_username),
                mCredentials.getUsername()).apply();

        if (((CheckBox) findViewById(R.id.logCheckBox)).isChecked()) {
            //save the users "want" to stay logged in
            prefs.edit().putBoolean(getString(R.string.keys_prefs_stay_logged_in), true).apply();
        }
    }

    private void loadHomePage() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void loadVerifyFragment(String email) {
        //give email string to verify fragment
        Bundle bundle = new Bundle();
        bundle.putString("args", email);
        VerifyFragment frag = new VerifyFragment();
        frag.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.loginFragmentContainer, frag, getString(R.string.keys_fragment_verify))
                .addToBackStack(null).commit();
        getSupportFragmentManager().executePendingTransactions();
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
    public void onLoginButtonInteraction(tcss450.uw.edu.messengerapp.model.Credentials credentials) {
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
    public void onSubmitButtonInteraction(tcss450.uw.edu.messengerapp.model.Credentials credentials) {
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
    }

    @Override
    public void onVerifyButtonInteraction(String email, String code) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_verify))
                .build();

        JSONObject msg = new JSONObject();
        try {
            msg.put("email", email);
            msg.put("code", code);
        } catch (JSONException e) {
            Log.wtf("Verify", "Error reading JSON" + e.getMessage());
        }

        new tcss450.uw.edu.messengerapp.utils.SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handleVerifyOnPre)
                .onPostExecute(this::handleVerifyOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNC_TASK_ERROR", result);
    }

    private void handleVerifyOnPre() {
        VerifyFragment frag = (VerifyFragment) getSupportFragmentManager()
                .findFragmentByTag(getString(R.string.keys_fragment_verify));
        frag.handleOnPre();
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

    private void handleVerifyOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                //checkStayLoggedIn();
                SharedPreferences prefs =
                        getSharedPreferences(getString(R.string.keys_shared_prefs),
                                Context.MODE_PRIVATE);
                prefs.edit().putString(getString(R.string.keys_prefs_username),
                        mCredentials.getUsername()).apply();

                loadHomePage();
            } else {
                VerifyFragment frag = (VerifyFragment) getSupportFragmentManager()
                        .findFragmentByTag(getString(R.string.keys_fragment_verify));
                frag.setError("Credentials are not matching");
                frag.handleOnError();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", result + System.lineSeparator() + e.getMessage());
        }
    }

    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");

            if (success) {
                String email = resultsJSON.getString("userEmail");
                loadVerifyFragment(email);
            } else {
                RegisterFragment frag = (RegisterFragment) getSupportFragmentManager()
                        .findFragmentByTag(getString(R.string.keys_fragment_register));
                String failReason = resultsJSON.getJSONObject("error").getString("detail");
                String str = failReason.split("[\\(\\)]")[1];
                str = str.substring(0, 1).toUpperCase() + str.substring(1);
                frag.setError(" " + str + " already exists!");
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
            boolean verification = resultsJSON.getBoolean("verification");

            if (success && verification) {
                //CHECK VERIFICATION FROM JSON
                checkStayLoggedIn();
                //Login was successful. Switch to the loadDisplayFragment
                loadHomePage();
            } else if (success & !(verification)){
                String email = resultsJSON.getString("userEmail");
                loadVerifyFragment(email);
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
