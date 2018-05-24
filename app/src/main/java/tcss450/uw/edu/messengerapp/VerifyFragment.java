package tcss450.uw.edu.messengerapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyFragment extends Fragment {

    private OnVerifyFragmentInteractionListener mListener;
    private String mEmail;


    public VerifyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEmail = getArguments().getString("args");
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_verify, container, false);

        final TextView TEXT = v.findViewById(R.id.verifyDoWhatTextView);
        final ImageView IMAGE = v.findViewById(R.id.verifyThumbsUp);
        final Button IGETITB = (Button) v.findViewById(R.id.verifyIGetItButton);
        final Button DOWHATB = (Button) v.findViewById(R.id.doWhatButton);

        Button b = (Button) v.findViewById(R.id.verifyButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onVerifyButtonClicked(view);
            }
        });

        DOWHATB.setVisibility(Button.VISIBLE);

        DOWHATB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TEXT.setVisibility(TextView.VISIBLE);
                IMAGE.setVisibility(ImageView.GONE);
                DOWHATB.setVisibility(Button.GONE);
                IGETITB.setVisibility(Button.VISIBLE);
            }
        });

        IGETITB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TEXT.setVisibility(TextView.GONE);
                IMAGE.setVisibility(ImageView.VISIBLE);
                IGETITB.setVisibility(Button.GONE);
                DOWHATB.setVisibility(Button.VISIBLE);
            }
        });

        return v;
    }

    public void onVerifyButtonClicked(View view) {
        boolean isEmpty = isFieldEmpty();
        if (!isEmpty) {
            String code = getCode();
            mListener.onVerifyButtonInteraction(mEmail, code);
        }
    }

    public boolean isFieldEmpty() {
        boolean isEmpty = false;
        EditText edit = getView().findViewById(R.id.verifyEditText);
        String text = edit.getText().toString();
        if (text.trim().length() == 0) {
            isEmpty = true;
            edit.setError("Field cannot be empty");
        }

        return isEmpty;
    }

    public String getCode() {
        EditText edit = getView().findViewById(R.id.verifyEditText);
        String code = edit.getText().toString();

        return code;
    }

    public void handleOnPre() {
        Button b = getView().findViewById(R.id.verifyButton);
        b.setEnabled(false);

        ProgressBar progBar = getView().findViewById(R.id.verifyProgressBar);
        progBar.setVisibility(ProgressBar.VISIBLE);
    }

    public void handleOnError() {
        Button b = getView().findViewById(R.id.verifyButton);
        b.setEnabled(true);

        ProgressBar progBar = getView().findViewById(R.id.verifyProgressBar);
        progBar.setVisibility(ProgressBar.GONE);
    }

    public void setError(String err) {
        //Register unsuccessful for reason: err. Try again.
        Toast.makeText(getActivity(), "Verify unsuccessful for reason: " + err,
                Toast.LENGTH_SHORT).show();

        ((EditText) getView().findViewById(R.id.verifyEditText))
                .setError("Verification Unsuccessful");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof VerifyFragment.OnVerifyFragmentInteractionListener) {
            mListener = (VerifyFragment.OnVerifyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerifyFragmentInteractionListener");
        }
    }

    public interface OnVerifyFragmentInteractionListener {
        void onVerifyButtonInteraction(String email, String code);
    }

}
