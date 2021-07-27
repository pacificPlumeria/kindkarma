package edu.neu.madcourse.kindkarma;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Responsible for managing the rows and keeps a track of everything inside the row (image, data, etc)
public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView itemImage;
    public ImageView itemProfileImage;
    public TextView itemUserName;
    public TextView itemDescription;
    public TextView itemTime;
    public View itemView;
    public TextView itemLikes;
    public ImageView itemLike;
    public ImageButton itemMenu;

    // Constructor
    // View Holder describes an item view and metadata about its place within the RecyclerView
    public ViewHolder(@NonNull View itemView, final ListItemClickListener listener) {
        super(itemView);
        this.itemView = itemView;
        itemImage = itemView.findViewById(R.id.ivPost);
        itemUserName = itemView.findViewById(R.id.tvUsername);
        itemDescription = itemView.findViewById(R.id.tvDescription);
        itemTime = itemView.findViewById(R.id.tvRelativeTime);
        itemProfileImage = itemView.findViewById(R.id.ivProfileImage);
        itemLike = itemView.findViewById(R.id.like);
        itemLikes = itemView.findViewById(R.id.likes);
        itemMenu = itemView.findViewById(R.id.overflowButton);
        itemLike.setOnClickListener(this);

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
