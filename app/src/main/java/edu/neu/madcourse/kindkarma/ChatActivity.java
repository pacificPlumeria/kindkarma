package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

import edu.neu.madcourse.kindkarma.models.Chat;
import edu.neu.madcourse.kindkarma.models.User;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView rvChat;
    private EditText editMessage;
    private ImageButton imbSend;
    private LinearLayoutManager mLayoutManager;
    FirestoreRecyclerAdapter<Chat,ChatViewHolder> adapter;
    String uid, idChatroom;
    private String TAG = "ChatActivity";
    String uidFriend;
    private String CLIENT_REGISTRATION_TOKEN;
    private static final String SERVER_KEY = "key=AAAAQq6C_Wo:APA91bF-83JCqjVbED5GJ2zfC5xKJeQ5clZNbMz8fWM8x3jVEjjHcDFzEse1Da0dkjrClUajd-IwnUgVCQIuMK4Rjupt7vnRbqiRRAMvZ8XQgLSEQO44E4K_Eitoa4rBrnt_0ZTRyNZF";
    User signedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // get instance of auth and database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();
        findUser();

        // Get token from instance ID
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                CLIENT_REGISTRATION_TOKEN = instanceIdResult.getToken();
                String msg = getString(R.string.msg_token_fmt, CLIENT_REGISTRATION_TOKEN);
                Log.e("Token",CLIENT_REGISTRATION_TOKEN);
                //Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });


        rvChat = findViewById(R.id.rvChat);
        editMessage = findViewById(R.id.editMessage);
        imbSend = findViewById(R.id.imbSend);

        // get values from main activity
        idChatroom = getIntent().getExtras().getString("idChatRoom");
        uidFriend = getIntent().getExtras().getString("uidFriend");
        Log.i("uid", uidFriend);

        // set layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(false);
        mLayoutManager.setStackFromEnd(true);

        rvChat.setHasFixedSize(true);

        // Bind the layout manager to the RV
        rvChat.setLayoutManager(mLayoutManager);

        // Get all chats from chat room using id
        FirestoreRecyclerOptions<Chat> options = new FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(db.collection("chatroom").document(idChatroom).collection("chat").orderBy("date"),Chat.class)
                .build();

        // set the chat to the holder
        adapter = new FirestoreRecyclerAdapter<Chat, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull Chat model) {
                holder.setList(model.getUid(), model.getMessage(),getApplicationContext());
            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message, parent, false);
                return new ChatViewHolder(view);
            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                mLayoutManager.smoothScrollToPosition(rvChat, null,adapter.getItemCount());
            }
        });

        // Bind the adapter to the RV
        rvChat.setAdapter(adapter);
        adapter.startListening();

        // Called when user clicks send message button
        imbSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get entered message
                String message = editMessage.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    // do nothing
                } else {
                    HashMap<String,Object> dataMessage = new HashMap<>();
                    // put all message contents in HashMap
                    dataMessage.put("date", FieldValue.serverTimestamp());
                    dataMessage.put("message",message);
                    dataMessage.put("uid",uid);
                    // add the values to the database
                    db.collection("chatroom").document(idChatroom).collection("chat").document().set(dataMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // if successful, clear the editText
                            editMessage.setText("");
                        }
                    });

                    sendChatMessageToFriend(message, uidFriend);

                }
            }
        });
    }


    private void sendChatMessageToFriend(String message, String uidFriend){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DocumentReference docRef = db.collection("users").document(uidFriend);

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String token = document.getString("token");
                                String username = document.getString("username");
                                sendMessageToDevice(token, message, username);
                                Log.i("username", username);
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        }).start();
    }

    /**
     * Pushes a notification to a given device-- in particular, this device,
     * because that's what the instanceID token is defined to be.
     */
    private void sendMessageToDevice(String targetToken, String message, String username) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                JSONObject jdata = new JSONObject();
                try {
                    jNotification.put("title", signedInUser.getUsername() + " sent you a message!");
                    jNotification.put("body", message);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    /*
                    // We can add more details into the notification if we want.
                    // We happen to be ignoring them for this demo.
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");
                    */
                    jdata.put("title","data title");
                    jdata.put("content","data content");

                    /***
                     * The Notification object is now populated.
                     * Next, build the Payload that we send to the server.
                     */

                    // If sending to a single client
                    jPayload.put("to", targetToken); // CLIENT_REGISTRATION_TOKEN);

                    /*
                    // If sending to multiple clients (must be more than 1 and less than 1000)
                    JSONArray ja = new JSONArray();
                    ja.put(CLIENT_REGISTRATION_TOKEN);
                    // Add Other client tokens
                    ja.put(FirebaseInstanceId.getInstance().getToken());
                    jPayload.put("registration_ids", ja);
                    */

                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);
                    jPayload.put("data",jdata);


                    /***
                     * The Payload object is now populated.
                     * Send it to Firebase to send the message to the appropriate recipient.
                     */
                    URL url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", SERVER_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    // Send FCM message content.
                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(jPayload.toString().getBytes());
                    outputStream.close();

                    // Read FCM response.
                    InputStream inputStream = conn.getInputStream();
                    final String resp = convertStreamToString(inputStream);

                    Handler h = new Handler(Looper.getMainLooper());
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(),resp,Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // updates progre
    private void findUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // get the signed in user object from the database
                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get().addOnSuccessListener((userSnapshot) -> {
                    signedInUser = userSnapshot.toObject(User.class);
                    Log.i(TAG, "Signed In User" + signedInUser);
                }).addOnFailureListener((exception) -> {
                    Log.i(TAG, "Failure fetching signed in user" + exception);
                });
            }
        }).start();
    }

    /**
     * Helper function
     * @param is
     * @return
     */
    private String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next().replace(",", ",\n") : "";
    }

    // Responsible for managing the rows and keeps a track of everything inside the row (image, data, etc)
    public class ChatViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ConstraintLayout clMessage;
        TextView txtMessage;

        // Constructor
        // View Holder describes an item view and metadata about its place within the RecyclerView
        public ChatViewHolder(View itemView){
            super(itemView);
            mView = itemView;
            clMessage = mView.findViewById(R.id.clMessage);
            txtMessage = mView.findViewById(R.id.txtMessage);
        }

        public void setList(String uidMessage, String message, Context context) {
            // if the message is sent by the signed in user, set certain attributes
            if (uidMessage.equals(uid)){
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(clMessage);
                constraintSet.setHorizontalBias(R.id.txtMessage,1.0f);
                constraintSet.applyTo(clMessage);
                txtMessage.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.user_message, context.getTheme()));
                txtMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
                txtMessage.setText(message);
            } else {
                // if the message is sent by the signed in user's friend, set certain attributes
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(clMessage);
                constraintSet.setHorizontalBias(R.id.txtMessage,0.0f);
                constraintSet.applyTo(clMessage);
                txtMessage.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.friend_message, context.getTheme()));
                txtMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                txtMessage.setText(message);
            }
        }
    }
}