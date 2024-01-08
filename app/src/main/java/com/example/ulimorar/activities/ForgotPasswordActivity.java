package com.example.ulimorar.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.ulimorar.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputLayout emailTextInputLayout;
    private Button cancelButton;
    private Button sendButton;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        cancelButton = findViewById(R.id.cancelButton);
        sendButton = findViewById(R.id.sendButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailTextInputLayout = findViewById(R.id.emailForgotTextField);
                String email = emailTextInputLayout.getEditText().getText().toString();


                boolean isValid = true;
                if (!email.contains("@") || !email.contains(".")) {
                    emailTextInputLayout.setError(getString(R.string.enter_valid_email));
                    isValid = false;
                }else{
                    emailTextInputLayout.setError(null);
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
                    auth.sendSignInLinkToEmail(email, actionCodeSettings).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPasswordActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if the user is already signed in
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d("ForgotPasswordUSer", "User is already signed in: " + currentUser.getEmail());
        }
    }

    // Handle the email link sign-in in onNewIntent
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getData() != null) {
            // Get the email link data from the intent
            String emailLink = intent.getData().toString();
            // Sign in with the email link
            signInWithEmailLink(emailLink);
        }
    }

    private void signInWithEmailLink(String emailLink) {
        auth.signInWithEmailLink(emailTextInputLayout.getEditText().getText().toString(), emailLink)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("SignInEmailLinkSucces", "Email link sign-in success.");
                        FirebaseUser user = task.getResult().getUser();
                        Toast.makeText(ForgotPasswordActivity.this, "Sign-in successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("SignInEmailLinkFailed", "Email link sign-in failed.", task.getException());
                        Toast.makeText(ForgotPasswordActivity.this, "Sign-in failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}