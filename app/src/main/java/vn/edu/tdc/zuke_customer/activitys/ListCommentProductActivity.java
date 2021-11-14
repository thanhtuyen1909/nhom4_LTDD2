package vn.edu.tdc.zuke_customer.activitys;

import android.annotation.SuppressLint;
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
import vn.edu.tdc.zuke_customer.adapters.CommentAdapter;
import vn.edu.tdc.zuke_customer.data_models.Cart;
import vn.edu.tdc.zuke_customer.data_models.CartDetail;
import vn.edu.tdc.zuke_customer.data_models.Rating;

public class ListCommentProductActivity extends AppCompatActivity {
    String productID = "", accountID = "";
    ArrayList<Rating> listComment;
    CommentAdapter commentAdapter;
    RecyclerView rcvComment;
    Toolbar toolbar;
    TextView subtitleAppbar;
    Intent intent;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference ratingRef = db.getReference("Rating");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list);

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleDSBL);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Lấy dữ liệu được gửi từ intent:
        intent = getIntent();
        accountID = intent.getStringExtra("item");
        productID = intent.getStringExtra("productID");

        // Khởi tạo biến:
        rcvComment = findViewById(R.id.list);
        listComment = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, listComment);

        // RecyclerView:
        rcvComment.setHasFixedSize(true);
        data();
        rcvComment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvComment.setAdapter(commentAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void data() {
        ratingRef.orderByChild("productID").equalTo(productID).addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listComment.clear();
                for (DataSnapshot node : snapshot.getChildren()) {
                    Rating rating = node.getValue(Rating.class);
                    if (rating.getProductID().equals(productID)) {
                        listComment.add(rating);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
