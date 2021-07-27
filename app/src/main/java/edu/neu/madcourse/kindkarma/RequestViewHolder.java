package edu.neu.madcourse.kindkarma;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// Responsible for managing the rows and keeps a track of everything inside the row (image, data, etc)
public class RequestViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public ImageView itemImage;
    public ImageView itemProfileImage;
    public TextView itemUserName;
    public TextView itemDescription;
    public TextView itemTime;
    public View itemView;
    public LinearLayout itemLayout;
    public TextView itemStatus;
    public Spinner itemStatusSpinner;
    public ImageView itemFavorite;
    public ImageButton itemMenu;
    public TextView itemStartDate;
    public TextView itemEndDate;
    public TextView itemDateHyphen;

    // Constructor
    // View Holder describes an item view and metadata about its place within the RecyclerView
    public RequestViewHolder(@NonNull View itemView, final ListItemClickListener listener) {
        super(itemView);
        this.itemView = itemView;
        itemImage = itemView.findViewById(R.id.ivRequest);
        itemUserName = itemView.findViewById(R.id.tvUsernameRequest);
        itemDescription = itemView.findViewById(R.id.tvDescriptionRequest);
        itemTime = itemView.findViewById(R.id.tvRelativeTimeRequest);
        itemProfileImage = itemView.findViewById(R.id.ivProfileImageRequest);
        itemLayout = itemView.findViewById(R.id.linear);
        itemStatus = itemView.findViewById(R.id.tvStatus);
        itemStatusSpinner = itemView.findViewById(R.id.spinnerStatus);
        itemFavorite = itemView.findViewById(R.id.favorite);
        itemMenu = itemView.findViewById(R.id.deleteButton);
        itemFavorite.setOnClickListener(this);
        itemStartDate = itemView.findViewById(R.id.tvStartDate);
        itemEndDate = itemView.findViewById(R.id.tvEndDate);
        itemDateHyphen = itemView.findViewById(R.id.tvDateHyphen);

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
