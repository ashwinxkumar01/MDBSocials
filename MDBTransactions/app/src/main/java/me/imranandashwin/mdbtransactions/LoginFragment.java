package me.imranandashwin.mdbtransactions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;


public class LoginFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private EditText emailLogin;
    private EditText passwordLogin;
    private TextView errorLogin;
    private Button loginSubmit;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_login_fragment, container, false);

        emailLogin = v.findViewById(R.id.emailLogin);
        passwordLogin = v.findViewById(R.id.passwordLogin);
        errorLogin = v.findViewById(R.id.errorLogin);
        loginSubmit = v.findViewById(R.id.submitLogin);
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailLogin.getText().toString();
                String password = passwordLogin.getText().toString();

                boolean noBlanks = !email.isEmpty() && !password.isEmpty();

                if (!noBlanks) {
                    errorLogin.setText("Cannot leave a field blank.");
                } else {
                    signIn(email, password);
                }
            }
        });

        return v;
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void signIn(final String email, final String password) {
        FirebaseUtils.getFirebase().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent purchasesIntent = new Intent(getActivity(), PurchasesActivity.class);
                            startActivity(purchasesIntent);
                            getActivity().finish();
                        } else {
                            errorLogin.setText("Login failed. Please try again.");
                        }
                    }
                });
    }
}
