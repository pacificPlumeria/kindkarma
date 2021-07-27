package edu.neu.madcourse.kindkarma;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Responsible for managing the rows and keeps a track of everything inside the row (image, data, etc)
public class CommunitiesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public View itemView;
    public TextView communityName;
    public TextView communityHoursWorked;
    //public ImageView communityProfileImage;
    public TextView communityStateName;
    public TextView communityNumberMembers;
    public TextView communityRank;
    public ImageButton communityBackColor;
    public TextView communityAbbreviation;


    // Constructor
    // View Holder describes an item view and metadata about its place within the RecyclerView
    public CommunitiesViewHolder(@NonNull View itemView, final ListItemClickListener listener) {
        super(itemView);
        this.itemView = itemView;
        this.communityName = itemView.findViewById(R.id.tvCommunityName);
        this.communityHoursWorked = itemView.findViewById(R.id.tvTotalCommunityHours);
        //this.communityProfileImage = itemView.findViewById(R.id.imgCommunity);
        this.communityStateName = itemView.findViewById(R.id.tvStateName);
        this.communityNumberMembers = itemView.findViewById(R.id.tvNumberMembers);
        this.communityRank = itemView.findViewById(R.id.tvRank);
        this.communityAbbreviation = itemView.findViewById(R.id.comm_abbrev);
        this.communityBackColor = itemView.findViewById(R.id.community_icon_back);

        // setting on click listeners for each row item
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null){
                    // gets position of tapped item in list
                    int itemPosition = getLayoutPosition();

                    // if recyclerview is changed then it is recalculated
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        listener.onListItemClick(itemPosition);
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(), "Clicked button at position: " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
    }
}
