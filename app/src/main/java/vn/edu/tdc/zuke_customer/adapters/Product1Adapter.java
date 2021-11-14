package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.ManuProduct;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class Product1Adapter extends RecyclerView.Adapter<Product1Adapter.ViewHolder> {
    Context context;
    ArrayList<Product> listProduct;
    ItemClickProduct itemClickProduct;

    public void setItemClickListener(ItemClickProduct itemClickListener) {
        this.itemClickProduct = itemClickListener;
    }

    public Product1Adapter(Context context, ArrayList<Product> items) {
        this.context = context;
        this.listProduct = items;
    }

    @NonNull
    @Override
    public Product1Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Product1Adapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_category_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Product1Adapter.ViewHolder holder, int position) {
        Product item = listProduct.get(position);
        holder.tv_name.setText(item.getName());
        StorageReference imageRef = FirebaseStorage.getInstance().getReference("images/products/" + item.getName() + "/" + item.getImage());
        imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).fit().into(holder.img));

        holder.cardCategory.setOnClickListener(v -> {
            if (itemClickProduct != null) {
                itemClickProduct.detailProduct(item);
            } else return;
        });
    }

    @Override
    public int getItemCount() {
        return listProduct.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name;
        private ImageView img;
        private RelativeLayout cardCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.item_title);
            img = itemView.findViewById(R.id.item_image);
            cardCategory = itemView.findViewById(R.id.item);
        }
    }

    public interface ItemClickProduct {
        void detailProduct(Product item);
    }
}
