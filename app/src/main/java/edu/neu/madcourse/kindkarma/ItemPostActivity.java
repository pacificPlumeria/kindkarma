package edu.neu.madcourse.kindkarma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.neu.madcourse.kindkarma.models.Chat;
import edu.neu.madcourse.kindkarma.models.Comment;
import edu.neu.madcourse.kindkarma.models.Friend;
import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.Request;
import edu.neu.madcourse.kindkarma.models.User;
import uk.co.senab.photoview.PhotoViewAttacher;

public class ItemPostActivity extends AppCompatActivity {
    String postId;
    String postDescription;
    String postImageUrl;
    String postPosterId;
    Long postCreationTimeMs;
    FirebaseFirestore firestoreDb = FirebaseFirestore.getInstance();
    DocumentReference usersDocReference;
    private final String TAG = "ItemPostActivity";
    User user;
    TextView userName;
    ImageView postProfilePic;
    TextView tvPostDescription;
    TextView postRelativeTime;
    ImageView postImage;
    PhotoViewAttacher mAttacher;
    ImageView postItemLike;
    TextView postLikesCount;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUser = mAuth.getCurrentUser().getUid();
    private EditText editComment;
    private ImageButton imbSend;
    public ArrayList<Comment> comments;
    private RecyclerView recyclerView;
    private PostCommentAdapter recyclerAdapter;
    private RecyclerView.LayoutManager recyclerLayoutManager;
    CollectionReference commentsReference;
    ImageButton overflowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_post);

        Bundle bundle = getIntent().getExtras();

        postId = bundle.getString("POST_ID");

        postDescription = bundle.getString("POST_DESCRIPTION");

        postImageUrl = bundle.getString("POST_IMAGEURL");

        postPosterId = bundle.getString("POST_POSTERID");

        postCreationTimeMs = bundle.getLong("POST_CREATIONTIMEMS");

        userName = (TextView)findViewById(R.id.postUsername);
        postProfilePic = (ImageView)findViewById(R.id.postProfileImage);
        tvPostDescription = (TextView)findViewById(R.id.postDescription);
        postRelativeTime = (TextView)findViewById(R.id.postRelativeTime);
        postImage = (ImageView)findViewById(R.id.postImage);
        postItemLike = (ImageView)findViewById(R.id.postLike);
        postLikesCount = (TextView)findViewById(R.id.postLikesCount);
        overflowButton = (ImageButton)findViewById(R.id.overflowButton_itempost);

        editComment = findViewById(R.id.editComment);
        imbSend = findViewById(R.id.commentSend);

        comments = new ArrayList<Comment>();
        recyclerLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.rvComments);
        recyclerAdapter = new PostCommentAdapter(this, comments);

        // Bind the adapter and layout manager to the RV
        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.setLayoutManager(recyclerLayoutManager);

        // add lines between comment items
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        boolean online = isOnline();
        if (!online){
            askNetwork();
        }

        // want to get to comments collection
        commentsReference = firestoreDb.collection("posts").document(postId).collection("comments");

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

        // sets username in post
        setUserName(postPosterId);

        // new data is bound to view holder
        // set the description in the object to the textview in the post
        tvPostDescription.setText(postDescription);
        // if an image is not uploaded, make the imageview disappear
        if (postImageUrl == null){
            postImage.setVisibility(View.GONE);
        } else {
            // if an image is uploaded, load image into imageview in post using the url
            GlideApp.with(getApplicationContext()).load(postImageUrl).into(postImage);
        }
        // set the time since creation in the textview in the post
        postRelativeTime.setText(DateUtils.getRelativeTimeSpanString(postCreationTimeMs));

        if (currentUser.equalsIgnoreCase(postPosterId)){
            overflowButton.setVisibility(View.VISIBLE);
        }

        countLikes(postLikesCount, postId);
        isLikes(postId, postItemLike);

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
                    firestoreDb.collection("posts").document(postId).collection("comments").document().set(dataMessage).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // if successful, clear the editText
                            editComment.setText("");
                        }
                    });
                }
            }
        });

        // set an onclick listener on the like imageView
        postItemLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference docRef = firestoreDb.collection("likes").document(postId).collection("userLiked").document(currentUser);

                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                // get the value of the user field in the doc which represents if the user has previously liked or not liked the post
                                Boolean value = documentSnapshot.getBoolean("value");
                                // if the user has not liked the post and they have now clicked the like button
                                if (value == false){
                                    // change the field to true for that user
                                    HashMap<String, Object> dataLikedUser = new HashMap<>();
                                    dataLikedUser.put("value",true);
                                    docRef.set(dataLikedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // set the image to the liked image (filled)
                                            postItemLike.setImageResource(R.drawable.ic_liked);
                                            postItemLike.setTag("liked");
                                        }
                                    });
                                    // if the user has liked the post and they have now clicked the like button
                                } else {
                                    // change the field to false for that user
                                    HashMap<String, Object> dataUnlikedUser = new HashMap<>();
                                    dataUnlikedUser.put("value",false);
                                    docRef.set(dataUnlikedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // set the image to the unliked image (not filled)
                                            postItemLike.setImageResource(R.drawable.ic_like);
                                            postItemLike.setTag("like");
                                        }
                                    });
                                }
                                // call the countLikes method which displays the count of likes to the user
                                countLikes(postLikesCount,postId);
                            }
                        }
                    }
                });
            }
        });

        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(ItemPostActivity.this, overflowButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu_post, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("Delete Post")) {
                            firestoreDb.collection("posts").document(postId)
                                    .delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                            //recyclerAdapter.notifyDataSetChanged();
                                            Intent intent = new Intent(ItemPostActivity.this,FeedActivity.class);
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
                        } else if (item.getTitle().equals("Edit Post")){
                            Intent intent = new Intent(ItemPostActivity.this,CreateActivity.class);
                            intent.putExtra("POST_DESCRIPTION",postDescription);
                            intent.putExtra("POST_CREATIONTIMEMS",postCreationTimeMs);
                            intent.putExtra("POST_IMAGEURL",postImageUrl);
                            intent.putExtra("POST_POSTID",postId);
                            Log.i("post id", postId);
                            intent.putExtra("POST_POSTERID",postPosterId);
                            Log.i("post poster id", postPosterId);
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
                    GlideApp.with(getApplicationContext()).load(user.getProfileImage()).into(postProfilePic);
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
        loadPhoto(postImage,width,height);
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

    // displays the updated number of likes that the post has received
    private void countLikes(TextView likes, String postId){
        // goes through all of the documents in the userLiked collection of a post and counts the ones that havee a value of true
        // A value of true means that the post has been liked by a user
        Query query = firestoreDb.collection("likes").document(postId).collection("userLiked").whereEqualTo("value", true);

        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                int likeCount = 0;
                for(DocumentSnapshot documentSnapshot: documentSnapshots) {
                    likeCount++;
                }
                // set the textView to the count of likes
                likes.setText(String.valueOf(likeCount) + " likes");
            }
        });
    }

    private void isLikes(String postId, final ImageView imageView){

        DocumentReference docRef = firestoreDb.collection("likes").document(postId).collection("userLiked").document(currentUser);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        // get the value of the user field in the doc which represents if the user has previously liked or not liked the post
                        Boolean value = documentSnapshot.getBoolean("value");
                        // if the user has not liked the post then the unfilled like is displayed
                        if (value == false){
                            imageView.setImageResource(R.drawable.ic_like);
                            // if the user has liked the post then the filled like is displayed
                        } else {
                            imageView.setImageResource(R.drawable.ic_liked);
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