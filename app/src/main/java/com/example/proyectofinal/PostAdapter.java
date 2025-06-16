package com.example.proyectofinal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private static final int PAGE_SIZE = 6;

    public PostAdapter(List<Post> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.titleTextView.setText(post.getTitle());
        holder.priceTextView.setText(String.format("$%.2f", post.getPrice()));
        holder.ratingBar.setRating((float) post.getRating());

        // Cargar imagen con Glide
        if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(post.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.postImageView);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void addPosts(List<Post> newPosts) {
        int startPosition = postList.size();
        postList.addAll(newPosts);
        notifyItemRangeInserted(startPosition, newPosts.size());
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImageView;
        TextView titleTextView;
        TextView priceTextView;
        RatingBar ratingBar;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImageView = itemView.findViewById(R.id.postImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}