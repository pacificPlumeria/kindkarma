package edu.neu.madcourse.kindkarma;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.Request;
import edu.neu.madcourse.kindkarma.models.User;

public class RecyclerAdapter extends RecyclerView.Adapter<ViewHolder>{
    private final ArrayList<Post> posts;
    private ListItemClickListener listener;
    public Context mContext;
    public User user;
    FirebaseFirestore firestoreDb;
    DocumentReference usersDocReference;
    private final String TAG = "RecyclerAdapter";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUser = mAuth.getCurrentUser().getUid();
    Post postItem;
    String postId;

    // Constructor
    public RecyclerAdapter(Context context, ArrayList<Post> posts) {
        this.mContext = context;
        this.posts = posts;
    }

    public void setOnListItemClickListener(ListItemClickListener listener) {
        this.listener = listener;
    }

    //Create individual rows that are necessary for displaying the items in the RecyclerView
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, listener);
        return viewHolder;
    }

    // When rows that have old data scroll off screen, they will be replaced with new data
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();

        //get the item in the list
        postItem = posts.get(position);
        postId = postItem.getPostId();

        // Get reference to the user doc in the database that matches the id of the person who posted
        usersDocReference = firestoreDb.collection("users").document(postItem.getPosterId());

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
                    holder.itemUserName.setText(user.getUsername());
                    // call getProfileImageUrl and load image into profile pic imageview in post using returned url
                    GlideApp.with(mContext).load(user.getProfileImage()).into(holder.itemProfileImage);
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });

        // new data is bound to view holder
        // set the description in the object to the textview in the post
        holder.itemDescription.setText(postItem.getDescription());
        // if an image is not uploaded, make the imageview disappear
        if (postItem.getImage_url() == null){
            holder.itemImage.setVisibility(View.GONE);
        } else {
            // if an image is uploaded, load image into imageview in post using the url
            GlideApp.with(mContext).load(postItem.getImage_url()).into(holder.itemImage);
        }
        // set the time since creation in the textview in the post
        holder.itemTime.setText(DateUtils.getRelativeTimeSpanString(postItem.getCreation_time_ms()));

        // call addUser method to add an unlike for the current user if not in collection already
        addUser(posts.get(position).getPostId(), holder.itemLike);
        // call isLikes method to display the correct imageView depending on if user previously liked on unliked the post
        isLikes(posts.get(position).getPostId(), holder.itemLike);
        // call countLikes method to display the updated number of likes that the post has received
        countLikes(holder.itemLikes,posts.get(position).getPostId(),holder.itemLike);

        // set an onclick listener on the like imageView
        holder.itemLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DocumentReference docRef = firestoreDb.collection("likes").document(posts.get(position).getPostId()).collection("userLiked").document(currentUser);

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
                                            holder.itemLike.setImageResource(R.drawable.ic_liked);
                                            holder.itemLike.setTag("liked");
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
                                            holder.itemLike.setImageResource(R.drawable.ic_like);
                                            holder.itemLike.setTag("like");
                                        }
                                    });
                                }
                                // call the countLikes method which displays the count of likes to the user
                                countLikes(holder.itemLikes,posts.get(position).getPostId(),holder.itemLike);
                            }
                        }
                    }
                });
            }
        });

        if (currentUser.equalsIgnoreCase(posts.get(position).getPosterId())){
            holder.itemMenu.setVisibility(View.VISIBLE);
        }

        holder.itemMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(mContext, holder.itemMenu);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.popup_menu_post, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().equals("Delete Post")) {
                            firestoreDb.collection("posts").document(posts.get(position).getPostId())
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
                        } else if (item.getTitle().equals("Edit Post")){
                            Intent intent = new Intent(mContext,CreateActivity.class);
                            intent.putExtra("POST_DESCRIPTION",posts.get(position).getDescription());
                            intent.putExtra("POST_CREATIONTIMEMS",posts.get(position).getCreation_time_ms());
                            intent.putExtra("POST_IMAGEURL",posts.get(position).getImage_url());
                            intent.putExtra("POST_POSTID",posts.get(position).getPostId());
                            intent.putExtra("POST_POSTERID",posts.get(position).getPosterId());
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
                Intent intent = new Intent(mContext,ItemPostActivity.class);
                intent.putExtra("POST_ID", posts.get(position).getPostId());
                intent.putExtra("POST_DESCRIPTION", posts.get(position).getDescription());
                intent.putExtra("POST_IMAGEURL", posts.get(position).getImage_url());
                intent.putExtra("POST_POSTERID", posts.get(position).getPosterId());
                intent.putExtra("POST_CREATIONTIMEMS", posts.get(position).getCreation_time_ms());
                ((Activity) mContext).startActivityForResult(intent,1);
            }
        });
    }

    // returns the size of the list
    @Override
    public int getItemCount() {
        return posts.size();
    }

    // displays the correct imageView depending on if user previously liked or unliked the post
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

    // addUser method adds an unlike for the current user if not in collection already
    private void addUser(String postId, final ImageView imageView){

        DocumentReference docRef = firestoreDb.collection("likes").document(postId).collection("userLiked").document(currentUser);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        // do nothing
                    } else {
                        // add a false field for that user
                        HashMap<String, Object> dataUnlikedUser = new HashMap<>();
                        dataUnlikedUser.put("value",false);
                        docRef.set(dataUnlikedUser).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // set the like imageView (unfilled)
                                imageView.setImageResource(R.drawable.ic_like);
                                imageView.setTag("like");
                            }
                        });
                    }
                }
            }
        });
    }

    // displays the updated number of likes that the post has received
    private void countLikes(TextView likes, String postId, final ImageView imageView){
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
}
