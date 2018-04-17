package group3.tcss450.uw.edu.groupproject;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;

    EditText name;
    EditText initPass;
    EditText confirmPass;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_register, container, false);

        name = v.findViewById(R.id.newUsername);
        initPass = v.findViewById(R.id.initPassword);
        confirmPass = v.findViewById(R.id.confirmPassword);

        Button b = (Button) (v.findViewById(R.id.button));
        b.setOnClickListener(this::checkCredentials);

        return v;
    }

    private void checkCredentials(View view) {
        if (mListener != null) {
            if (name.getText().toString().length() > 0
                    && initPass.getText().toString().equals(confirmPass.getText().toString())
                    && initPass.getText().toString().length() > 0 && confirmPass.getText().toString().length() > 0) {

                mListener.onNewRegister(name.getText().toString(), confirmPass.getText().toString());
            }
            else {
                if (name.getText().toString().length() < 1) {
                    name.setError("Username cannot be empty");
                }
                if (!initPass.getText().toString().equals(confirmPass.getText().toString())) {
                    confirmPass.setError("Passwords do not match");
                }
                if (initPass.getText().toString().length() < 1 || confirmPass.getText().toString().length() < 1) {
                    initPass.setError("This field cannot be empty");
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onNewRegister(String theUsername, String thePassword);
    }
}
