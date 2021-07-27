package edu.neu.madcourse.kindkarma;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import edu.neu.madcourse.kindkarma.models.Post;

public class LoginActivity extends AppCompatActivity {

    Button btnLogin;
    EditText getEmail;
    EditText getPassword;
    String email;
    String password;
    FirebaseAuth auth;
    String TAG = "Tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_KindKarma);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        // check authorization
        checkAuth();

        btnLogin = (Button)findViewById(R.id.btnLogin);
        getEmail = (EditText)findViewById(R.id.getEmail);
        getPassword = (EditText)findViewById(R.id.getPassword);
    }

    //https://stackoverflow.com/questions/39884661/notify-user-that-the-user-is-not-connected-to-internet-at-any-screen
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void askNetwork() {
        // below line is use to request
        // permission in the current activity.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet");
        builder.setMessage("Please connect to your internet");
        builder.setPositiveButton("Wifi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                // below is the intent from which we are redirecting our user.
                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));

            }
        });
        builder.setNegativeButton("Mobile Data", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings$DataUsageSummaryActivity"));
                dialog.cancel();
                startActivity(intent);
            }
        });
        builder.show();

    }


    public void onClick(View view){
        // disables button after user clicks it once
        btnLogin.setEnabled(false);
        // getting email and password from user
        email = getEmail.getText().toString();
        password = getPassword.getText().toString();

        // checks if email and password is empty -- if it is display error and enable button
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Email/Password cannot be empty", Toast.LENGTH_SHORT).show();
            btnLogin.setEnabled(true);
        } else {
            // does email and password auth
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                // enables button once confirmed successful login
                btnLogin.setEnabled(true);
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show();
                    goFeedActivity();
                } else {
                    // if auth fails -- displays error and enable button
                    Log.i(TAG, "Sign In with Email failed", task.getException());
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    btnLogin.setEnabled(true);
                }
            });
        }
    }

    public void goFeedActivity(){
        Log.i(TAG,"goFeedActivity");
        // opens feedActivity
        Intent intent = new Intent(this, FeedActivity.class);
        startActivity(intent);
        // finish login activity so that back button on next activity doesn't bring you back to login
        // no longer part of backstack
        finish();
    }


    // If the user is a new user, redirect to sign up page
    public void onClickSignUp(View view){
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    // updates progre
    private void checkAuth() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Firebase auth check - object on which we can check user's credentials
                // check as soon as activity is created
                auth = FirebaseAuth.getInstance();
                // means firebase has stored the session in the app already
                // Go straight to feed activity
                if (auth.getCurrentUser() != null){
                    goFeedActivity();
                }
            }
        }).start();
    }
}