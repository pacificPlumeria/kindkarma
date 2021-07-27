package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.kindkarma.models.Community;
import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.User;

public class CommunitiesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    FirebaseFirestore firestoreDb;
    CollectionReference communitiesReference;
    CollectionReference countyReference;
    private final String TAG = "CommunitiesActivity";
    public List<Community> communitiesList;
    public ArrayList<Community> communities;
    private RecyclerView recyclerView;
    private final String EXTRA_USERID = "EXTRA_USERID";
    private CommunitiesAdapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    private String signedInUserUID;
    Toolbar toolbar;
    TextView signedInUserName, signedInUserNameCommunity;
    ImageView signedInUserImageHeader;
    User signedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communities);

        toolbar = (Toolbar) findViewById(R.id.communitiesToolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutCommunities);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_viewCommunities);
        navigationView.setNavigationItemSelectedListener(this);

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        communities = new ArrayList<Community>();

        // Create the layout file which represents one row in the recyclerView
        // Create data source
        // Create the adapter
        recyclerLayoutManager = new LinearLayoutManager(this);

        recyclerView = drawer.findViewById(R.id.rvCommunities);
        recyclerAdapter = new CommunitiesAdapter(this, communities);

        // Bind the adapter and layout manager to the RV
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(recyclerLayoutManager);

        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();

        // get the user id of the logged in user
        signedInUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();


        signedInUserImageHeader = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageUserNameHeader);
        signedInUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.personName);
        signedInUserNameCommunity = (TextView) navigationView.getHeaderView(0).findViewById(R.id.communityName);

        updateCommunitiesRecyclerView();

        updateDrawer();
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
            intent.putExtra(EXTRA_USERID,signedInUserUID);
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
            finish();
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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutCommunities);
        drawer.closeDrawer(GravityCompat.START);
        return true;
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


    // updates progre
    private void updateDrawer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // get the signed in user object from the database
                firestoreDb.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .get().addOnSuccessListener((userSnapshot) -> {
                    signedInUser = userSnapshot.toObject(User.class);
                    GlideApp.with(CommunitiesActivity.this).load(signedInUser.getProfileImage()).into(signedInUserImageHeader);
                    signedInUserNameCommunity.setText(signedInUser.getCommunity());
                    signedInUserName.setText(signedInUser.getUsername());
                    Log.i(TAG, "Signed In User" + signedInUser);
                }).addOnFailureListener((exception) -> {
                    Log.i(TAG, "Failure fetching signed in user" + exception);
                });
            }
        }).start();
    }

    // updates progre
    private void updateCommunitiesRecyclerView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // want to get to community collection
                communitiesReference = firestoreDb.collection("community");

                // show all communities and order by decreasing total community hours
                Query query = communitiesReference.orderBy("totalCommunityHours", Query.Direction.DESCENDING);

                query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null || value == null){
                            Log.e(TAG, "Exception when querying posts", error);
                            return;
                        }
                        // create a list of community objects
                        communitiesList = value.toObjects(Community.class);
                        Log.i("communities", communitiesList.toString());
                        communities.clear();
                        // add all of the community objects to the communities list
                        communities.addAll(communitiesList);
                        recyclerAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();
    }
}