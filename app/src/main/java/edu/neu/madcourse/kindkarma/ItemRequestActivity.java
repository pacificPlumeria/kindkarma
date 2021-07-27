package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.neu.madcourse.kindkarma.models.Comment;
import edu.neu.madcourse.kindkarma.models.User;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ItemRequestActivity extends AppCompatActivity {
    String requestTitle;
    String requestBody;
    String requestImageUrl;
    Long requestCreationTimeMs;
    int requestLastSpinnerLocation;
    String requestStatus;
    ArrayList<String> requestFavorite;
    HashMap<String,String> requestCategories;
    String requesterId;
    Boolean showRequestee;
    String requestId;
    FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
    DocumentReference usersDocReference;
    private final String TAG = "ItemPostActivity";
    User user;
    TextView userName;
    ImageView requestProfilePic;
    TextView tvRequestBody;
    TextView tvRequestTitle;
    TextView requestRelativeTime;
    ImageView requestImage;
    PhotoViewAttacher mAttacher;
    ImageView requestItemFavorite;
    TextView tvRequestStatus;
    TextView tvRequestStartDate;
    TextView tvRequestEndDate;
    public LinearLayout requestLayout;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUser = mAuth.getCurrentUser().getUid();
    private EditText editComment;
    private ImageButton imbSend;
    public ArrayList<Comment> comments;
    private RecyclerView recyclerView;
    private PostCommentAdapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    CollectionReference commentsReference;
    Boolean requestOnline;
    ImageButton overflowButton;
    String requestStartDate, requestEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_request);

        Bundle bundle = getIntent().getExtras();

        requestTitle = bundle.getString("REQUEST_TTILE");
        requestBody = bundle.getString("REQUEST_BODY");
        requestCreationTimeMs = bundle.getLong("REQUEST_CREATIONTIMEMS");
        requestImageUrl = bundle.getString("REQUEST_IMAGEURL");
        requestLastSpinnerLocation = bundle.getInt("REQUEST_LASTSPINNERLOCATION");
        requestStatus = bundle.getString("REQUEST_STATUS");
        requestFavorite = bundle.getStringArrayList("REQUEST_FAVORITE");
        requestCategories = (HashMap<String,String>) bundle.getSerializable("REQUEST_REQUESTCATEGORIES");
        requesterId = bundle.getString("REQUEST_REQUESTERID");
        showRequestee = bundle.getBoolean("REQUEST_SHOWREQUESTEE");
        requestId = bundle.getString("REQUEST_REQUESTID");
        requestOnline = bundle.getBoolean("REQUEST_ONLINE");
        requestId = bundle.getString("REQUEST_REQUESTID");
        requestStartDate = bundle.getString("REQUEST_STARTDATE");
        requestEndDate = bundle.getString("REQUEST_ENDDATE");

        userName = (TextView)findViewById(R.id.requestUsername);
        requestProfilePic = (ImageView)findViewById(R.id.requestProfileImage);
        tvRequestBody = (TextView)findViewById(R.id.requestBody);
        tvRequestTitle = (TextView)findViewById(R.id.requestTitle);
        requestRelativeTime = (TextView)findViewById(R.id.requestRelativeTime);
        requestImage = (ImageView)findViewById(R.id.requestImage);
        requestItemFavorite = (ImageView)findViewById(R.id.requestFavorite);
        tvRequestStatus = (TextView)findViewById(R.id.requestStatus);
        requestLayout = (LinearLayout) findViewById(R.id.linearRequest);
        tvRequestStartDate = (TextView) findViewById(R.id.tvStartDate);
        tvRequestEndDate = (TextView) findViewById(R.id.tvEndDate);
        overflowButton = (ImageButton)findViewById(R.id.overflowButton_itemrequest);


        editComment = findViewById(R.id.editRequestComment);
        imbSend = findViewById(R.id.commentRequestSend);

        comments = new ArrayList<Comment>();
        recyclerLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.rvRequestComments);
        recyclerAdapter = new PostCommentAdapter(this, comments);

        // Bind the adapter and layout manager to the RV
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(recyclerLayoutManager);

        isFavorite(requestId, requestItemFavorite);

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }


        // want to get to comments collection
        commentsReference = firestoreDb.collection("requests").document(requestId).collection("comments");

        // show all comments and order by decreasing creation time of the post
        Query query = commentsReference.orderBy("creation_time_ms", Query.Direction.DESCENDING);

        query.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                comments.clear();
                for (DocumentSnapshot documentSnapshot : documentSnapshots) {
                    if (documentSnapshot.getData() != null) {
                        Comment comment = documentSnapshot.toObject(Comment.class);
                        comments.add(comment);
                    }
                }
                recyclerAdapter.notifyDataSetChanged();
            }
        });

        // Sets starting and ending dates
        try {
            if (!requestStartDate.equals("")) {
                tvRequestStartDate.setText(requestStartDate);
            }
            if (!requestEndDate.equals("")) {
                tvRequestEndDate.setText(requestEndDate);
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            TextView hyphen = (TextView) findViewById(R.id.requestDateHyphen);
            hyphen.setText("");
        }

        // sets username in post
        setUserName(requesterId);

        // new data is bound to view holder
        // set the description in the object to the textview in the post
        tvRequestTitle.setText(requestTitle);
        tvRequestBody.setText(requestBody);
        // if an image is not uploaded, make the imageview disappear
        if (requestImageUrl.equals("")){
            requestImage.setVisibility(View.GONE);
        } else {
            // if an image is uploaded, load image into imageview in post using the url
            GlideApp.with(getApplicationContext()).load(requestImageUrl).into(requestImage);
        }

        if (currentUser.equalsIgnoreCase(requesterId)){
            overflowButton.setVisibility(View.VISIBLE);
        }
        // set the time since creation in the textview in the post
        requestRelativeTime.setText(DateUtils.getRelativeTimeSpanString(requestCreationTimeMs));
        tvRequestStatus.setText(requestStatus);

        // set the text of the textView for the status of the request and change visual attributes
        GradientDrawable gd1 = new GradientDrawable();
        tvRequestStatus.setText(requestStatus);
        tvRequestStatus.setTypeface(tvRequestStatus.getTypeface(), Typeface.BOLD);
        tvRequestStatus.setTextColor(getApplication().getResources().getColor(R.color.white));
        tvRequestStatus.setPadding(3, 2, 5, 3);

        // change color of the background based on request status type
        if (requestStatus.equalsIgnoreCase("active")){
            gd1.setColor(Color.GREEN);
            gd1.setCornerRadius(5);
            gd1.setStroke(1, Color.GREEN);
            tvRequestStatus.setBackground(gd1);
        } else if (requestStatus.equalsIgnoreCase("pending")){
            gd1.setColor(Color.CYAN);
            gd1.setCornerRadius(5);
            gd1.setStroke(1, Color.CYAN);
            tvRequestStatus.setBackground(gd1);
        } else if (requestStatus.equalsIgnoreCase("complete")){
            gd1.setColor(Color.RED);
            gd1.setCornerRadius(5);
            gd1.setStroke(1, Color.RED);
            tvRequestStatus.setBackground(gd1);
        }

        imbSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get entered message
                String message = editComment.getText().toString().trim();
                if (TextUtils.isEmpty(message)){
                    // do nothing
                } else {
                    HashMap<String,Object> dataMessage = new HashMap<>();
                    // put all message contents in HashMap
                    dataMessage.put("creation_time_ms", System.currentTimeMillis());
                    dataMessage.put("comment",message);
                    dataMessage.put("uid",currentUser);
                    // add the values to the database
                    firestoreDb.collection("requests").document(requestId).collection("comments").document().set(dataMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // if successful, clear the editText
                            editComment.setText("");
                        }
                    });
                }
            }
        });

        // does not show favorite option if request is a user's request
        if (showRequestee == true){
            requestItemFavorite.setVisibility(View.GONE);
            //set the vis true...
        }

        // set an onclick listener on the like imageView
        requestItemFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get document reference to requests doc for chosen request
                DocumentReference docRef = firestoreDb.collection("requests").document(requestId);

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                // get the array of users that have favorited the post in the favorite field in the doc
                                ArrayList<String> favoritedUsers = (ArrayList<String>) documentSnapshot.get("favorite");
                                // if the user has previously favorited  the reequest and they have now clicked the favorite button
                                if (favoritedUsers.contains(currentUser)){
                                    // remove the user from the array
                                    favoritedUsers.remove(currentUser);
                                    // update the array in the database and object
                                    HashMap<String, Object> dataUnfavoritedUser = new HashMap<>();
                                    dataUnfavoritedUser.put("favorite",favoritedUsers);
                                    docRef.update(dataUnfavoritedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // set the image to the unfavorited image (not filled)
                                            requestItemFavorite.setImageResource(R.drawable.ic_favorite);
                                        }
                                    });
                                    // if the user has not favorited the request and they have now clicked the favorite button
                                } else {
                                    // add the current user to the array
                                    favoritedUsers.add(currentUser);
                                    // update the array in the database and object
                                    HashMap<String, Object> dataAddFavoritedUser = new HashMap<>();
                                    dataAddFavoritedUser.put("favorite",favoritedUsers);
                                    docRef.update(dataAddFavoritedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // set the image to the favorited image (filled)
                                            requestItemFavorite.setImageResource(R.drawable.ic_favorited);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        });

        // Loop through the HashMap of request categories assigned to the request by the requester
        // get the signed in user document from firestore
        firestoreDb.collection("users").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){

                        // Convert to user object
                        User signedInUser =  documentSnapshot.toObject(User.class);

                        // get the signed in user's skill categories
                        Map<String, String> signedInUserCategories = signedInUser.getCategories();
                        Log.i("signed in user skills", signedInUserCategories.toString());

                        // get the request's request categories
                        //Map<String, String> requestCategories = requests.get(position).getRequestCategories();
                        ArrayList<String> requestsAlreadyDisplayed = new ArrayList<>();
                        //Log.i("request category skills", requests.get(position).getRequestCategories().toString());

                        // clears all category tags before adding them again
                        requestLayout.removeAllViews();

                        int i = 0;

                        if (requestOnline == true){
                            // create a new text view for each category
                            TextView onlinetextView = new TextView(getApplicationContext());
                            // set the id of the newly created textView
                            onlinetextView.setId(i);
                            // set padding for the textView
                            onlinetextView.setPadding(10, 5, 10, 5);

                            // Allows for a margin between the textview items in the horizontal linear layout
                            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            layoutParams2.setMargins(0, 20, 10, 5);

                            // creates a border, background, and text color for the textview
                            GradientDrawable gd = new GradientDrawable();
                            gd.setCornerRadius(5);
                            gd.setStroke(1, 0xFF000000);
                            gd.setColor(Color.GREEN); // Changes this drawabale to use a single color instead of a gradient
                            onlinetextView.setTextColor(getApplicationContext().getColor(R.color.white));
                            onlinetextView.setTypeface(onlinetextView.getTypeface(), Typeface.BOLD);
                            onlinetextView.setBackground(gd);

                            // set the value of the textView
                            onlinetextView.setText("online");

                            // add the textView to the linear layout of the request item
                            requestLayout.addView(onlinetextView, layoutParams2);
                        }

                        // loop through the signed in user's skill categories
                        for (String signedInUserCategoryKey : signedInUserCategories.keySet()){
                            // loop through the request categories
                            for (Map.Entry<String, String> entry : requestCategories.entrySet()) {
                                // check if at least one category matches
                                if (entry.getKey().equalsIgnoreCase(signedInUserCategoryKey) && !requestsAlreadyDisplayed.contains(entry.getKey())) {
                                    // create a new text view for each category
                                    TextView textView = new TextView(getApplicationContext());
                                    // set the id of the newly created textView
                                    textView.setId(i);
                                    // set padding for the textView
                                    textView.setPadding(10, 5, 10, 5);

                                    // Allows for a margin between the textview items in the horizontal linear layout
                                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                    layoutParams.setMargins(0, 20, 10, 5);

                                    // creates a border, background, and text color for the textview
                                    GradientDrawable gd = new GradientDrawable();
                                    gd.setCornerRadius(5);
                                    gd.setStroke(1, 0xFF000000);
                                    requestsAlreadyDisplayed.add(entry.getKey());
                                    gd.setColor(getApplicationContext().getResources().getColor(R.color.purple_500)); // Changes this drawabale to use a single color instead of a gradient
                                    textView.setTextColor(getApplicationContext().getResources().getColor(R.color.white));
                                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                                    textView.setBackground(gd);

                                    // set the value of the textView
                                    String value = entry.getValue();
                                    Log.i("matches user skills", entry.getValue().toString());
                                    textView.setText(value);

                                    // add the textView to the linear layout of the request item
                                    requestLayout.addView(textView, layoutParams);

                                }
                                //increment so that ids of the textviews are different
                                i++;
                            }
                        }
                        // loop through the request categories
                        for (Map.Entry<String, String> entry : requestCategories.entrySet()){
                            // check if the categories left have not been displayed already -- if they haven't then they don't match a user skill
                            if (!requestsAlreadyDisplayed.contains(entry.getKey())) {
                                // create a new text view for each category
                                TextView textView = new TextView(getApplicationContext());
                                // set the id of the newly created textView
                                textView.setId(i);
                                // set padding for the textView
                                textView.setPadding(10, 5, 10, 5);

                                // Allows for a margin between the textview items in the horizontal linear layout
                                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                layoutParams.setMargins(0, 20, 10, 5);

                                // creates a border, background, and text color for the textview
                                GradientDrawable gd = new GradientDrawable();
                                gd.setCornerRadius(5);
                                gd.setStroke(1, 0xFF000000);

                                requestsAlreadyDisplayed.add(entry.getKey());
                                gd.setColor(getApplicationContext().getResources().getColor(R.color.white)); // Changes this drawabale to use a single color instead of a gradient
                                textView.setTextColor(getApplicationContext().getResources().getColor(R.color.purple_500));
                                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                                textView.setBackground(gd);

                                // set the value of the textView
                                String value = entry.getValue();
                                Log.i("does not match user skills", entry.getValue().toString());
                                textView.setText(value);

                                // add the textView to the linear layout of the request item
                                requestLayout.addView(textView, layoutParams);
                            }
                            i++;
                        }
                        // clear the arraylist
                        requestsAlreadyDisplayed.clear();

                    } else {
                        // if entered user is a new friend, call the createNewChatRoom function
                        Log.i("unsuccessful", "nope");
                    }
                }
            }
        });

        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ItemRequestActivity.this, overflowButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("Delete Request")) {
                            firestoreDb.collection("requests").document(requestId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                            //recyclerAdapter.notifyDataSetChanged();
                                            Intent intent = new Intent(ItemRequestActivity.this,RequestsActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error deleting document", e);
                                        }
                                    });
                        } else if (item.getTitle().equals("Edit Request")){
                            Intent intent = new Intent(ItemRequestActivity.this,CreateRequest.class);
                            intent.putExtra("REQUEST_TTILE", requestTitle);
                            intent.putExtra("REQUEST_BODY", requestBody);
                            intent.putExtra("REQUEST_CREATIONTIMEMS", requestCreationTimeMs);
                            intent.putExtra("REQUEST_IMAGEURL", requestImageUrl);
                            intent.putExtra("REQUEST_LASTSPINNERLOCATION", requestLastSpinnerLocation);
                            intent.putExtra("REQUEST_STATUS", requestStatus);
                            intent.putExtra("REQUEST_FAVORITE", requestFavorite);
                            intent.putExtra("REQUEST_REQUESTCATEGORIES", requestCategories);
                            intent.putExtra("REQUEST_REQUESTERID", requesterId);
                            intent.putExtra("REQUEST_SHOWREQUESTEE", showRequestee);
                            intent.putExtra("REQUEST_REQUESTID", requestId);
                            intent.putExtra("REQUEST_STARTDATE", requestStartDate);
                            intent.putExtra("REQUEST_ENDDATE", requestEndDate);
                            intent.putExtra("REQUEST_ONLINE", requestOnline);
                            //intent.putExtra("POST_COMMENTS",comments);
                            startActivity(intent);
                            finish();
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
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

    private void setUserName(String postPosterId){
        // Get reference to the user doc in the database that matches the id of the person who posted
        usersDocReference = firestoreDb.collection("users").document(postPosterId);

        usersDocReference.addSnapshotListener(new EventListener<DocumentSnapshot>()  {
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    // convert user doc to user object
                    user = snapshot.toObject(User.class);
                    // get the username of the user and set it to the textview in the post
                    userName.setText(user.getUsername());
                    // call getProfileImageUrl and load image into profile pic imageview in post using returned url
                    //GlideApp.with(getApplicationContext()).load(getProfileImageUrl(user.getUsername())).into(requestProfilePic);
                    GlideApp.with(getApplicationContext()).load(user.getProfileImage()).into(requestProfilePic);
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
    }

    // open dialog when image is clicked
    public void onClick(View view) {
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        loadPhoto(requestImage,width,height);
    }

    // source - https://stackoverflow.com/questions/7693633/android-image-dialog-popup
    private void loadPhoto(ImageView imageView, int width, int height) {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.fullimage_dialog,
                (ViewGroup) findViewById(R.id.layout_root));
        ImageView image = (ImageView) layout.findViewById(R.id.fullimage);
        image.setImageDrawable(imageView.getDrawable());
        image.getLayoutParams().height = height;
        image.getLayoutParams().width = width;
        mAttacher = new PhotoViewAttacher(image);
        image.requestLayout();
        dialog.setContentView(layout);
        dialog.show();
    }

    // displays the correct imageView depending on if user previously favorited or unfavorited the post when the activity opens
    private void isFavorite(String requestId, final ImageView imageView){

        DocumentReference docRef = firestoreDb.collection("requests").document(requestId);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        // get the array of users that have favorited the post in the favorite field in the doc
                        ArrayList<String> favoritedUsers = (ArrayList<String>) documentSnapshot.get("favorite");
                        // if the user has previously favorited  the request
                        if (favoritedUsers.contains(currentUser)){
                            // show favorited imageView (filled)
                            imageView.setImageResource(R.drawable.ic_favorited);
                            // show unfavorited imageView (unfilled)
                        } else {
                            imageView.setImageResource(R.drawable.ic_favorite);
                        }
                    }
                }
            }
        });
    }

    // refreshes feed activity
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, FeedActivity.class));
    }
}