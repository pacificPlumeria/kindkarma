package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.neu.madcourse.kindkarma.models.Community;
import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.User;

public class FeedActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    FirebaseFirestore firestoreDb;
    CollectionReference postsReference;
    private final String TAG = "FeedActivity";
    public List<Post> postList;
    public ArrayList<Post> posts;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private final String EXTRA_USERID = "EXTRA_USERID";
    private User signedInUser = null;
    public FloatingActionButton fabCreate;
    private String signedInUserUID;
    ProgressBar progressbar;
    TextView progressPercentage;
    ImageView help;
    Toolbar toolbar;
    TextView signedInUserName, signedInUserNameCommunity;
    ImageView signedInUserImageHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        fabCreate = (FloatingActionButton)findViewById(R.id.fabCreate);

        progressbar =(ProgressBar)findViewById(R.id.progressBar);

        progressPercentage = (TextView) findViewById(R.id.tvProgressPercentage);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // question icon
        help = (ImageView) findViewById(R.id.help);
        TooltipCompat.setTooltipText(help, "The numbers of hours you have worked vs the avg hours a member in your community worked.");


        posts = new ArrayList<Post>();

        // Create the layout file which represents one row in the recyclerView
        // Create data source
        // Create the adapter
        recyclerLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.rvPosts);
        recyclerAdapter = new RecyclerAdapter(this, posts);

        // Bind the adapter and layout manager to the RV
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(recyclerLayoutManager);

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        signedInUserImageHeader = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageUserNameHeader);
        signedInUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.personName);
        signedInUserNameCommunity = (TextView) navigationView.getHeaderView(0).findViewById(R.id.communityName);

        // get the user id of the logged in user
        signedInUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        updateRecyclerView();
        Log.d(TAG, "ABOUT TO CALL PROGRESS BAR");
        updateProgressBar();

        updateDrawer();

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


    // If fab is clicked, redirect user to create activity
    public void fabClick(View view){
        Intent intent = new Intent(this, CreateActivity.class);
        startActivity(intent);
        finish();
    }



    // show menu posts and profile in actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_posts, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    // tells us which item the user selected in the menu
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If requests icon is chosen, go to requests activity and pass user uid
        if (item.getItemId() == R.id.menu_requests){
            Intent intent = new Intent(this, RequestsActivity.class);
            intent.putExtra(EXTRA_USERID,signedInUserUID);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    // updates progre
    private void updateProgressBar() {
        Log.d(TAG, "IN PROGRESS BAR");
        new Thread(new Runnable() {
            @Override
            public void run() {
                // get the signed in user document from firestore
                firestoreDb.collection("users").document(signedInUserUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {

                                // Convert to user object
                                User signedInUser = documentSnapshot.toObject(User.class);

                                // get total hours worked for signed in user
                                int signInUserTotalHoursWorked = (int) signedInUser.getTotalHoursWorked();

                                // get reference to community doc for signed in user
                                DocumentReference docRef = firestoreDb.collection("community").document(signedInUser.getCommunity() + signedInUser.getState());

                                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            if (documentSnapshot.exists()) {
                                                // get total number of users for that community
                                                long members = documentSnapshot.getLong("totalUsers");
                                                // get total community hours for the signed in user's community
                                                long communityHours = documentSnapshot.getLong("totalCommunityHours");
                                                // get average number of hours worked for a member of that community
                                                int avgCommunityHoursPerMember = (int) (communityHours/members);
                                                // gets the percentage that the signed in user has worked in comparison to the average number of hours worked for a member of that community
                                                Double avgCommunityHoursPerMemberPercentage = (double) signInUserTotalHoursWorked/communityHours * 100;
                                                int roundedPercentage = (int) Math.round(avgCommunityHoursPerMemberPercentage);

                                                // set the values to progress bar and percentage textview
                                                progressbar.setMax((int)communityHours);
                                                Log.d(TAG," PROGRESS AVG COMM HOURS PER MEM" +avgCommunityHoursPerMember + "");
                                                progressbar.setProgress(signInUserTotalHoursWorked);
                                                Log.d(TAG, "PROGRESS HOURS WORKED" +signInUserTotalHoursWorked + "");
                                                if (roundedPercentage > 100){
                                                    progressPercentage.setText("100%");
                                                } else {
                                                    progressPercentage.setText(String.valueOf(roundedPercentage) + "%");
                                                }
                                            }
                                        }
                                    }
                                });

                            } else {
                                Log.i("unsuccessful", "nope");
                            }
                        }
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

    // updates progre
    private void updateDrawer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // get the signed in user object from the database
                firestoreDb.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get().addOnSuccessListener((userSnapshot) -> {
                    signedInUser = userSnapshot.toObject(User.class);
                    GlideApp.with(FeedActivity.this).load(signedInUser.getProfileImage()).into(signedInUserImageHeader);
                    signedInUserNameCommunity.setText(signedInUser.getCommunity());
                    signedInUserName.setText(signedInUser.getUsername());
                    Log.i(TAG, "Signed In User" + signedInUser);
                }).addOnFailureListener((exception) -> {
                    Log.i(TAG, "Failure fetching signed in user" + exception);
                });
            }
        }).start();
    }

    public void onClickHelp(View view){
        view.performLongClick();
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
            // If logout option is chosen, sign out user and go to login activity
        } else if (id == R.id.nav_signout) {
            // Logout the user
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.nav_feed) {
            // Logout the user
            Intent intent = new Intent(this, FeedActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // updates progre
    private void updateRecyclerView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // want to get to posts collection
                postsReference = firestoreDb.collection("posts");

                // show all posts and order by decreasing creation time of the post
                Query query = postsReference.orderBy("creation_time_ms", Query.Direction.DESCENDING);

                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null || value == null){
                            Log.e(TAG, "Exception when querying posts", error);
                            return;
                        }
                        // create a list of post objects
                        postList = value.toObjects(Post.class);
                        posts.clear();
                        // add all of the post object to the posts list
                        posts.addAll(postList);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }

}

