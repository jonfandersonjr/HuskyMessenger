package group3.tcss450.uw.edu.groupproject;


import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener,
        RegisterFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState == null) {
            if (findViewById(R.id.fragmentContainer) != null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragmentContainer, new LoginFragment())
                        .commit();
            }
        }
    }

    @Override
    public void onLogin(String theUsername, String thePassword) {
        SuccessFragment success = new SuccessFragment();
        Bundle args = new Bundle();
        args.putSerializable(getString(R.string.user_key), theUsername);
        args.putSerializable(getString(R.string.pass_key), thePassword);
        success.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, success);

        transaction.commit();
    }

    @Override
    public void onRegister() {
        RegisterFragment register = new RegisterFragment();

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, register)
                .addToBackStack(null);

        transaction.commit();
    }

    @Override
    public void onNewRegister(String theUsername, String thePassword) {
        getSupportFragmentManager().popBackStack();
        onLogin(theUsername, thePassword);
    }
}
