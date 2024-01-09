package com.example.ulimorar.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    private static final String FAILURE_TAG = "FAILURE_TAG";

    private TextInputLayout loginInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText loginEditText;
    private TextInputEditText passwordEditText;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_ULIMNoActionBar);
        try {
            setContentView(R.layout.activity_login);
        }catch (Exception ignore){

        }

        auth = FirebaseAuth.getInstance();

        loginInputLayout = findViewById(R.id.emailTextField);
        passwordInputLayout = findViewById(R.id.passwordTextField);
        loginEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        TextView forgotTextView = findViewById(R.id.forgotPasswordButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser(loginEditText.getText().toString(), passwordEditText.getText().toString());
            }
        });

        forgotTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isValid = true;
                if (!loginEditText.getText().toString().contains("@") || !loginEditText.getText().toString().contains(".")) {
                    loginInputLayout.setError(getString(R.string.enter_valid_email));
                    isValid = false;
                }else{
                    loginInputLayout.setError(null);
                }

                if (isValid){
                    ActionCodeSettings actionCodeSettings =
                            ActionCodeSettings.newBuilder()
                                    // URL you want to redirect back to. The domain (www.example.com) for this
                                    // URL must be whitelisted in the Firebase Console.
                                    .setUrl("https://ulimorar.page.link/emailLinkSignIn")
                                    // This must be true
                                    .setHandleCodeInApp(true)
                                    .setAndroidPackageName(
                                            getPackageName(),
                                            true, /* installIfNotAvailable */
                                            null    /* minimumVersion */)
                                    .build();
                    auth.sendSignInLinkToEmail(loginEditText.getText().toString(), actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                String message = getText(R.string.email_sent_to)+ " " + loginEditText.getText().toString()+"\n"
                                        +getText(R.string.please_click_link_in_email);
                                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, 10000);
                                snackbar.show();
                                // Save email in preferences to access it after click on link from email
                                SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString("ForgotPasswordEmail", loginEditText.getText().toString());
                                editor.apply();

                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            Log.d("FailureSendLink", e.getMessage());
                            Toast.makeText(LoginActivity.this, R.string.error_to_send_link, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void loginUser(String email, String password){
        if(loginEditText.getText().toString().trim().equals("")){
            loginInputLayout.setError(getText(R.string.empty_email_error));
        }else if(passwordEditText.getText().toString().trim().length() < 7){
            passwordInputLayout.setError(getText(R.string.password_length_error));
        } else {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = auth.getCurrentUser();
                                startActivity(new Intent(LoginActivity.this, FacultyActivity.class).putExtra("currentUserEmail", user.getEmail()));
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(FAILURE_TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, R.string.authentication_error,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, FacultyActivity.class);
            intent.putExtra("currentUserEmail", currentUser.getEmail());
            startActivity(intent);
        }

        //Get dynamic link if exists from email link authentication
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            Log.d("DynamicLink", deepLink.toString());
                            signInWithEmailLink(deepLink.toString());
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DynamicLinkFailure", "getDynamicLink:onFailure", e);
                    }
                });
    }

    private void signInWithEmailLink(String emailLink) {
        Log.d("SignInWithEmailLink", "Attempting sign-in with email link...");
        // get email from preferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedEmail = preferences.getString("ForgotPasswordEmail", "");

        if (auth.isSignInWithEmailLink(emailLink)) {
            auth.signInWithEmailLink(savedEmail, emailLink)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            Log.d("SignInEmailLinkSuccess", "Email link sign-in success. "+ user.getEmail());
                            startActivity(new Intent(LoginActivity.this, FacultyActivity.class).putExtra("currentUserEmail", savedEmail));
                        } else {
                            Log.e("SignInEmailLinkFailed", "Email link sign-in failed.", task.getException());
                            Toast.makeText(LoginActivity.this, R.string.email_signin_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e("InvalidEmailLink", "The provided link is not a valid email link.");
            Toast.makeText(this, R.string.invalid_email_link, Toast.LENGTH_SHORT).show();
        }
    }

}