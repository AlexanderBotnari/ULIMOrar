package com.example.ulimorar.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.ulimorar.BuildConfig;
import com.example.ulimorar.R;
import com.example.ulimorar.activities.LoginActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender extends AsyncTask<String, Void, Void> {

    @SuppressLint("StaticFieldLeak")
    private LoginActivity loginActivity;

    public EmailSender(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    @Override
    protected Void doInBackground(String... params) {
        String toEmail = params[0];
        String userPassword = params[1];

        sendPasswordResetEmail(toEmail, userPassword);

        return null;
    }

    private void sendPasswordResetEmail(String toEmail, String userPassword) {
        // credentials for sender email
        final String username = BuildConfig.EMAIL_ADDRESS;
        final String password = BuildConfig.EMAIL_PASSWORD;

        // Session properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create session
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(loginActivity.getText(R.string.reset_password_email_title).toString());
            message.setText(loginActivity.getText(R.string.email_addressing) +
                    loginActivity.getText(R.string.somebody_request_password).toString()+" "+toEmail+"\n\n"+
                    loginActivity.getText(R.string.you_can_reset_password) +
                    loginActivity.getText(R.string.current_password) +" "+userPassword+"\n\n"+
                    loginActivity.getText(R.string.false_request_password) +
                    loginActivity.getText(R.string.email_termination));

            // Send message
            Transport.send(message);

            String messageForSnack = loginActivity.getText(R.string.email_sent_to)+ " " + toEmail+"\n"
                    +loginActivity.getText(R.string.please_verify_inbox);
            Snackbar snackbar = Snackbar.make(loginActivity.findViewById(android.R.id.content), messageForSnack, 10000);
            snackbar.show();

        } catch (MessagingException e) {
            Log.d("FailureSendEmail", e.getMessage());
            loginActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(loginActivity.getApplicationContext(), R.string.error_send_email, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
