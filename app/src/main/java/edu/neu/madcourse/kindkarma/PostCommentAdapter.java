package edu.neu.madcourse.kindkarma;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import edu.neu.madcourse.kindkarma.GlideApp;
import edu.neu.madcourse.kindkarma.ItemPostActivity;
import edu.neu.madcourse.kindkarma.ListItemClickListener;
import edu.neu.madcourse.kindkarma.MD5Util;
import edu.neu.madcourse.kindkarma.PostCommentViewHolder;
import edu.neu.madcourse.kindkarma.R;
import edu.neu.madcourse.kindkarma.RequestViewHolder;
import edu.neu.madcourse.kindkarma.ViewHolder;
import edu.neu.madcourse.kindkarma.models.Chat;
import edu.neu.madcourse.kindkarma.models.Comment;
import edu.neu.madcourse.kindkarma.models.Post;
import edu.neu.madcourse.kindkarma.models.Request;
import edu.neu.madcourse.kindkarma.models.User;

public class PostCommentAdapter extends RecyclerView.Adapter<PostCommentViewHolder>{
    private final ArrayList<Comment> comments;
    private ListItemClickListener listener;
    public Context mContext;
    public User user;
    FirebaseFirestore firestoreDb;
    DocumentReference usersDocReference;
    private final String TAG = "RecyclerAdapter";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUser = mAuth.getCurrentUser().getUid();
    Comment commentItem;
    String commentId;

    // Constructor
    public PostCommentAdapter(Context context, ArrayList<Comment> comments) {
        this.mContext = context;
        this.comments = comments;
    }

    public void setOnListItemClickListener(ListItemClickListener listener) {
        this.listener = listener;
    }

    //Create individual rows that are necessary for displaying the items in the RecyclerView
    public PostCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_comment, parent, false);
        PostCommentViewHolder viewHolder = new PostCommentViewHolder(view, listener);
        return viewHolder;
    }

    // When rows that have old data scroll off screen, they will be replaced with new data
    @Override
    public void onBindViewHolder(@NonNull PostCommentViewHolder holder, int position) {
        // Get instance of firebasefirestore - points to root of our database
        firestoreDb = FirebaseFirestore.getInstance();

        // Get reference to the user doc in the database that matches the id of the person who posted comment
        usersDocReference = firestoreDb.collection("users").document(comments.get(position).getUid());

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
                    holder.postTxtUserName.setText(user.getUsername());
                    // call getProfileImageUrl and load image into profile pic imageview in post using returned url
                    //GlideApp.with(mContext).load(getProfileImageUrl(user.getUsername())).into(holder.postProfileImage);
                    GlideApp.with(mContext).load(user.getProfileImage()).into(holder.postProfileImage);
                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });

        // set comment message and time sent
        holder.postTxtComment.setText(comments.get(position).getComment());
        holder.postRelativeTimeSpan.setText(DateUtils.getRelativeTimeSpanString(comments.get(position).getCreation_time_ms()));
    }

    // returns the size of the list
    @Override
    public int getItemCount() {
        return comments.size();
    }

}
