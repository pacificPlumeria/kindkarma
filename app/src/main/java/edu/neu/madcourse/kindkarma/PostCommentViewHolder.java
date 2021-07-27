package edu.neu.madcourse.kindkarma;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Responsible for managing the rows and keeps a track of everything inside the row (image, data, etc)
public class PostCommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public View itemView;
    public TextView postTxtUserName;
    public TextView postTxtComment;
    public ImageView postProfileImage;
    public TextView postRelativeTimeSpan;


    // Constructor
    // View Holder describes an item view and metadata about its place within the RecyclerView
    public PostCommentViewHolder(@NonNull View itemView, final ListItemClickListener listener) {
        super(itemView);
        this.itemView = itemView;
        this.postTxtComment = itemView.findViewById(R.id.txtComment);
        this.postTxtUserName = itemView.findViewById(R.id.txtUserName);
        this.postProfileImage = itemView.findViewById(R.id.imgPostProfile);
        this.postRelativeTimeSpan = itemView.findViewById(R.id.txtRelativeTimeSpan);

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