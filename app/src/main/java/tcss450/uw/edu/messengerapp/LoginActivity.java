package tcss450.uw.edu.messengerapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;

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
                    skipLogin();
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

    private void skipLogin() {
        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @Override
    public void onRegisterButtonInteraction() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.pop_enter, R.anim.pop_exit, R.anim.enter, R.anim.exit)
                .replace(R.id.loginFragmentContainer, new RegisterFragment())
                .addToBackStack(null).commit();
    }

    @Override
    public void onLoginButtonInteraction(instructor.tcss450.uw.edu.messengerapp.model.Credentials credentials) {
        mCredentials = credentials;
        checkStayLoggedIn();
        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }

    @Override
    public void onSubmitButtonInteraction() {
        Intent intent = new Intent(this, HomeActivity.class);
//        intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
