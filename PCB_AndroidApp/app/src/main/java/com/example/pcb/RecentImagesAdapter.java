package com.example.pcb;

import android.view.*;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class RecentImagesAdapter extends RecyclerView.Adapter<RecentImagesAdapter.ViewHolder> {
    private List<String> images;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onClick(String imageUrl);
    }

    public RecentImagesAdapter(List<String> images, OnImageClickListener listener) {
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView iv = new ImageView(parent.getContext());
        iv.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new ViewHolder(iv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = images.get(position);
        Glide.with(holder.itemView.getContext()).load(url).into((ImageView) holder.itemView);
        holder.itemView.setOnClickListener(v -> listener.onClick(url));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) { super(itemView); }
    }
}