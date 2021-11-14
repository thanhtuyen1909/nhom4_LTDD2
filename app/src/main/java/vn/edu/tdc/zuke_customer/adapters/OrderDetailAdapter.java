package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.OrderDetail;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {
    Context context;
    ArrayList<OrderDetail> items;
    DatabaseReference proRef = FirebaseDatabase.getInstance().getReference("Products");

    public OrderDetailAdapter(Context context, ArrayList<OrderDetail> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public OrderDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderDetailAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_product_order, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailAdapter.ViewHolder holder, int position) {
        OrderDetail item = items.get(position);
        // Lấy tên, ảnh từ products:
        proRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    product.setKey(snapshot.getKey());
                    if(product.getKey().equals(item.getProductID())) {
                        holder.itemName.setText(product.getName());
                        StorageReference imageRef = FirebaseStorage.getInstance().getReference("images/products/"
                                + product.getName() + "/" + product.getImage());
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).resize(holder.itemImage.getWidth(), holder.itemImage.getHeight()).into(holder.itemImage));
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Lấy giá và số lượng từ orderdetails:
        holder.itemAmount.setText("Số lượng: " + item.getAmount());
        holder.itemPrice.setText(formatPrice(item.getPrice()));
        holder.itemTotal.setText(formatPrice(item.getPrice() * item.getAmount()));
    }

    private String formatPrice(int price) {
        String stmp = String.valueOf(price);
        int amount;
        amount = (int)(stmp.length() / 3);
        if (stmp.length() % 3 == 0)
            amount--;
        for (int i = 1; i <= amount; i++)
        {
            stmp = new StringBuilder(stmp).insert(stmp.length() - (i * 3) - (i - 1), ",").toString();
        }
        return stmp + " ₫";
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName, itemPrice, itemAmount, itemTotal;

        public ViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id.img);
            itemName = view.findViewById(R.id.txt_name);
            itemPrice = view.findViewById(R.id.txt_price);
            itemAmount = view.findViewById(R.id.txt_amount);
            itemTotal = view.findViewById(R.id.txt_total);
        }
    }
}
