package vn.edu.tdc.zuke_customer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Customer;
import vn.edu.tdc.zuke_customer.data_models.Rating;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Rating> items;
    DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer");
    DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Order");
    Query queryCustomer;

    public CommentAdapter(Context context, ArrayList<Rating> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentAdapter.ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_comment_1, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdapter.ViewHolder holder, int position) {
        Rating item = items.get(position);
        orderRef.child(item.getOrderID()).child("accountID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                customerRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                        for (DataSnapshot node : snapshot1.getChildren()) {
                            Customer customer = node.getValue(Customer.class);
                            if (customer.getAccountID().equals(snapshot.getValue(String.class))) {
                                holder.itemName.setText(customer.getName());
                                holder.itemDetail.setText(item.getComment());
                                holder.itemTime.setText(timeDiff(item.getCreated_at(), new Date()));
                                if(!customer.getImage().equals("")) Picasso.get().load(customer.getImage()).fit().into(holder.itemImage);
                                holder.itemRating.setRating(item.getRating());
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Hàm hiển thị thời gian
    private String timeDiff(String created, Date now) {
        String time = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date create = sdf.parse(created, new ParsePosition(0));
        long diff = now.getTime() - create.getTime();
        int minute = (int) (diff / (1000 * 60));

        if (minute == 0) {
            time = "Bây giờ";
        } else if (minute <= 59) {
            time = minute + " phút trước";
        } else if (minute >= 60) {
            int hour = (int) (diff / (1000 * 60 * 60));

            if (hour <= 23) {
                time = hour + " giờ trước";
            } else {
                int day = (int) (diff / (1000 * 60 * 60 * 24));
                time = day + " ngày trước";
            }
        }
        return time;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        TextView itemName, itemDetail, itemTime;
        RatingBar itemRating;

        public ViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id.img);
            itemName = view.findViewById(R.id.txt_name);
            itemDetail = view.findViewById(R.id.txt_detail);
            itemTime = view.findViewById(R.id.txt_time);
            itemRating = view.findViewById(R.id.simpleRatingBar1);
        }
    }
}
