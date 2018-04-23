package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private OnLoginFragmentInteractionListener myListener;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        Button b = (Button) v.findViewById(R.id.loginButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLoginButtonClicked(view);
            }
        });

        b = (Button) v.findViewById(R.id.registerButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRegisterButtonClicked(view);
            }
        });

        return v;
    }


    public boolean usernameIsEmpty() {
        boolean whatever;
        EditText username = getView().findViewById(R.id.usernameEditText);
        String usernameString = username.getText().toString();
        if (usernameString.trim().length() > 0) {
            whatever = false;
        } else {
            whatever = true;
            username.setError("Username cannot be empty");
        }
        return whatever;
    }

    public boolean passwordIsEmpty() {
        boolean fine;
        EditText password = getView().findViewById(R.id.passwordEditText);
        String passwordString = password.getText().toString();
        if (passwordString.trim().length() > 0) {
            fine = false;
        } else {
            fine = true;
            password.setError("Password cannot be empty");
        }

        return fine;
    }

    public boolean usernameIsNotEmail() {
        boolean okay;
        EditText username = getView().findViewById(R.id.usernameEditText);
        String usernameString = username.getText().toString();
        if (usernameString.contains("@")) {
            okay = false;
        } else {
            okay = true;
            username.setError("Must be an email address");
        }

        return okay;
    }

    public void onLoginButtonClicked(View view) {
        boolean isNotEmail = usernameIsNotEmail();
        boolean userIsEmpty = usernameIsEmpty();
        boolean passIsEmpty = passwordIsEmpty();

        if (!(userIsEmpty || passIsEmpty || isNotEmail)) {
            myListener.onLoginButtonInteraction();
        }
    }

    public void onRegisterButtonClicked(View view) {
        myListener.onRegisterButtonInteraction();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            myListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    public interface OnLoginFragmentInteractionListener {
        void onRegisterButtonInteraction();
        void onLoginButtonInteraction();
    }

}
