// PostAdapter.java
package com.example.proyectofinal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // o Picasso
import com.parse.ParseFile;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private final Context context;
    private final List<Post> posts;

    public PostAdapter(Context ctx, List<Post> list) {
        context = ctx;
        posts = list;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Post post = posts.get(pos);
        h.tvTitle.setText(post.getTitle());
        Number rating = post.getRating();
        h.ratingBar.setRating(rating != null ? rating.floatValue() : 0f);

        List<ParseFile> imgs = post.getImages();
        if (imgs != null && !imgs.isEmpty()) {
            String url = imgs.get(0).getUrl();
            Glide.with(context)
                    .load(url)
                    .into(h.ivThumb);
        } else {
            h.ivThumb.setImageResource(R.drawable.placeholder);
        }
    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle;
        RatingBar ratingBar;

        ViewHolder(View itemView) {
            super(itemView);
            ivThumb   = itemView.findViewById(R.id.ivThumbnail);
            tvTitle   = itemView.findViewById(R.id.tvPostTitle);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
