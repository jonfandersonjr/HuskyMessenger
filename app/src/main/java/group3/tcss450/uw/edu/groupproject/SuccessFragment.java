package group3.tcss450.uw.edu.groupproject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class SuccessFragment extends Fragment {


    public SuccessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_success, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getArguments() != null) {
            String username = getArguments().getString(getString(R.string.user_key));
            String password = getArguments().getString(getString(R.string.pass_key));
            updateContent(username, password);
        }
    }




    public void updateContent(String theUsername, String thePassword) {
        TextView user = getActivity().findViewById(R.id.successUsername);
        TextView pass = getActivity().findViewById(R.id.successPassword);
        user.setText(theUsername);
        pass.setText(thePassword);
    }

}
