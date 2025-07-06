// PostAdapter.java
package com.example.proyectofinal;

import android.content.Context;
import android.content.Intent;
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
    private final boolean isEditable;

    public PostAdapter(Context ctx, List<Post> list, boolean editable) {
        context = ctx;
        posts = list;
        isEditable = editable;
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
        h.tvPrice.setText("S/." + String.format("%.2f", post.getPrice()));
        h.ratingBar.setRating(post.getRating().floatValue());

        // Primera imagen
        List<ParseFile> imgs = post.getImages();
        if (imgs!=null && !imgs.isEmpty()) {
            Glide.with(context).load(imgs.get(0).getUrl()).into(h.ivThumb);
        }


        h.itemView.setOnClickListener(v -> {
            if (isEditable) {
                // En AccountActivity: edición
                Intent i = new Intent(context, CreatePostActivity.class);
                i.putExtra("editPostId", post.getObjectId());
                context.startActivity(i);
            } else {
                // En MainActivity: detalle
                Intent i = new Intent(context, PostDetailActivity.class);
                i.putExtra("postId", post.getObjectId());
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() { return posts.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvTitle, tvPrice;  // Added tvPrice here
        RatingBar ratingBar;

        ViewHolder(View itemView) {
            super(itemView);
            ivThumb   = itemView.findViewById(R.id.ivThumbnail);
            tvTitle   = itemView.findViewById(R.id.tvPostTitle);
            tvPrice   = itemView.findViewById(R.id.tvPostPrice);  // Make sure this ID exists in item_post.xml
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
