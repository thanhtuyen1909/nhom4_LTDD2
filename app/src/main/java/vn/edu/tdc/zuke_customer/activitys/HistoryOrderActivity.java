package vn.edu.tdc.zuke_customer.activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.CustomBottomNavigationView;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.OrderAdapter;
import vn.edu.tdc.zuke_customer.data_models.Order;
import vn.edu.tdc.zuke_customer.data_models.OrderDetail;
import vn.edu.tdc.zuke_customer.data_models.Rating;

public class HistoryOrderActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    Toolbar toolbar;
    TextView subtitleAppbar;
    ImageView buttonAction;
    String accountID = "";
    Handler handler = new Handler();
    RecyclerView recyclerView;
    ArrayList<Order> list;
    OrderAdapter adapter;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference orderRef = db.getReference("Order");
    DatabaseReference ratingRef = db.getReference("Rating");
    DatabaseReference order_detailRef = db.getReference("Order_Details");
    Intent intent;
    String check = "true";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);
        intent = getIntent();
        accountID = intent.getStringExtra("accountID");

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleDHCT);
        buttonAction = findViewById(R.id.buttonAction);
        buttonAction.setBackground(getResources().getDrawable(R.drawable.ic_round_filter_alt_24));
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        buttonAction.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.inflate(R.menu.popup_status_order);
            popupMenu.show();
        });

        // Đổ dữ liệu recycleview:
        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new OrderAdapter(list, this);
        data();
        adapter.setItemClickListener(itemClickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Khai báo và khởi tạo đối tượng itemClickListener
    private OrderAdapter.ItemClickListener itemClickListener = new OrderAdapter.ItemClickListener() {
        @Override
        public void getInfor(Order item) {
            intent = new Intent(HistoryOrderActivity.this, DetailHistoryOrderActivity.class);
            intent.putExtra("item", item);
            startActivity(intent);
        }

        @Override
        public void getRating(String key, View v) {
            intent = new Intent(HistoryOrderActivity.this, RatingActivity.class);
            intent.putExtra("key", key);
            ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    check = "true";
                    for (DataSnapshot node : snapshot.getChildren()) {
                        Rating rating = node.getValue(Rating.class);
                        if (rating.getOrderID().equals(key) && !rating.getComment().equals("")
                                && rating.getRating() != 0 && !rating.getCreated_at().equals("")) {
                            check = "false";
                            break;
                        } else if (rating.getOrderID().equals(key) && rating.getComment().equals("")
                                && rating.getRating() == 0 && rating.getCreated_at().equals("")) {
                            check = "edit";
                            break;
                        }
                    }
                    if (check.equals("true")) {
                        order_detailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot node : snapshot.getChildren()) {
                                    OrderDetail orderDetail = node.getValue(OrderDetail.class);
                                    if (orderDetail.getOrderID().equals(key)) {
                                        Rating rating1 = new Rating(key, "", "", orderDetail.getProductID(), 0);
                                        ratingRef.push().setValue(rating1);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        intent.putExtra("to", "write");
                    } else if (check.equals("edit")) {
                        intent.putExtra("to", "edit");
                    } else intent.putExtra("to", "read");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            handler.postDelayed(() -> {
                String transitionName = "a";
                ViewCompat.setTransitionName(v, transitionName);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(HistoryOrderActivity.this, v, transitionName);
                startActivity(intent, optionsCompat.toBundle());
            }, 200);
        }
    };

    // Lấy dữ liệu hiển thị:
    public void data() {
        orderRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Order order = snapshot.getValue(Order.class);
                    order.setOrderID(snapshot.getKey());
                    if (accountID.equals(order.getAccountID())) {
                        list.add(order);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.mChoXuLy:
                orderRef.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            order.setOrderID(snapshot.getKey());
                            if (accountID.equals(order.getAccountID()) && order.getStatus() == 1) {
                                list.add(order);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.mDangXuLy:
                orderRef.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            order.setOrderID(snapshot.getKey());
                            if (accountID.equals(order.getAccountID()) &&
                                    (order.getStatus() == 2 || order.getStatus() == 3 || order.getStatus() == 4)) {
                                list.add(order);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.mDangGiao:
                orderRef.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            order.setOrderID(snapshot.getKey());
                            if (accountID.equals(order.getAccountID()) && order.getStatus() == 5) {
                                list.add(order);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.mDaNhanHang:
                orderRef.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            order.setOrderID(snapshot.getKey());
                            if (accountID.equals(order.getAccountID()) && (order.getStatus() == 6 || order.getStatus() == 8)) {
                                list.add(order);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            case R.id.mHuy:
                orderRef.addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Order order = snapshot.getValue(Order.class);
                            order.setOrderID(snapshot.getKey());
                            if (accountID.equals(order.getAccountID()) && order.getStatus() == 0){
                                list.add(order);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                break;
            default:
                data();
                break;
        }
        return true;
    }
}
