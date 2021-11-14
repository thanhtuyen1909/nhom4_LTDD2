package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

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
import vn.edu.tdc.zuke_customer.data_models.Favorite;
import vn.edu.tdc.zuke_customer.data_models.OfferDetail;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    Context context;
    ArrayList<Favorite> items = new ArrayList<>();
    FavoriteAdapter.ItemClick itemClick;
    Handler handler = new Handler();
    Product product1 = null;
    DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");
    DatabaseReference favRef = FirebaseDatabase.getInstance().getReference("Favorite");
    DatabaseReference offerDetailRef = FirebaseDatabase.getInstance().getReference("Offer_Details");

    public FavoriteAdapter(Context context, ArrayList<Favorite> items) {
        this.context = context;
        this.items = items;
    }

    public void setItemClickListener(FavoriteAdapter.ItemClick itemClickListener) {
        this.itemClick = itemClickListener;
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FavoriteAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_product_search_favorite, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position) {
        Favorite favorite = items.get(position);
        holder.itemTitle.setText("");
        holder.itemPrice.setText("");
        holder.itemPriceMain.setText("");
        holder.itemRating.setText("");
        holder.itemRatingAmount.setText("");
        // Thông tin sản phẩm:
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    Product product = node.getValue(Product.class);
                    product.setKey(node.getKey());
                    if (product.getKey().equals(favorite.getProductId())) {
                        if(product.getStatus() == -1) {
                            favRef.child(favorite.getKey()).removeValue();
                            notifyDataSetChanged();
                        } else {
                            product1 = product;
                            //Tên
                            holder.itemTitle.setText(product.getName());
                            //Giá
                            offerDetailRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int maxSale = 0;
                                    for (DataSnapshot node1 : snapshot.getChildren()) {
                                        OfferDetail detail = node1.getValue(OfferDetail.class);
                                        if (detail.getProductID().equals(favorite.getProductId())) {
                                            if (detail.getPercentSale() > maxSale) {
                                                maxSale = detail.getPercentSale();
                                            }
                                        }
                                    }
                                    if (maxSale != 0) {
                                        holder.itemPriceMain.setVisibility(View.VISIBLE);
                                        int discount = product.getPrice() / 100 * (100 - maxSale);
                                        holder.itemPrice.setText(formatPrice(discount));
                                        holder.itemPriceMain.setText(formatPrice(product.getPrice()));
                                        holder.itemPriceMain.setPaintFlags(holder.itemPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                    } else {
                                        holder.itemPrice.setText(formatPrice(product.getPrice()));
                                        holder.itemPriceMain.setVisibility(View.INVISIBLE);
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //Ảnh
                            StorageReference imageRef =  FirebaseStorage.getInstance().getReference("images/products/" + product.getName() + "/" + product.getImage());
                            imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).resize(holder.itemImage.getWidth(), holder.itemImage.getHeight()).into(holder.itemImage));
                            //Rating:
                            if(product.getRating() > 0) {
                                holder.itemRating.setText(product.getRating() + "");
                            } else {
                                holder.itemRating.setVisibility(View.GONE);
                            }

                            //Đã bán:
                            if (product.getSold() > 0) {
                                holder.itemRatingAmount.setText(product.getSold() + " đã bán");
                            } else {
                                holder.itemRatingAmount.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.bt_favorite.setChecked(true);
        holder.onClickListener = v -> {
            if (itemClick != null) {
                handler.postDelayed(() -> {
                    if (v == holder.bt_favorite) {
                        itemClick.deleteFavorite(favorite.getKey());
                    } else if (v == holder.bt_cart) {
                        itemClick.addCart(favorite.getProductId(), formatInt(holder.itemPrice.getText() + ""));
                    } else {
                        if(product1 != null) itemClick.detailProduct(product1);
                    }
                }, 50);
            } else {
                return;
            }
        };
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

    private int formatInt(String price) {
        return Integer.parseInt(price.substring(0, price.length() - 2).replace(",", ""));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ToggleButton bt_favorite;
        ImageView itemImage, bt_cart;
        TextView itemTitle, itemPrice, itemPriceMain, itemRating, itemRatingAmount;
        View.OnClickListener onClickListener;

        public ViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id.img);
            itemTitle = view.findViewById(R.id.txt_name);
            itemPrice = view.findViewById(R.id.txt_price);
            itemPriceMain = view.findViewById(R.id.txt_pricemain);
            itemRating = view.findViewById(R.id.item_rating);
            itemRatingAmount = view.findViewById(R.id.item_rating_amount);
            bt_favorite = view.findViewById(R.id.button_favorite);
            bt_cart = view.findViewById(R.id.addCart);
            bt_cart.setOnClickListener(this);
            bt_favorite.setOnClickListener(this);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onClickListener != null) {
                onClickListener.onClick(v);
            }
        }
    }

    public interface ItemClick {
        void addCart(String productID, int price);

        void deleteFavorite(String key);

        void detailProduct(Product item);
    }
}
