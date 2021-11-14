package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.activitys.DetailProductActivity;
import vn.edu.tdc.zuke_customer.data_models.ManuProduct;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class ManuProductAdapter extends RecyclerView.Adapter<ManuProductAdapter.ViewHolder> {
    ArrayList<ManuProduct> list;
    Context context;
    String accountID = "";
    ArrayList<Product> listProduct = new ArrayList<Product>();

    public void setAccountID(String accountID) {
        this.accountID = accountID;
    }

    public ManuProductAdapter(Context context, ArrayList<ManuProduct> items) {
        this.context = context;
        this.list = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ManuProductAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_catelogy_each_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ManuProduct item = list.get(position);
        holder.textView.setText(item.getNameManu());
        boolean isExpandable = item.isExpandable();
        holder.expandable_layout.setVisibility(isExpandable ? View.VISIBLE : View.GONE);
        if(isExpandable) holder.button_arrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24);
        else holder.button_arrow.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_24);
        holder.linear_layout.setOnClickListener(v -> {
            item.setExpandable(!item.isExpandable());
            listProduct = item.getListProduct();
            notifyItemChanged(holder.getAdapterPosition());
        });

        // Product:
        Product1Adapter adapter = new Product1Adapter(context, listProduct);
        adapter.setItemClickListener(itemClickProduct);
        holder.rcvProduct.setLayoutManager(new GridLayoutManager(holder.itemView.getContext(), 2));
        holder.rcvProduct.setHasFixedSize(true);
        holder.rcvProduct.setAdapter(adapter);
    }

    private final Product1Adapter.ItemClickProduct itemClickProduct = item -> {
        Intent intent = new Intent(context, DetailProductActivity.class);
        intent.putExtra("item", item);
        intent.putExtra("accountID", accountID);
        context.startActivity(intent);
    };

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout linear_layout;
        RelativeLayout expandable_layout;
        ImageView button_arrow;
        TextView textView;
        RecyclerView rcvProduct;
        public ViewHolder(View view) {
            super(view);
            linear_layout = view.findViewById(R.id.linear_layout);
            expandable_layout = view.findViewById(R.id.expandable_layout);
            button_arrow = view.findViewById(R.id.button_arrow);
            textView = view.findViewById(R.id.itemTv);
            rcvProduct = view.findViewById(R.id.child_rv);
        }
    }


}
