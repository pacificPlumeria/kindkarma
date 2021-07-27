package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
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
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtUserName;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private String email;
    private String password;
    private String userName;
    private FirebaseFirestore db;
    private Boolean result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtEmail = (EditText) findViewById(R.id.editEmail);
        edtPassword = (EditText) findViewById(R.id.editPassword);
        edtUserName = (EditText) findViewById(R.id.editName);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        email = edtEmail.getText().toString().trim();
        password = edtPassword.getText().toString().trim();
        userName = edtUserName.getText().toString().trim();

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }


        // on click listener on register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = edtEmail.getText().toString().trim();
                password = edtPassword.getText().toString().trim();
                userName = edtUserName.getText().toString().trim();

                db.collection("users").whereEqualTo("username", userName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        // if not found, show error
                        if(queryDocumentSnapshots.isEmpty()) {
                            // error messages displayed if field is empty or entered username is taken
                            if (TextUtils.isEmpty(email)) {
                                edtEmail.setError("Email can't be empty");
                            } else if (TextUtils.isEmpty(password)){
                                edtPassword.setError("Password can't be empty");
                            } else if (TextUtils.isEmpty(userName)) {
                                edtUserName.setError("Username can't be empty");
                            } else {
                                // create user
                                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()){
                                            // get uid for new user
                                            FirebaseUser currentUser = mAuth.getCurrentUser();
                                            String uid = currentUser.getUid();


                                            HashMap<String, Object> dataUser = new HashMap<>();
                                            dataUser.put("about", "");
                                            dataUser.put("categories", new HashMap<String,String>());
                                            dataUser.put("city", "");
                                            dataUser.put("community", "");
                                            dataUser.put("profileImage", "");
                                            dataUser.put("state", "");
                                            dataUser.put("totalHoursWorked", 0);
                                            dataUser.put("username", userName);
                                            dataUser.put("zipcode", 0);


                                            // put username value in document with uid as document id
                                            db.collection("users").document(uid).set(dataUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    // Redirect user to feed activity
                                                    Intent i = new Intent(SignUpActivity.this,Profile_Activity.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            });
                                        } else {
                                            // if registration failed show toast error message
                                            Toast.makeText(SignUpActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        } else {
                            edtUserName.setError("Username is already taken");
                        }
                    }
                });
            }
        });
    }

    // redirect user to sign in page if they click button
    public void onClickSignIn(View view){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
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

}