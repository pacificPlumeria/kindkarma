package edu.neu.madcourse.kindkarma;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

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
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.Request;
import edu.neu.madcourse.kindkarma.models.User;

public class RequestsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    FirebaseFirestore firestoreDb;
    CollectionReference requestsReference;
    public List<Request> requestList;
    private final String TAG = "RequestsActivity";
    public ArrayList<Request> requests;
    private final String EXTRA_REQUESTERID = "EXTRA_REQUESTERID";
    private final String EXTRA_FAVORITES = "EXTRA_FAVORITES";
    private RecyclerView recyclerView;
    private RequestsAdapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    Button userRequest;
    Button allRequests;
    Button favoriteRequests;
    private String signedInUserUID;
    Toolbar toolbar;
    TextView signedInUserName, signedInUserNameCommunity;
    ImageView signedInUserImageHeader;
    User signedInUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        //final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        toolbar = (Toolbar) findViewById(R.id.requestsToolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutRequests);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) drawer.findViewById(R.id.nav_requests);
        navigationView.setNavigationItemSelectedListener(this);

        // Get buttons
        userRequest = (Button)findViewById(R.id.yourRequests);
        allRequests = (Button)findViewById(R.id.allRequests);
        favoriteRequests = (Button)findViewById(R.id.favoriteRequests);

        requests = new ArrayList<Request>();

        // Create the layout file which represents one row in the recyclerView
        // Create data source
        recyclerLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.rvRequests);
        // Create the adapter
        recyclerAdapter = new RequestsAdapter(this, requests);

        // Bind the adapter and layout manager to the recyclerView
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(recyclerLayoutManager);

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }


        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();

        // get the signed in user uid
        signedInUserUID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // want to get to requests collection
        requestsReference = firestoreDb.collection("requests");

        // show all requests and order by decreasing creation time of the post
        Query query = requestsReference.orderBy("creation_time_ms", Query.Direction.DESCENDING);

        // get requester id
        String requesterId = getIntent().getStringExtra(EXTRA_REQUESTERID);

        // get favorite value
        Boolean favorite = getIntent().getBooleanExtra(EXTRA_FAVORITES, false);

        // Used for the "your requests" activity -- shows all requests for logged in user
        if (requesterId != null) {
            // if on your requests activity, underline the button to indicate the tab you are on
            userRequest.setPaintFlags(userRequest.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            userRequest.setTypeface(null, Typeface.BOLD);
            query = query.whereEqualTo("requesterId", requesterId);

            query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    requests.clear();
                    for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                        Request request = documentSnapshot.toObject(Request.class);

                        request.setRequestId(documentSnapshot.getId().toString());
                        request.setShowRequestee(true);
                        requests.add(request);
                    }
                    recyclerAdapter.notifyDataSetChanged();
                }
            });
            // only show posts that have been favorited by the user
        } else if (favorite){
                // if on favorite requests activity, underline the button to indicate the tab you are on
                favoriteRequests.setPaintFlags(userRequest.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                favoriteRequests.setTypeface(null, Typeface.BOLD);
            query = query.whereArrayContains("favorite", signedInUserUID);
            // "Favorite Requests" Filter by requests' request categories that match a signed in user's skill categories
            query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.d(TAG, "Error:" + e.getMessage());
                    } else {
                        requests.clear();
                        for (DocumentSnapshot documentSnapshot : documentSnapshots) {

                            // convert the firestore request to Request object
                            Request request = documentSnapshot.toObject(Request.class);

                            // get the request document id from firebase and set it in the request object
                            request.setRequestId(documentSnapshot.getId().toString());

                            // get the signed in user document from firestore
                            firestoreDb.collection("users").document(signedInUserUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        if (documentSnapshot.exists()) {

                                            // Convert to user object
                                            User signedInUser = documentSnapshot.toObject(User.class);

                                            // get the signed in user's skill categories
                                            Map<String, String> signedInUserCategories = signedInUser.getCategories();
                                            Log.i("signed in user categories", signedInUserCategories.toString());

                                            // get the request's request categories
                                            Map<String, String> requestCategories = request.getRequestCategories();
                                            Log.i("request categories", requestCategories.toString());

                                            // loop through the signed in user's skill categories and check if the request categories contain the same key -- check if at least one category matches
                                            for (String signedInUserCategoryKey : signedInUserCategories.keySet()) {
                                                if (requestCategories.containsKey(signedInUserCategoryKey)) {
                                                    if (requests.contains(request)) {
                                                        // do nothing
                                                    } else {
                                                        // if at least one category matches, add the request to the list of request objects
                                                        requests.add(request);
                                                    }
                                                }
                                            }

                                        } else {
                                            // if entered user is a new friend, call the createNewChatRoom function
                                            Log.i("unsuccessful", "nope");
                                        }
                                    }
                                    recyclerAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                }
            });

        } else {
            // if on all requests activity, underline the button to indicate the tab you are on
            allRequests.setPaintFlags(allRequests.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            allRequests.setTypeface(null, Typeface.BOLD);
            // "All Requests" Filter by requests' request categories that match a signed in user's skill categories
            query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    requests.clear();
                    for(DocumentSnapshot documentSnapshot: documentSnapshots) {
                        Log.i("data", documentSnapshot.getData().toString());
                        // convert the firestore request to Request object
                        Request request = documentSnapshot.toObject(Request.class);

                        // get the request document id from firebase and set it in the request object
                        request.setRequestId(documentSnapshot.getId().toString());

                        // get the signed in user document from firestore
                        firestoreDb.collection("users").document(signedInUserUID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    DocumentSnapshot documentSnapshot = task.getResult();
                                    if (documentSnapshot.exists()){

                                        // Convert to user object
                                        User signedInUser =  documentSnapshot.toObject(User.class);

                                        // get the signed in user's skill categories
                                        Map<String, String> signedInUserCategories = signedInUser.getCategories();
                                        Log.i("signed in user categories", signedInUserCategories.toString());

                                        // get the request's request categories
                                        Map<String, String> requestCategories = request.getRequestCategories();
                                        Log.i("request categories", requestCategories.toString());

                                        // loop through the signed in user's skill categories and check if the request categories contain the same key -- check if at least one category matches
                                        for (String signedInUserCategoryKey : signedInUserCategories.keySet()){
                                            if (requestCategories.containsKey(signedInUserCategoryKey)){
                                                if (requests.contains(request)){
                                                    // do nothing
                                                } else {
                                                    // if at least one category matches, add the request to the list of request objects
                                                    requests.add(request);
                                                }
                                            }
                                        }

                                    } else {
                                        // if entered user is a new friend, call the createNewChatRoom function
                                        Log.i("unsuccessful", "nope");
                                    }
                                }
                                recyclerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }
            });
        }

        signedInUserImageHeader = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.imageUserNameHeader);
        signedInUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.personName);
        signedInUserNameCommunity = (TextView) navigationView.getHeaderView(0).findViewById(R.id.communityName);

        // get the signed in user object from the database
        firestoreDb.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnSuccessListener((userSnapshot) -> {
            signedInUser = userSnapshot.toObject(User.class);
            GlideApp.with(RequestsActivity.this).load(signedInUser.getProfileImage()).into(signedInUserImageHeader);
            signedInUserNameCommunity.setText(signedInUser.getCommunity());
            signedInUserName.setText(signedInUser.getUsername());
            Log.i(TAG, "Signed In User" + signedInUser);
        }).addOnFailureListener((exception) -> {
            Log.i(TAG, "Failure fetching signed in user" + exception);
        });

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


    // Function that is called when user clicks "your requests"
    public void onClickYourRequests(View view){
        // if on your requests activity, underline the button to indicate the tab you are on
        userRequest.setPaintFlags(userRequest.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        userRequest.setTypeface(null, Typeface.BOLD);
        Intent intent = new Intent(this, UserRequestsActivity.class);
        // send signed in user id to activity
        intent.putExtra(EXTRA_REQUESTERID, signedInUserUID);
        startActivity(intent);
        finish();
    }

    // Function that is called when user clicks "all requests"
    public void onClickAllRequests(View view){
        // if on all requests activity, underline the button to indicate the tab you are on
        allRequests.setPaintFlags(allRequests.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        allRequests.setTypeface(null, Typeface.BOLD);
        // Go back to this activity
        Intent intent = new Intent(RequestsActivity.this, RequestsActivity.class);
        startActivity(intent);
        finish();
    }

    // Function that is called when user clicks "favorites"
    public void onClickFavoriteRequests(View view){
        // if on your requests activity, underline the button to indicate the tab you are on
        favoriteRequests.setPaintFlags(userRequest.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        favoriteRequests.setTypeface(null, Typeface.BOLD);
        Intent intent = new Intent(this, FavoriteRequestsActivity.class);
        // send signed in user id to activity
        intent.putExtra(EXTRA_FAVORITES, true);
        startActivity(intent);
        finish();
    }

    // Function that is called when user clicks "all requests"
    public void onClickFAB(View view){
        // Go back to this activity
        Intent intent = new Intent(RequestsActivity.this, CreateRequest.class);
        startActivity(intent);
        finish();
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
            intent.putExtra("EXTRA_USERID",signedInUserUID);
            startActivity(intent);
            finish();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layoutRequests);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}