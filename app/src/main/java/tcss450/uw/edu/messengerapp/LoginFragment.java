package tcss450.uw.edu.messengerapp;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import tcss450.uw.edu.messengerapp.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private OnLoginFragmentInteractionListener myListener;
    private boolean mArgumentsRead;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        mArgumentsRead = false;

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

        b = (Button) v.findViewById(R.id.loginForgotPasswordButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText et = new EditText(getActivity());
                et.setHint("Enter email address");
                et.setTextColor(getResources().getColor(android.R.color.white));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                et.setLayoutParams(lp);

                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                        android.R.style.Theme_Material_Dialog_Alert);
                builder.setView(et);
                builder.setTitle("Change Password")
                        .setMessage("To change your password, we need to verify your email address")
                        .setNegativeButton("Done", null)
                        .setPositiveButton("Nevermind", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                final AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button b = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                boolean isNotEmail = checkFieldIsEmail(et);
                                boolean isEmpty = checkFieldIsEmpty(et);

                                if (!(isEmpty || isNotEmail)) {
                                    SharedPreferences prefs =
                                            getActivity().getSharedPreferences
                                                    (getString(R.string.keys_shared_prefs),
                                                            Context.MODE_PRIVATE);
                                    prefs.edit().putString("changePassEmail",
                                            et.getText().toString()).apply();
                                    dialog.dismiss();
                                    myListener.onChangePasswordInteraction();
                                }
                            }
                        });
                    }
                });
                dialog.show();
            }
        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mArgumentsRead) {
            if (this.getArguments() != null) {
                Bundle bundle = this.getArguments();
                try {
                    boolean passChanged = bundle.getBoolean("passChanged");
                    if (passChanged) {
                        AlertDialog.Builder builder;
                        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
                        builder.setTitle("Password Changed")
                                .setMessage("Your password has successfully been changed")
                                .setPositiveButton("Great!", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        mArgumentsRead = true;
                                    }
                                })
                                .setIcon(R.drawable.checkicon);
                        builder.show();
                    }
                } catch (NullPointerException e) {

                }
            }
        }
    }

    public boolean checkFieldIsEmpty(EditText et) {
        boolean isEmpty;
        String email = et.getText().toString();
        if (email.trim().length() > 0) {
            isEmpty = false;
        } else {
            isEmpty = true;
            et.setError("Field cannot be empty");
        }

        return isEmpty;
    }

    public boolean checkFieldIsEmail(EditText et) {
        boolean isNotEmail;
        String email = et.getText().toString();
        if (email.contains("@")) {
            isNotEmail = false;
        } else {
            isNotEmail = true;
            et.setError("Must be an email address");
        }

        return isNotEmail;
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

    public Editable getPassword() {
        EditText password = getView().findViewById(R.id.passwordEditText);
        Editable passwordString = password.getEditableText();
        return passwordString;
    }

    public String getUsername() {
        EditText username = getView().findViewById(R.id.usernameEditText);
        String usernameString = username.getText().toString();
        return usernameString;
    }

    public void onLoginButtonClicked(View view) {
        boolean userIsEmpty = usernameIsEmpty();
        boolean passIsEmpty = passwordIsEmpty();
        String username = getUsername();
        Editable password = getPassword();

        if (!(userIsEmpty || passIsEmpty)) {
            tcss450.uw.edu.messengerapp.model.Credentials credentials =
                    new Credentials.Builder(username, password).build();
            myListener.onLoginButtonInteraction(credentials);
        }
    }
    public void handleOnPre() {
        Button b = getView().findViewById(R.id.loginButton);
        b.setEnabled(false);

        b = getView().findViewById(R.id.registerButton);
        b.setEnabled(false);

        b = getView().findViewById(R.id.loginForgotPasswordButton);
        b.setEnabled(false);

        ProgressBar progBar = getView().findViewById(R.id.loginProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);
    }

    public void handleOnError() {
        ProgressBar progBar = getView().findViewById(R.id.loginProgressBar);
        progBar.setVisibility(ProgressBar.GONE);

        Button b = getView().findViewById(R.id.loginButton);
        b.setEnabled(true);

        b = getView().findViewById(R.id.registerButton);
        b.setEnabled(true);

        b = getView().findViewById(R.id.loginForgotPasswordButton);
        b.setEnabled(true);
    }

    public void setError(String err) {
        //Log in unsuccessful for reason: err. Try again.
        Toast.makeText(getActivity(), "Log in unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();

        ((TextView) getView().findViewById(R.id.usernameEditText))
                .setError("Login Unsuccessful");
    }

    public void showEmailAlert() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert)
        .setTitle("Error")
        .setMessage("Email is not associated with an account")
        .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        })
        .setIcon(R.drawable.alert);
        builder.show();
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
        void onLoginButtonInteraction(tcss450.uw.edu.messengerapp.model.Credentials credentials);
        void onChangePasswordInteraction();
    }

}
