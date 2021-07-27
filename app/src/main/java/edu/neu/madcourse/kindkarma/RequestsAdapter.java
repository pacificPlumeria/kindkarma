package edu.neu.madcourse.kindkarma;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.StrictMode;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.Request;
import edu.neu.madcourse.kindkarma.models.User;

public class RequestsAdapter extends RecyclerView.Adapter<RequestViewHolder>{
    private final ArrayList<Request> requests;
    private ListItemClickListener listener;
    public Context mContext;
    FirebaseFirestore firestoreDb;
    DocumentReference usersDocReference;
    private final String TAG = "RequestsAdapter";
    public User user;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    Request requestItem;
    long totalHoursWorked;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    // Please add the server key from your firebase console in the follwoing format "key=<serverKey>"
    //private static final String SERVER_KEY = "key=AAAAQq6C_Wo:APA91bF-83JCqjVbED5GJ2zfC5xKJeQ5clZNbMz8fWM8x3jVEjjHcDFzEse1Da0dkjrClUajd-IwnUgVCQIuMK4Rjupt7vnRbqiRRAMvZ8XQgLSEQO44E4K_Eitoa4rBrnt_0ZTRyNZF";
    // This is the client registration token
    //private static final String CLIENT_REGISTRATION_TOKEN = "dW584Ab0JoY:APA91bGv2dltU6sR5_cM3ii1Y5iQVaxMdk4Mn7b7OPtsIhssBmNzpxk9t9kB-iKziXUic1z2TGPYJgbGSu0aGLyA3d4cwsyj3IRJr4pzEXT6TiGmFXF3BllxAnquEEgTPFZ0ffDU6KYO";
    //private String CLIENT_REGISTRATION_TOKEN;
    //private String channelName = "";
    private static final String SERVER_KEY = "key=AAAAQq6C_Wo:APA91bF-83JCqjVbED5GJ2zfC5xKJeQ5clZNbMz8fWM8x3jVEjjHcDFzEse1Da0dkjrClUajd-IwnUgVCQIuMK4Rjupt7vnRbqiRRAMvZ8XQgLSEQO44E4K_Eitoa4rBrnt_0ZTRyNZF";

    // Constructor
    public RequestsAdapter(Context context, ArrayList<Request> requests) {
        this.mContext = context;
        this.requests = requests;
    }

    public void setOnListItemClickListener(ListItemClickListener listener) {
        this.listener = listener;
    }

