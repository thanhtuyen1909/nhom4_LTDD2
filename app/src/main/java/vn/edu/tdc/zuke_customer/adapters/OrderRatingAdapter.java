package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import vn.edu.tdc.zuke_customer.data_models.Product;
import vn.edu.tdc.zuke_customer.data_models.Rating;

public class OrderRatingAdapter extends RecyclerView.Adapter<OrderRatingAdapter.ViewHolder> {
    Context context;
    ArrayList<Rating> items;
    DatabaseReference proRef = FirebaseDatabase.getInstance().getReference("Products");
    ArrayList<String> list;
    RecommendRatingAdapter adapter;
    String to = "";

    public OrderRatingAdapter(Context context, ArrayList<Rating> items) {
        this.context = context;
        this.items = items;
    }

    public void setTo(String to) {
        this.to = to;
    }

    @NonNull
    @Override
    public OrderRatingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrderRatingAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_rating_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull OrderRatingAdapter.ViewHolder holder, int position) {
        Rating item = items.get(position);
        // Lấy tên, ảnh từ products:
        proRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Product product = snapshot.getValue(Product.class);
                    product.setKey(snapshot.getKey());
                    if (product.getKey().equals(item.getProductID())) {
                        holder.txtName.setText(product.getName());
                        StorageReference imageRef = FirebaseStorage.getInstance().getReference("images/products/"
                                + product.getName() + "/" + product.getImage());
                        imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).resize(holder.imageView.getWidth(), holder.imageView.getHeight()).into(holder.imageView));
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        holder.ratingBar.setRating(item.getRating());
        holder.txtComment.setText(item.getComment());
        if(item.getReply() != null) {
            holder.layout.setVisibility(View.VISIBLE);
            holder.txtReply.setText(item.getReply().getReplyComment());
        }
        // RecycleView:
        holder.recyclerView.setHasFixedSize(true);
        list = new ArrayList<String>();
        list.add("Chất lượng sản phẩm tuyệt vời");
        list.add("Giá cả phù hợp");
        list.add("Rất đáng tiền");
        list.add("Sản phẩm tạm được");
        list.add("Sản phẩm kém chất lượng");
        adapter = new RecommendRatingAdapter(context, list);
        holder.recyclerView.setAdapter(adapter);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        if (to.equals("read")) {
            holder.ratingBar.setFocusable(false);
            holder.ratingBar.setIsIndicator(true);
            holder.txtComment.setEnabled(false);
        }
        holder.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> item.setRating((int) rating));
        if (!to.equals("read")) {
            adapter.itemClickListener = s -> {
                if (holder.txtComment.getText().toString().equals("")) {
                    holder.txtComment.setText(s);
                } else {
                    holder.txtComment.setText(holder.txtComment.getText().toString() + ".\n" + s);
                }
            };
        }
        holder.txtComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                item.setComment(String.valueOf(holder.txtComment.getText()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView txtName, txtReply;
        RecyclerView recyclerView;
        RatingBar ratingBar;
        EditText txtComment;
        RelativeLayout layout;

        public ViewHolder(View view) {
            super(view);
            txtComment = view.findViewById(R.id.comment);
            recyclerView = view.findViewById(R.id.list);
            ratingBar = view.findViewById(R.id.simpleRatingBar);
            imageView = view.findViewById(R.id.img);
            txtName = view.findViewById(R.id.txt_name);
            txtReply = view.findViewById(R.id.txt_replycomment);
            layout = view.findViewById(R.id.layout);
        }
    }
}
