package vn.edu.tdc.zuke_customer.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.OrderDetailAdapter;
import vn.edu.tdc.zuke_customer.data_models.Order;
import vn.edu.tdc.zuke_customer.data_models.OrderDetail;

public class DetailHistoryOrderActivity extends AppCompatActivity {
    // Khai báo biến:
    Toolbar toolbar;
    TextView subtitleAppbar;
    String from = "", accountID = "";
    TextView txtTotal, txtDate, txtStatus, txtNote, txtName, txtAddress, txtPhone;
    Intent intent;
    Order item = null;
    RecyclerView recyclerView;
    ArrayList<OrderDetail> list;
    OrderDetailAdapter adapter;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference statusRef = db.getReference("Status");
    DatabaseReference order_detailRef = db.getReference("Order_Details");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_order);

        // Nhận đối tượng item từ intent
        intent = getIntent();
        item = intent.getParcelableExtra("item");
        from = intent.getStringExtra("from");
        accountID = intent.getStringExtra("accountID");

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(item.getOrderID());
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Khởi tạo biến:
        txtTotal = findViewById(R.id.txt_tongtien);
        txtDate = findViewById(R.id.txt_date);
        txtStatus = findViewById(R.id.txt_status);
        txtNote = findViewById(R.id.txt_note);
        txtName = findViewById(R.id.txt_name);
        txtAddress = findViewById(R.id.txt_address);
        txtPhone = findViewById(R.id.txt_phone);

        // Đổ dữ liệu recycleview:
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new OrderDetailAdapter(this, list);
        data();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Kiểm tra nếu nhận được đối tượng item thì set dữ liệu
        if(item != null) {
            txtName.setText("Họ tên người nhận: " + item.getName());
            txtPhone.setText("Số điện thoại: " + item.getPhone());
            txtAddress.setText("Địa chỉ: " + item.getAddress());
            txtNote.setText(item.getNote());
            txtDate.setText(item.getCreated_at());
            txtTotal.setText(formatPrice(item.getTotal()));
            statusRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if(Integer.parseInt(snapshot.getKey()) == item.getStatus()) {
                            txtStatus.setText(snapshot.getValue(String.class));
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(from.equals("payment")) {
            startActivity(new Intent(DetailHistoryOrderActivity.this, HomeScreenActivity.class).putExtra("account", accountID));
        }
        onBackPressed();
        return true;
    }

    // Lấy dữ liệu hiển thị:
    private void data() {
        order_detailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    OrderDetail orderDetail = node.getValue(OrderDetail.class);
                    if(orderDetail.getOrderID().equals(item.getOrderID())) {
                        list.add(orderDetail);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Format tiền:
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
}
