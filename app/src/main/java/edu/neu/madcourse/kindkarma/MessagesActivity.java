package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.neu.madcourse.kindkarma.models.Friend;
import edu.neu.madcourse.kindkarma.models.User;

public class MessagesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FloatingActionButton fabAdd;
    private FirebaseFirestore db;
    private String uid;
    private RecyclerView rvFriend;
    private LinearLayoutManager mLayoutManager;
    private FirestoreRecyclerAdapter<Friend, FriendViewHolder> adapter;
    private String TAG = "MainActivity";
    Toolbar toolbar;
    TextView signedInUserName, signedInUserNameCommunity;
    ImageView signedInUserImageHeader;
    User signedInUser;
    private final String EXTRA_USERID = "EXTRA_USERID";
    CollectionReference usersReference;
    public List<User> userList;
    public ArrayList<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        mAuth = FirebaseAuth.getInstance();
        // Get instance of firebasefirestore - points to root of our database
        db = FirebaseFirestore.getInstance();

        // get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        uid = currentUser.getUid();

        toolbar = (Toolbar) findViewById(R.id.messagesToolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutMessages);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // get RecyclerView
        rvFriend = findViewById(R.id.rvFriend);

        // set layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        // Create dividers between recyclerView items
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvFriend.getContext(), mLayoutManager.getOrientation());
        rvFriend.addItemDecoration(dividerItemDecoration);
        rvFriend.setHasFixedSize(true);

        // Bind the layout manager to the RV
        rvFriend.setLayoutManager(mLayoutManager);

        // Get all friends from user -- the people that the user has added
        FirestoreRecyclerOptions<Friend> options = new FirestoreRecyclerOptions.Builder<Friend>()
                .setQuery(db.collection("users").document(uid).collection("friend"),Friend.class)
                .build();

        // set the friend to the holder
        adapter = new FirestoreRecyclerAdapter<Friend, FriendViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FriendViewHolder holder, int position, @NonNull Friend model) {
                //get the item in the list
                String uidFriend = getSnapshots().getSnapshot(position).getId();
                holder.setList(uidFriend);

                // if someone clicks on the specific friend's name in the recyclerView
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // get document reference to the friend of the signed in user
                        DocumentReference docRef = db.collection("users").document(uid).collection("friend").document(uidFriend);

                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                        // get the chatroom id from the document -- id for user and friend's chats
                                        // call goChatRoom function
                                        goChatRoom(document.getString("idChatRoom"), uidFriend);
                                    } else {
                                        Log.d(TAG, "No such document");
                                    }
                                } else {
                                    Log.d(TAG, "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                });
            }

            @NonNull
            @Override
            public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_friend, parent, false);
                return new FriendViewHolder(view);
            }
        };

        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_messages);
        navigationView.setNavigationItemSelectedListener(MessagesActivity.this);

        signedInUserImageHeader = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageUserNameHeader);
        signedInUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.personName);
        signedInUserNameCommunity = (TextView) navigationView.getHeaderView(0).findViewById(R.id.communityName);

        updateDrawer();

        // Bind the adapter to the RV
        rvFriend.setAdapter(adapter);
        adapter.startListening();
        fabAdd = findViewById(R.id.fabAdd);
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



    // Called when user clicks fab to add new friend
    public void onClickFab(View view){
        // Dialog created to prompt user to enter username of friend
        final Dialog dialog = new Dialog(MessagesActivity.this);
        dialog.setTitle("Enter Username");
        dialog.setContentView(R.layout.dialog_add);
        dialog.show();

        TextView title = dialog.findViewById(R.id.textView2);
        final EditText editId = dialog.findViewById(R.id.editId);
        Button btnOk = dialog.findViewById(R.id.btnOk);

        title.setText("Enter Username");

        // set on click listener on ok button
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the entered username
                String userName = editId.getText().toString();
                // if no username provided, show error
                if (TextUtils.isEmpty(userName)){
                    editId.setError("required");
                } else {
                    // get the user doc where the username is equal to the entered username
                    db.collection("users").whereEqualTo("username", userName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            // if not found, show error
                            if(queryDocumentSnapshots.isEmpty()) {
                                editId.setError("Username is not found");
                            } else {
                                // loop through all of the documents in user
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                    String uidFriend = documentSnapshot.getId();
                                    // if the entered username matched the signed user's username, show error
                                    if(uid.equals(uidFriend)){
                                        editId.setError("wrong Id");
                                    } else {
                                        dialog.cancel();
                                        // check if friend exists
                                        checkFriendExist(uidFriend);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    private void checkFriendExist(final String uidFriend) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // check if entered user is a friend of the signed in user
                db.collection("users").document(uid).collection("friend").document().get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                // if entered user is already a friend, get the chatId value from the document field and call the goChatRoom function
                                String idChatRoom = documentSnapshot.get("idChatRoom", String.class);
                                goChatRoom(idChatRoom, uidFriend);
                            } else {
                                // if entered user is a new friend, call the createNewChatRoom function
                                createNewChatRoom(uidFriend);
                            }
                        }
                    }
                });
            }
        }).start();
    }

    // Responsible for managing the rows and keeps a track of everything inside the row (image, data, etc)
    public class FriendViewHolder extends RecyclerView.ViewHolder{
        View mView;
        ImageView imgProfile;
        TextView txtName;

        // Constructor
        // View Holder describes an item view and metadata about its place within the RecyclerView
        public FriendViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            imgProfile = mView.findViewById(R.id.imgProfile);
            txtName = mView.findViewById(R.id.txtName);
        }

        // get the username of the friend and set it to the textView
        public void setList(String uidFriend){
            db.collection("users").document(uidFriend).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()){
                            String name = documentSnapshot.get("username", String.class);
                            txtName.setText(name);
                            String profileUrl = documentSnapshot.get("profileImage", String.class);
                            GlideApp.with(getApplicationContext()).load(profileUrl).into(imgProfile);
                        }
                    }
                }
            });
        }
    }

    // Called when new chatRoom needs to be created
    private void createNewChatRoom(final String uidFriend) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // put timestamp valuee into HashMap
                HashMap<String,Object> dataChatRoom = new HashMap<>();
                dataChatRoom.put("dateAdded", FieldValue.serverTimestamp());
                // create chatroom collection doc with both uids (user and friend together)
                db.collection("chatroom").document(uid+uidFriend).set(dataChatRoom).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // write user data
                        HashMap<String, Object> dataFriend = new HashMap<>();
                        dataFriend.put("idChatRoom",uid+uidFriend);

                        // add the friend's uid to the signed in user's friends collection and put the chatroom id in it
                        db.collection("users").document(uid).collection("friend").document(uidFriend).set(dataFriend).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // write on user's friend data
                                HashMap<String, Object> dataUserFriend = new HashMap<>();
                                dataUserFriend.put("idChatRoom",uid+uidFriend);
                                // add the signed in user's uid to the friend's "friends" collection and put the chatroom id in it
                                db.collection("users").document(uidFriend).collection("friend").document(uid).set(dataUserFriend).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // once the chatroom has beeen created, call the function
                                        goChatRoom(uid+uidFriend,uidFriend);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }).start();
    }

    // Go to specific chat room based on chat room id
    private void goChatRoom(String idChatRoom, String uidFriend) {
        Intent i = new Intent(MessagesActivity.this,ChatActivity.class);
        // sending chat room id to next activity
        i.putExtra("idChatRoom", idChatRoom);
        // sending friend uid to next activity
        i.putExtra("uidFriend", uidFriend);
        startActivity(i);
    }

    // show menu posts and profile in actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_posts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // tells us which item the user selected in the menu
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If requests icon is chosen, go to requests activity and pass user uid
        if (item.getItemId() == R.id.menu_requests){
            Intent intent = new Intent(this, RequestsActivity.class);
            intent.putExtra(EXTRA_USERID,uid);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(this, Profile_Activity.class);
            startActivity(intent);
        } else if (id == R.id.nav_communities) {
            Intent intent = new Intent(this, CommunitiesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_messages) {
            Intent intent = new Intent(this, MessagesActivity.class);
            startActivity(intent);
            finish();
            // If logout option is chosen, sign out user and go to login activity
        } else if (id == R.id.nav_signout) {
            // Logout the user
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.nav_feed) {
            // Logout the user
            Intent intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutMessages);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // updates progre
    private void updateDrawer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // get the signed in user object from the database
                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get().addOnSuccessListener((userSnapshot) -> {
                    signedInUser = userSnapshot.toObject(User.class);
                    GlideApp.with(MessagesActivity.this).load(signedInUser.getProfileImage()).into(signedInUserImageHeader);
                    signedInUserNameCommunity.setText(signedInUser.getCommunity());
                    signedInUserName.setText(signedInUser.getUsername());
                    Log.i(TAG, "Signed In User" + signedInUser);
                }).addOnFailureListener((exception) -> {
                    Log.i(TAG, "Failure fetching signed in user" + exception);
                });
            }
        }).start();
    }

}
