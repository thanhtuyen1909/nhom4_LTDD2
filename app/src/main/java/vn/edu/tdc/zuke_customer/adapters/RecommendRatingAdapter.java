package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;

public class RecommendRatingAdapter extends RecyclerView.Adapter<RecommendRatingAdapter.ViewHolder> {
    Context context;
    ArrayList<String> items;
    RecommendRatingAdapter.ItemClick itemClickListener;

    public RecommendRatingAdapter(Context context, ArrayList<String> items) {
        this.context = context;
        this.items = items;
    }

    public void setItemClickListener(RecommendRatingAdapter.ItemClick itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecommendRatingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RecommendRatingAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendRatingAdapter.ViewHolder holder, int position) {
        String item = items.get(position);
        holder.txtComment.setText(item);
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.getComment(item);
            }
            else return;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtComment;

        public ViewHolder(View view) {
            super(view);
            txtComment = view.findViewById(R.id.comment);
        }
    }

    public interface ItemClick {
        void getComment(String s);
    }
}
