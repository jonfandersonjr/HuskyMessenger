package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import instructor.tcss450.uw.edu.messengerapp.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    private OnRegisterFragmentInteractionListener myListener;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        Button b = (Button) v.findViewById(R.id.submitButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubmitButtonClicked(view);
            }
        });

        return v;
    }

    public void onSubmitButtonClicked(View view) {
        EditText edits[] = new EditText[6];

        edits[0] = getView().findViewById(R.id.register_fname);
        edits[1] = getView().findViewById(R.id.register_lname);
        edits[2] = getView().findViewById(R.id.register_nickname);
        edits[3] = getView().findViewById(R.id.register_email);
        edits[4] = getView().findViewById(R.id.register_password);
        edits[5] = getView().findViewById(R.id.register_confirm_password);

        boolean fieldIsEmpty = isFieldEmpty(edits);
        boolean passwordsMatch = passwordsMatch();
        boolean isEmail = isEmail();
        boolean meetsConstraints = passMeetsConstraints();

        if (!fieldIsEmpty && passwordsMatch && isEmail && meetsConstraints) {
            String fname = getFname();
            String lname = getLname();
            String nickname = getNickname();
            String email = getEmail();
            Editable pass = getPassword();

            instructor.tcss450.uw.edu.messengerapp.model.Credentials credentials =
                    new Credentials.Builder(nickname, pass)
                            .addFirstName(fname)
                            .addLastName(lname)
                            .addEmail(email)
                            .build();

            myListener.onSubmitButtonInteraction(credentials);
        }
    }

    public boolean isFieldEmpty(EditText edits[]) {
        boolean fieldIsEmpty = false;

        for (int i = 0; i < edits.length; i++) {
            EditText field = edits[i];
            String text = field.getText().toString();
            if (text.trim().length() == 0) {
                fieldIsEmpty = true;
                field.setError("Field cannot be empty");
            }
        }

        return fieldIsEmpty;
    }

    public boolean passwordsMatch() {
        boolean isMatching;

        EditText passwordOne = getView().findViewById(R.id.register_password);
        EditText passwordTwo = getView().findViewById(R.id.register_confirm_password);
        String passwordOneString = passwordOne.getText().toString();
        String passwordTwoString = passwordTwo.getText().toString();
        if (passwordOneString.equals(passwordTwoString)) {
            isMatching = true;
        } else {
            isMatching = false;
            passwordOne.setError("Passwords must be matching");
            passwordTwo.setError("Passwords must be matching");
        }

        return isMatching;
    }

    public boolean isEmail() {
        boolean isEmail = false;

        EditText email = getView().findViewById(R.id.register_email);
        String emailString = email.getText().toString();

        if (emailString.contains("@")) {
            isEmail = true;
        } else {
            email.setError("Must be an email address");
        }

        return isEmail;
    }

    public boolean passMeetsConstraints() {
        boolean hasUpper = false;
        boolean hasDigit = false;
        boolean hasLength = false;

        boolean meetsConstraints = false;

        EditText password = getView().findViewById(R.id.register_password);
        String passwordString = password.getText().toString();

        if (passwordString.trim().length() < 5) {
            password.setError("Password must be at least 5 characters");
        } else {
            hasLength = true;
        }

        for (int i = 0; i < passwordString.length(); i++) {
            if (Character.isUpperCase(passwordString.charAt(i))) {
                hasUpper = true;
            }
        }

        if (!hasUpper) {
            password.setError("Password must contain upper case letter");
        }

        for (int i = 0; i < passwordString.length(); i++) {
            if (Character.isDigit(passwordString.charAt(i))) {
                hasDigit = true;
            }
        }

        if (!hasDigit) {
            password.setError("Password must contain one digit");
        }

        if (hasDigit && hasLength && hasUpper) {
            meetsConstraints = true;
        }

        return meetsConstraints;
    }

    public String getNickname() {
        EditText nickName = getView().findViewById(R.id.register_nickname);
        String nickNameString = nickName.getText().toString();

        return nickNameString;
    }

    public Editable getPassword() {
        EditText password = getView().findViewById(R.id.register_password);
        Editable passwordString = password.getEditableText();

        return passwordString;

    }

    public String getFname() {
        EditText fname = getView().findViewById(R.id.register_fname);
        String fnameString = fname.getText().toString();

        return fnameString;
    }

    public String getLname() {
        EditText lname = getView().findViewById(R.id.register_lname);
        String lnameString = lname.getText().toString();

        return lnameString;
    }

    public String getEmail() {
        EditText email = getView().findViewById(R.id.register_email);
        String emailString = email.getText().toString();

        return emailString;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRegisterFragmentInteractionListener) {
            myListener = (OnRegisterFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    public void handleOnPre() {
        ProgressBar progBar = getView().findViewById(R.id.registerProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);

        Button b = getView().findViewById(R.id.submitButton);
        b.setEnabled(false);
    }

    public void handleOnError() {
        ProgressBar progBar = getView().findViewById(R.id.registerProgressBar);
        progBar.setVisibility(ProgressBar.GONE);

        Button b = getView().findViewById(R.id.submitButton);
        b.setEnabled(true);
    }

    public void setError(String err) {
        //Register unsuccessful for reason: err. Try again.
        Toast.makeText(getActivity(), "Register unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();

        ((TextView) getView().findViewById(R.id.register_fname))
                .setError("Login Unsuccessful");
    }

    public interface OnRegisterFragmentInteractionListener {
        void onSubmitButtonInteraction(instructor.tcss450.uw.edu.messengerapp.model.Credentials credentials);
    }

}