    //Create individual rows that are necessary for displaying the items in the RecyclerView
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_request, parent, false);
        RequestViewHolder viewHolder = new RequestViewHolder(view, listener);
        return viewHolder;
    }

    // When rows that have old data scroll off screen, they will be replaced with new data
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();
        // get the item the list
        requestItem = requests.get(position);

        // Get reference to the user doc in the database that matches the id of the person who posted the request
        usersDocReference = firestoreDb.collection("users").document(requestItem.getRequesterId());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // sets spinner values
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(mContext, R.array.statusSpinnerItems, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.itemStatusSpinner.setAdapter(spinnerAdapter);

        // sets request dates if user has entered them
        try {
            if (!requestItem.getStartDate().equals("")) {
                holder.itemStartDate.setText(requestItem.getStartDate());
            }
            if (!requestItem.getEndDate().equals("")) {
                holder.itemEndDate.setText(requestItem.getEndDate());
            }
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            holder.itemStartDate.setText("");
            holder.itemEndDate.setText("");
            holder.itemDateHyphen.setText("");
        }

        // does not show favorite option if request is a user's request
        if (requestItem.getShowRequestee() == true){
            holder.itemFavorite.setVisibility(View.GONE);
        }

        // call isFavorite method to display the correct imageView depending on if user previously favorited or unfavorited the post
        isFavorite(requests.get(position).getRequestId(), holder.itemFavorite);

        // set an onclick listener on the like imageView
        holder.itemFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get document reference to requests doc for chosen request
                DocumentReference docRef = firestoreDb.collection("requests").document(requests.get(position).getRequestId());

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                // get the array of users that have favorited the post in the favorite field in the doc
                                ArrayList<String> favoritedUsers = (ArrayList<String>) documentSnapshot.get("favorite");
                                // if the user has previously favorited  the reequest and they have now clicked the favorite button
                                if (favoritedUsers.contains(currentUser.getUid())){
                                    // remove the user from the array
                                    favoritedUsers.remove(currentUser.getUid());
                                    // update the array in the database and object
                                    HashMap<String, Object> dataUnfavoritedUser = new HashMap<>();
                                    dataUnfavoritedUser.put("favorite",favoritedUsers);
                                    requestItem.setFavorite(favoritedUsers);
                                    docRef.update(dataUnfavoritedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // set the image to the unfavorited image (not filled)
                                            holder.itemFavorite.setImageResource(R.drawable.ic_favorite);
                                        }
                                    });
                                    // if the user has not favorited the request and they have now clicked the favorite button
                                } else {
                                    // add the current user to the array
                                    favoritedUsers.add(currentUser.getUid());
                                    // update the array in the database and object
                                    HashMap<String, Object> dataAddFavoritedUser = new HashMap<>();
                                    dataAddFavoritedUser.put("favorite",favoritedUsers);
                                    requestItem.setFavorite(favoritedUsers);
                                    docRef.update(dataAddFavoritedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // set the image to the favorited image (filled)
                                            holder.itemFavorite.setImageResource(R.drawable.ic_favorited);
                                        }
                                    });
                                }
                            }
                        }
                    }
                });
            }
        });


        usersDocReference.addSnapshotListener(new EventListener<DocumentSnapshot>()  {
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Log.i("hi", "hi");
            }

            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, source + " data: " + snapshot.getData());
                    // convert user doc to user object
                    user = snapshot.toObject(User.class);
                    // get the username of the user and set it to the textview in the post
                    holder.itemUserName.setText(user.getUsername());
                    // call getProfileImageUrl and load image into profile pic imageview in post using returned url
                    ///GlideApp.with(mContext.getApplicationContext()).load(getProfileImageUrl(user.getUsername())).into(holder.itemProfileImage);
                    GlideApp.with(mContext).load(user.getProfileImage()).into(holder.itemProfileImage);

                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });

        // Loop through the HashMap of request categories assigned to the request by the requester
        // get the signed in user document from firestore
        String uid = currentUser.getUid();
        firestoreDb.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                        Map<String, String> requestCategories = requests.get(position).getRequestCategories();
                        ArrayList<String> requestsAlreadyDisplayed = new ArrayList<>();
                        Log.i("request category skills", requests.get(position).getRequestCategories().toString());

                        // clears all category tags before adding them again
                        holder.itemLayout.removeAllViews();
                        int i = 0;

                        if (requests.get(position).getOnline() == true){
                            // create a new text view for each category
                            TextView onlinetextView = new TextView(mContext);
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
                            onlinetextView.setTextColor(mContext.getResources().getColor(R.color.white));
                            onlinetextView.setTypeface(onlinetextView.getTypeface(), Typeface.BOLD);
                            onlinetextView.setBackground(gd);

                            // set the value of the textView
                            onlinetextView.setText("online");

                            // add the textView to the linear layout of the request item
                            holder.itemLayout.addView(onlinetextView,  layoutParams2);
                        }

                        // loop through the signed in user's skill categories
                        for (String signedInUserCategoryKey : signedInUserCategories.keySet()){
                            // loop through the request categories
                            for (Map.Entry<String, String> entry : requests.get(position).getRequestCategories().entrySet()) {
                                // check if at least one category matches
                                if (entry.getKey().equalsIgnoreCase(signedInUserCategoryKey) && !requestsAlreadyDisplayed.contains(entry.getKey())) {
                                    // create a new text view for each category
                                    TextView textView = new TextView(mContext);
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
                                    gd.setColor(mContext.getResources().getColor(R.color.purple_500)); // Changes this drawabale to use a single color instead of a gradient
                                    textView.setTextColor(mContext.getResources().getColor(R.color.white));
                                    textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                                    textView.setBackground(gd);

                                    // set the value of the textView
                                    String value = entry.getValue();
                                    Log.i("matches user skills", entry.getValue().toString());
                                    textView.setText(value);

                                    // add the textView to the linear layout of the request item
                                    holder.itemLayout.addView(textView, layoutParams);

                                }
                                //increment so that ids of the textviews are different
                                i++;
                            }
                        }
                        // loop through the request categories
                        for (Map.Entry<String, String> entry : requests.get(position).getRequestCategories().entrySet()){
                            // check if the categories left have not been displayed already -- if they haven't then they don't match a user skill
                                if (!requestsAlreadyDisplayed.contains(entry.getKey())) {
                                // create a new text view for each category
                                TextView textView = new TextView(mContext);
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
                                gd.setColor(mContext.getResources().getColor(R.color.white)); // Changes this drawabale to use a single color instead of a gradient
                                textView.setTextColor(mContext.getResources().getColor(R.color.purple_500));
                                textView.setTypeface(textView.getTypeface(), Typeface.BOLD);
                                textView.setBackground(gd);

                                // set the value of the textView
                                String value = entry.getValue();
                                Log.i("does not match user skills", entry.getValue().toString());
                                textView.setText(value);

                                // add the textView to the linear layout of the request item
                                holder.itemLayout.addView(textView, layoutParams);
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

        // new data is bound to view holder
        // set the description in the object to the textview in the request
        holder.itemDescription.setText(requestItem.getTitle());

        // if an image is not uploaded, make the imageview disappear
        if (requestItem.getImage_url().equals("")){
            holder.itemImage.setVisibility(View.GONE);
        } else {
            // if an image is uploaded, load image into imageview in request using the url
            GlideApp.with(mContext.getApplicationContext()).load(requestItem.getImage_url()).into(holder.itemImage);
        }
        // set the time since creation in the textview in the post
        holder.itemTime.setText(DateUtils.getRelativeTimeSpanString(requestItem.getCreation_time_ms()));

        // set the text of the textView for the status of the request and change visual attributes
        GradientDrawable gd1 = new GradientDrawable();
        holder.itemStatus.setText(requestItem.getStatus());
        holder.itemStatus.setTypeface(holder.itemStatus.getTypeface(), Typeface.BOLD);
        holder.itemStatus.setTextColor(mContext.getResources().getColor(R.color.white));
        holder.itemStatus.setPadding(3, 2, 5, 3);

        // change color of the background based on request status type
        if (requestItem.getStatus().equalsIgnoreCase("active")){
            gd1.setColor(Color.GREEN);
            gd1.setCornerRadius(5);
            gd1.setStroke(1, Color.GREEN);
            holder.itemStatus.setBackground(gd1);
        } else if (requestItem.getStatus().equalsIgnoreCase("pending")){
            gd1.setColor(Color.CYAN);
            gd1.setCornerRadius(5);
            gd1.setStroke(1, Color.CYAN);
            holder.itemStatus.setBackground(gd1);
        } else if (requestItem.getStatus().equalsIgnoreCase("complete")){
            gd1.setColor(Color.RED);
            gd1.setCornerRadius(5);
            gd1.setStroke(1, Color.RED);
            holder.itemStatus.setBackground(gd1);
            holder.itemStatusSpinner.setVisibility(View.GONE);
            holder.itemStatusSpinner.setEnabled(false);
        }

        // if the request is one of "your requests" show the complete checkbox
        if (requests.get(position).getShowRequestee() == true){
            holder.itemStatusSpinner.setVisibility(View.VISIBLE);
            holder.itemMenu.setVisibility(View.VISIBLE);

            // if the item has already been marked as complete get rid of the checkbox and keep match parent parameters
            if (holder.itemStatus.getText().toString().equals("complete")) {
                holder.itemStatusSpinner.setVisibility(View.GONE);
            } else {
                // if the request is your request and hasn't been marked as complete yet, then change the status width parameters to accommodate
                ViewGroup.LayoutParams params = holder.itemStatus.getLayoutParams();
                params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                holder.itemStatus.setLayoutParams(params);
            }
        }

        // get request id of cardview request
        String requestId = requests.get(position).getRequestId();
        DocumentReference docRef = firestoreDb.collection("requests").document(requestId);

        // set the text of the textView for the status of the request and change visual attributes
        GradientDrawable gd2 = new GradientDrawable();


        // add a listener to the spinner
        holder.itemStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                // get the chosen item value
                String item = holder.itemStatusSpinner.getSelectedItem().toString();

                if(requestItem.getLastSpinnerPosition() == i){
                    //
                }
                // if status chosen was complete
                else if (item.equalsIgnoreCase("complete")) {
                    String uid = currentUser.getUid();

                    // sets specific fields, titles, and hints for request completion dialog
                    final Dialog dialog = new Dialog(mContext);
                    dialog.setTitle("Request Completion Information");
                    dialog.setContentView(R.layout.dialog_add);
                    dialog.show();

                    TextView title = dialog.findViewById(R.id.textView2);
                    final EditText editId = dialog.findViewById(R.id.editId);
                    Button btnOk = dialog.findViewById(R.id.btnOk);
                    final EditText editHoursWorked = dialog.findViewById(R.id.editHours);
                    // make hours worked edit text visible
                    editHoursWorked.setVisibility(View.VISIBLE);

                    title.setText("Request Completion Information");
                    editId.setHint("Fulfiller Username");
                    editHoursWorked.setHint("Hours Worked");

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
                                firestoreDb.collection("users").whereEqualTo("username", userName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
                                                    // adds total hours worked for request to the fulfillers user document field
                                                    //long totalHoursWorked = (Long) documentSnapshot.get("totalHoursWorked");
                                                    totalHoursWorked = (Long) documentSnapshot.get("totalHoursWorked");
                                                    // adds and updates the total hours worked
                                                    totalHoursWorked = totalHoursWorked + Long.parseLong(editHoursWorked.getText().toString());
                                                    HashMap<String,Object> dataHoursWorked = new HashMap<>();
                                                    dataHoursWorked.put("totalHoursWorked", totalHoursWorked);
                                                    documentSnapshot.getReference().update(dataHoursWorked);

                                                    // get state and county details for fulfiller
                                                    String state = documentSnapshot.getString("state");
                                                    String community = documentSnapshot.getString("community");
                                                    //DocumentReference docRefCommunity = firestoreDb.collection("community").document(state).collection("county").document(community);
                                                    DocumentReference docRefCommunity = firestoreDb.collection("community").document(community + state);

                                                    // updating county total hours completed
                                                    /*docRefCommunity.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                                if (documentSnapshot.exists()) {
                                                                    // add hours worked to fulfiller's community's hours
                                                                    long communityHours = documentSnapshot.getLong("totalCommunityHours");
                                                                    communityHours += totalHoursWorked;
                                                                    docRefCommunity.update("totalCommunityHours", communityHours);
                                                                    totalHoursWorked = 0;
                                                                }
                                                            }
                                                        }
                                                    });*/


                                                    docRefCommunity.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                                if (documentSnapshot.exists()) {
                                                                    // add hours worked to fulfiller's community's hours
                                                                    long communityHours = documentSnapshot.getLong("totalCommunityHours");
                                                                    communityHours += totalHoursWorked;
                                                                    docRefCommunity.update("totalCommunityHours", communityHours);
                                                                    totalHoursWorked = 0;
                                                                }
                                                            }
                                                        }
                                                    });


                                                    dialog.cancel();
                                                    // set the status of the request item to complete
                                                    requestItem.setStatus("complete");
                                                    holder.itemStatus.setText(requestItem.getStatus());
                                                    gd2.setColor(Color.RED);
                                                    gd2.setCornerRadius(5);
                                                    gd2.setStroke(1, Color.RED);
                                                    holder.itemStatus.setBackground(gd2);
                                                    //record the last spinner position
                                                    requestItem.setLastSpinnerPosition(2);
                                                    // make spinner disappear
                                                    holder.itemStatusSpinner.setVisibility(View.GONE);
                                                    holder.itemStatusSpinner.setEnabled(false);

                                                    // adjusts layout for status tag when marked complete
                                                    //ViewGroup.LayoutParams params = holder.itemStatus.getLayoutParams();
                                                    //params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                                                    //holder.itemStatus.setLayoutParams(params);

                                                    // get the request doc where the request id is equal to the request id of the item in the recyclerview
                                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                DocumentSnapshot document = task.getResult();
                                                                if (document.exists()) {
                                                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                                                    // set the status of the request to complete
                                                                    HashMap<String,Object> dataRequestStatus = new HashMap<>();
                                                                    dataRequestStatus.put("status", "complete");
                                                                    document.getReference().update(dataRequestStatus);

                                                                    // get the title of the request
                                                                    HashMap<String,String> dataRequest = new HashMap<>();
                                                                    String requestTitle = document.get("title", String.class);
                                                                    dataRequest.put("fulfilledRequestTitle", requestTitle);

                                                                    // add the title of the fulfilled request to the requestee's "allFulfilledRequests" collection
                                                                    firestoreDb.collection("users").document(uidFriend).collection("allFulfilledRequests").document(requestId).set(dataRequest).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            Log.i("success","added request to user");
                                                                        }
                                                                    });

                                                                } else {
                                                                    Log.d(TAG, "No such document");
                                                                }
                                                            } else {
                                                                Log.d(TAG, "get failed with ", task.getException());
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    });
                    // if status chosen was pending
                } else if (item.equalsIgnoreCase("pending")) {
                    requestItem.setStatus("pending");
                    holder.itemStatus.setText(requestItem.getStatus());
                    gd2.setColor(Color.CYAN);
                    gd2.setCornerRadius(5);
                    gd2.setStroke(1, Color.CYAN);
                    holder.itemStatus.setBackground(gd2);
                    //record the last spinner position
                    requestItem.setLastSpinnerPosition(1);
                    holder.itemStatusSpinner.setSelection(1);

                    // get the request doc where the request id is equal to the request id of the item in the recyclerview
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    // set the status of the request to pending
                                    HashMap<String,Object> dataRequestStatus = new HashMap<>();
                                    dataRequestStatus.put("status", "pending");
                                    document.getReference().update(dataRequestStatus);
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });

                    // if status chosen was active
                } else if (item.equalsIgnoreCase("active")) {
                    requestItem.setStatus("active");
                    holder.itemStatus.setText(requestItem.getStatus());
                    gd2.setColor(Color.GREEN);
                    gd2.setCornerRadius(5);
                    gd2.setStroke(1, Color.GREEN);
                    holder.itemStatus.setBackground(gd2);
                    //record the last spinner position
                    requestItem.setLastSpinnerPosition(0);
                    holder.itemStatusSpinner.setSelection(0);

                    // get the request doc where the request id is equal to the request id of the item in the recyclerview
                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                    // set the status of the request to active
                                    HashMap<String,Object> dataRequestStatus = new HashMap<>();
                                    dataRequestStatus.put("status", "active");
                                    document.getReference().update(dataRequestStatus);
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        holder.itemMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(mContext, holder.itemMenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("Delete Request")) {
                            firestoreDb.collection("requests").document(requests.get(position).getRequestId())
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                            notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.w(TAG, "Error deleting document", e);
                                        }
                                    });
                        } else if (item.getTitle().equals("Edit Request")){
                            Intent intent = new Intent(mContext,CreateRequest.class);
                            intent.putExtra("REQUEST_TTILE", requests.get(position).getTitle());
                            intent.putExtra("REQUEST_BODY", requests.get(position).getBody());
                            intent.putExtra("REQUEST_CREATIONTIMEMS", requests.get(position).getCreation_time_ms());
                            intent.putExtra("REQUEST_IMAGEURL", requests.get(position).getImage_url());
                            intent.putExtra("REQUEST_LASTSPINNERLOCATION", requests.get(position).getLastSpinnerPosition());
                            intent.putExtra("REQUEST_STATUS", requests.get(position).getStatus());
                            intent.putExtra("REQUEST_FAVORITE", requests.get(position).getFavorite());
                            intent.putExtra("REQUEST_REQUESTCATEGORIES", requests.get(position).getRequestCategories());
                            intent.putExtra("REQUEST_REQUESTERID", requests.get(position).getRequesterId());
                            intent.putExtra("REQUEST_SHOWREQUESTEE", requests.get(position).getShowRequestee());
                            intent.putExtra("REQUEST_REQUESTID", requests.get(position).getRequestId());
                            intent.putExtra("REQUEST_STARTDATE", requests.get(position).getStartDate());
                            intent.putExtra("REQUEST_ENDDATE", requests.get(position).getEndDate());
                            intent.putExtra("REQUEST_ONLINE", requests.get(position).getOnline());
                            //intent.putExtras("REQUEST", (Parcelable) requests.get(position));
                            ((Activity) mContext).startActivityForResult(intent,1);
                        }
                        return true;
                    }
                });

                popup.show(); //showing popup menu
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,ItemRequestActivity.class);
                intent.putExtra("REQUEST_TTILE", requests.get(position).getTitle());
                intent.putExtra("REQUEST_BODY", requests.get(position).getBody());
                intent.putExtra("REQUEST_CREATIONTIMEMS", requests.get(position).getCreation_time_ms());
                intent.putExtra("REQUEST_IMAGEURL", requests.get(position).getImage_url());
                intent.putExtra("REQUEST_LASTSPINNERLOCATION", requests.get(position).getLastSpinnerPosition());
                intent.putExtra("REQUEST_STATUS", requests.get(position).getStatus());
                intent.putExtra("REQUEST_FAVORITE", requests.get(position).getFavorite());
                intent.putExtra("REQUEST_REQUESTCATEGORIES", requests.get(position).getRequestCategories());
                intent.putExtra("REQUEST_REQUESTERID", requests.get(position).getRequesterId());
                intent.putExtra("REQUEST_SHOWREQUESTEE", requests.get(position).getShowRequestee());
                intent.putExtra("REQUEST_REQUESTID", requests.get(position).getRequestId());
                intent.putExtra("REQUEST_STARTDATE", requests.get(position).getStartDate());
                intent.putExtra("REQUEST_ENDDATE", requests.get(position).getEndDate());
                intent.putExtra("REQUEST_ONLINE", requests.get(position).getOnline());
                ((Activity) mContext).startActivityForResult(intent,1);
            }
        });
    }
    
        private void  notifyCommunityTaskComplete(String member ,
                                              String community){
        Log.i("notifyCommunityTaskComplete" , member);
        JSONObject jPayload = new JSONObject();
        JSONObject jNotification = new JSONObject();
        JSONObject jdata = new JSONObject();
        try {
            jNotification.put("title",  community+ " member completed a task!");
            jNotification.put("body", "Message body hi members");
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
            jPayload.put("to", member); // CLIENT_REGISTRATION_TOKEN);
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
                    Log.e(TAG, "run: " + resp);
                  //  Toast.makeText(getApplicationContext(),resp,Toast.LENGTH_LONG).show();
                }
            });
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
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

    // returns the size of the list
    @Override
    public int getItemCount() {
        return requests.size();
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
                        if (favoritedUsers.contains(currentUser.getUid())){
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
}
