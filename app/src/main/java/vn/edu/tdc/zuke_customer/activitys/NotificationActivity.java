package vn.edu.tdc.zuke_customer.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.CartDetailAdapter;
import vn.edu.tdc.zuke_customer.adapters.NotificationAdapter;
import vn.edu.tdc.zuke_customer.data_models.CartDetail;
import vn.edu.tdc.zuke_customer.data_models.Notification;

public class NotificationActivity extends AppCompatActivity {
    String accountID = "-MnFno1Jzj8tuduSeAw4";
    RecyclerView recycleView;
    Toolbar toolbar;
    TextView subtitleAppbar;
    Intent intent;

    ArrayList<Notification> listNotify;
    NotificationAdapter notificationAdapter;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference notiRef = FirebaseDatabase.getInstance().getReference("Notification");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);

        // Nhận dữ liệu từ intent:
        intent = getIntent();
        accountID = intent.getStringExtra("accountID");

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleTB);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Khởi tạo biến:
        recycleView = findViewById(R.id.list);
        recycleView.setHasFixedSize(true);
        listNotify = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(this, listNotify);
        notificationAdapter.setItemClickListener(itemClickListener);
        data();
        recycleView.setAdapter(notificationAdapter);
        recycleView.setLayoutManager(new LinearLayoutManager(this));

        // Trượt xoá thông báo:
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        recycleView.addItemDecoration(itemDecoration);
    }

    private NotificationAdapter.ItemClickListener itemClickListener = id -> notiRef.child(id).removeValue();

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void data() {
        notiRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listNotify.clear();
                for (DataSnapshot node : snapshot.getChildren()) {
                    Notification noti = node.getValue(Notification.class);
                    noti.setKey(node.getKey());
                    if (noti.getAccountID().equals(accountID)) {
                        listNotify.add(noti);
                    }
                }
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
