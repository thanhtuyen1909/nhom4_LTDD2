package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.CartDetail;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class CartDetailTTAdapter extends RecyclerView.Adapter<CartDetailTTAdapter.ViewHolder> {
    private Context context;
    private ArrayList<CartDetail> list;
    DatabaseReference proRef = FirebaseDatabase.getInstance().getReference("Products");
    DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart");
    DatabaseReference cartDetailRef = FirebaseDatabase.getInstance().getReference("Cart_Detail");

    public CartDetailTTAdapter(Context context, ArrayList<CartDetail> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CartDetailTTAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.item_product_order, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartDetail item = list.get(position);
        holder.itemName.setText("");
        holder.itemPrice.setText("");
        holder.itemTotal.setText("");
        holder.itemAmount.setText("");
        proRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    Product product = node.getValue(Product.class);
                    if (node.getKey().equals(item.getProductID())) {
                        if (product.getStatus() == -1) {
                            cartDetailRef.child(item.getKey()).removeValue();
                            cartRef.child(item.getCartID()).child("total").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    cartRef.child(item.getCartID()).child("total").setValue(snapshot.getValue(Integer.class) - item.getPrice() * item.getAmount());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        } else {
                            //set name
                            holder.itemName.setText(product.getName());
                            //set gia san pham
                            holder.itemPrice.setText(formatPrice(item.getPrice()));

                            holder.itemAmount.setText("Số lương : " + item.getAmount());
                            holder.itemTotal.setText(formatPrice(item.getPrice() * item.getAmount()));
                            //set hinh anh
                            FirebaseStorage storage = FirebaseStorage.getInstance();
                            final long ONE_MEGABYTE = 1024 * 1024;
                            StorageReference imageRef = storage.getReference("images/products/" + product.getName() + "/" + product.getImage());
                            imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    holder.itemImage.setImageBitmap(Bitmap.createScaledBitmap(bmp, holder.itemImage.getWidth(), holder.itemImage.getHeight(), false));
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView itemImage;
        private TextView itemName, itemPrice, itemTotal, itemAmount;
        View.OnClickListener onClickListener;

        public ViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id.img);
            itemName = view.findViewById(R.id.txt_name);
            itemPrice = view.findViewById(R.id.txt_price);
            itemTotal = view.findViewById(R.id.txt_total);
            itemAmount = view.findViewById(R.id.txt_amount);
        }

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
        }
    }

    private String formatPrice(int price) {
        String stmp = String.valueOf(price);
        int amount;
        amount = (int) (stmp.length() / 3);
        if (stmp.length() % 3 == 0)
            amount--;
        for (int i = 1; i <= amount; i++) {
            stmp = new StringBuilder(stmp).insert(stmp.length() - (i * 3) - (i - 1), ",").toString();
        }
        return stmp + " ₫";
    }
}
