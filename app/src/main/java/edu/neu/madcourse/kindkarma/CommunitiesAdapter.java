package edu.neu.madcourse.kindkarma;

import android.content.Context;
import android.graphics.Color;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;

import edu.neu.madcourse.kindkarma.models.Comment;
import edu.neu.madcourse.kindkarma.models.Community;
import edu.neu.madcourse.kindkarma.models.User;

public class CommunitiesAdapter extends RecyclerView.Adapter<CommunitiesViewHolder>{
    private final ArrayList<Community> communities;
    private ListItemClickListener listener;
    public Context mContext;
    public User user;
    private final String TAG = "CommunitiesAdapter";
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String currentUser = mAuth.getCurrentUser().getUid();

    // Constructor
    public CommunitiesAdapter(Context context, ArrayList<Community> communities) {
        this.mContext = context;
        this.communities = communities;
    }

    public void setOnListItemClickListener(ListItemClickListener listener) {
        this.listener = listener;
    }

    //Create individual rows that are necessary for displaying the items in the RecyclerView
    public CommunitiesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_community, parent, false);
        CommunitiesViewHolder viewHolder = new CommunitiesViewHolder(view, listener);
        return viewHolder;
    }

    // When rows that have old data scroll off screen, they will be replaced with new data
    @Override
    public void onBindViewHolder(@NonNull CommunitiesViewHolder holder, int position) {
        // set community values to be displayed per recyclerview item
        holder.communityName.setText(communities.get(position).getCounty());
        holder.communityHoursWorked.setText(String.valueOf(communities.get(position).getTotalCommunityHours()) + " hours");
        holder.communityNumberMembers.setText(String.valueOf(communities.get(position).getTotalUsers()) + " members");
        holder.communityStateName.setText(communities.get(position).getState());
        holder.communityRank.setText(String.valueOf(position + 1));
        holder.communityAbbreviation.setText(communities.get(position).getAbbreviation());
        String x = communities.get(position).getCommunityColor();
        int col2 = Color.parseColor(x);
        holder.communityBackColor.setBackgroundColor(col2);
    }

    // returns the size of the list
    @Override
    public int getItemCount() {
        return communities.size();
    }

}
