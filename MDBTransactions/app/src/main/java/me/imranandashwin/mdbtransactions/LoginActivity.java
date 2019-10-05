package me.imranandashwin.mdbtransactions;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.google.firebase.FirebaseApp;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener, SignUpFragment.OnFragmentInteractionListener, View.OnTouchListener{

    private Button signInToggle;
    private Button loginInToggle;

    private static boolean isAnimating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        signInToggle = findViewById(R.id.signUpButton);
        loginInToggle = findViewById(R.id.loginButton);
        signInToggle.setOnTouchListener(this);
        loginInToggle.setOnTouchListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame, new LoginFragment()).commit();

        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseUtils.startFireBase();

        if (FirebaseUtils.isLoggedIn()) {
            Intent purchasesIntent = new Intent(getApplicationContext(), PurchasesActivity.class);
            startActivity(purchasesIntent);
            finish();
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri){
    }


    public static void stopAnimating() {
        isAnimating = false;
    }

    public static void startAnimating() {
        isAnimating = true;
    }

    public static boolean isAnimating() {
        return isAnimating;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!LoginActivity.isAnimating() && v.isEnabled()) {

            loginInToggle.setEnabled(!loginInToggle.isEnabled());
            signInToggle.setEnabled(!signInToggle.isEnabled());

            int signInColor = signInToggle.getTextColors().getDefaultColor();
            signInToggle.setTextColor(loginInToggle.getTextColors().getDefaultColor());
            loginInToggle.setTextColor(signInColor);

            switch (v.getId()) {
                case R.id.signUpButton:
                    LoginActivity.startAnimating();
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left).replace(R.id.frame, new SignUpFragment()).commit();
                    break;
                case R.id.loginButton:
                    LoginActivity.startAnimating();
                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.frame, new LoginFragment()).commit();
                    break;
            }
        }
        return false;
    }
}
