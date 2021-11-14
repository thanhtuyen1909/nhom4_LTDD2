package vn.edu.tdc.zuke_customer.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Category;

public class Category1Adapter extends RecyclerView.Adapter<Category1Adapter.CategoryViewHolder> {
    Context mContext;
    ArrayList<Category> arrCategory;
    ItemClickCategory itemClickCategory;

    public Category1Adapter(Context mContext, ArrayList<Category> arrCategory) {
        this.mContext = mContext;
        this.arrCategory = arrCategory;
    }

    public void setItemClickListener(ItemClickCategory itemClickListener) {
        this.itemClickCategory = itemClickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_product, parent, false);
        return new Category1Adapter.CategoryViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        //Load data
        Category model = arrCategory.get(position);
        holder.tv_name.setText(model.getName());
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("images/categories/" + model.getImage());
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).resize(holder.img.getWidth(), holder.img.getHeight()).into(holder.img));

        holder.cardView.setOnClickListener(v -> {
            if(itemClickCategory != null) {
                itemClickCategory.changeManu(model.getKey());
            } else return;
        });
    }

    @Override
    public int getItemCount() {
        return arrCategory.size();
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        ImageView img;
        RelativeLayout cardCategory;
        CardView cardView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.item_title);
            img = itemView.findViewById(R.id.item_image);
            cardCategory = itemView.findViewById(R.id.item);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    public interface ItemClickCategory {
        void changeManu(String categoryID);
    }
}
