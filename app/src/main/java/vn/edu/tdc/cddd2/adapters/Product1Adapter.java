package vn.edu.tdc.cddd2.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.edu.tdc.cddd2.R;
import vn.edu.tdc.cddd2.data_models.Product;

public class Product1Adapter extends RecyclerView.Adapter<Product1Adapter.ViewHolder> {
    ArrayList<Product> listProducts;
    private Context context;
    Product1Adapter.ItemClickListener itemClickListener;

    public void setItemClickListener(Product1Adapter.ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public Product1Adapter(ArrayList<Product> listProducts, Context context) {
        this.listProducts = listProducts;
        this.context = context;
    }

    @NonNull
    @Override
    public Product1Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_product_2,parent,false);
        Product1Adapter.ViewHolder viewHolder = new Product1Adapter.ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Product1Adapter.ViewHolder holder, int position) {
        Product item = listProducts.get(position);
        holder.im_item.setImageResource(R.drawable.ic_baseline_laptop_mac_24);
        holder.tv_name.setText(item.getName());
        holder.tv_price.setText("Giá: " + String.valueOf(item.getPrice()));
        holder.tv_manu.setText("Hãng: " + item.getManu());
        holder.tv_amount.setText("Số lượng: " + String.valueOf(item.getQuantity()));
        holder.onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(itemClickListener != null) {
                    itemClickListener.getInfor(item);
                } else {
                    return;
                }
            }
        };
    }

    @Override
    public int getItemCount() {
        return listProducts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView im_item, im_edit, im_delete;
        TextView tv_name, tv_price, tv_amount, tv_manu;
        View.OnClickListener onClickListener;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            im_item = itemView.findViewById(R.id.img);
            tv_name = itemView.findViewById(R.id.txt_name);
            tv_price = itemView.findViewById(R.id.txt_price);
            tv_amount = itemView.findViewById(R.id.txt_amount);
            tv_manu = itemView.findViewById(R.id.txt_manu);
            im_delete = itemView.findViewById(R.id.btnDelete);
            im_delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onClickListener != null) {
                onClickListener.onClick(v);
            }
        }
    }

    public interface ItemClickListener {
        void getInfor(Product item);
    }
}
