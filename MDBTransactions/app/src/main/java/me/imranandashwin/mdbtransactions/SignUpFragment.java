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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class SignUpFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private EditText nameSignUp;
    private EditText emailSignUp;
    private EditText passwordSignUp;
    private EditText passwordConfirmSignUp;
    private TextView errorMessageSignUp;
    private Button signUpSubmit;

    public SignUpFragment() {
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
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);

        nameSignUp = v.findViewById(R.id.nameSignUp);
        emailSignUp = v.findViewById(R.id.emailSignUp);
        passwordSignUp = v.findViewById(R.id.passwordSignUp);
        passwordConfirmSignUp = v.findViewById(R.id.passwordConfirm);
        errorMessageSignUp = v.findViewById(R.id.errorSignUp);
        signUpSubmit = v.findViewById(R.id.signUpSubmit);
        signUpSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = nameSignUp.getText().toString();
                String email = emailSignUp.getText().toString();
                String password = passwordSignUp.getText().toString();
                String confirm = passwordConfirmSignUp.getText().toString();

                boolean passwordsMatch = password.equals(confirm);
                boolean nonBlank = !email.isEmpty() && !password.isEmpty() && !confirm.isEmpty() && !name.isEmpty();
                if (passwordsMatch && nonBlank) {
                    createAccount(email, password, name);
                } else if(!nonBlank) {
                    errorMessageSignUp.setText("Cannot leave a field blank.");
                } else {
                    errorMessageSignUp.setText("Passwords do not match.");
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

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {

        Animation anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);

        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                LoginActivity.stopAnimating();
            }
        });

        return anim;
    }

    public void createAccount(String email, String password, final String name) {
        FirebaseUtils.getFirebase().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUtils.setDisplayName(name);
                            Intent purchasesIntent = new Intent(getActivity(), PurchasesActivity.class);
                            startActivity(purchasesIntent);
                            getActivity().finish();
                        } else {
                            errorMessageSignUp.setText("Account creation failed. " + task.getException().getMessage());
                        }
                    }
                });
    }
}
