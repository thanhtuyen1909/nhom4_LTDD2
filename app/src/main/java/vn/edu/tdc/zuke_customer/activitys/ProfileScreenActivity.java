package vn.edu.tdc.zuke_customer.activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import vn.edu.tdc.zuke_customer.CustomBottomNavigationView;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Customer;
import vn.edu.tdc.zuke_customer.data_models.Order;

public class ProfileScreenActivity extends AppCompatActivity implements View.OnClickListener, NavigationBarView.OnItemSelectedListener{
    String accountID = "";
    ImageView imgCustomer, imgType;
    TextView name, phone, donDM, donHT, donH, type;
    RelativeLayout lsDH, hoTro, gioiThieu, dangXuat, ttCaNhan;
    private CustomBottomNavigationView customBottomNavigationView;
    Intent intent;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference accountRef;
    DatabaseReference cusRef = db.getReference("Customer");
    DatabaseReference cusTypeRef = db.getReference("CustomerType");
    DatabaseReference orderRef = db.getReference("Order");
    int numDM, numHT, numH;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);

        // Nhận dữ liệu từ intent:
        intent = getIntent();
        accountID = intent.getStringExtra("accountID");
        accountRef = db.getReference("Account/" + accountID);


        // Bottom navigation:
        customBottomNavigationView = findViewById(R.id.customBottomBar);
        customBottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        customBottomNavigationView.setOnItemSelectedListener(this);
        customBottomNavigationView.getMenu().findItem(R.id.mProfile).setChecked(true);

        // Khởi tạo biến:
        imgCustomer = findViewById(R.id.profile);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        donDM = findViewById(R.id.donmua);
        donHT = findViewById(R.id.donhoanthanh);
        donH = findViewById(R.id.donhuy);
        lsDH = findViewById(R.id.lichsudh);
        hoTro = findViewById(R.id.hotro);
        gioiThieu = findViewById(R.id.gioithieu);
        dangXuat = findViewById(R.id.dangxuat);
        imgType = findViewById(R.id.imgType);
        type = findViewById(R.id.type);
        ttCaNhan = findViewById(R.id.ttcanhan);

        // Gọi hàm lấy dữ liệu:
        data();

        // Set sự kiện click cho các đối tượng:
        lsDH.setOnClickListener(this);
        hoTro.setOnClickListener(this);
        gioiThieu.setOnClickListener(this);
        dangXuat.setOnClickListener(this);
        ttCaNhan.setOnClickListener(this);
    }

    // Hàm lấy dữ liệu:
    private void data() {
        // Lấy dữ liệu thông tin khách hàng:
        cusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Customer customer = snapshot1.getValue(Customer.class);
                    customer.setKey(snapshot1.getKey());
                    if (customer.getAccountID().equals(accountID)) {
                        name.setText(customer.getName());
                        accountRef.child("username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                phone.setText(snapshot.getValue(String.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        cusTypeRef.child(customer.getType_id()).child("name").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                type.setText(snapshot.getValue(String.class));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        switch (customer.getType_id()) {
                            case "Type1":
                                imgType.setImageResource(R.drawable.ic_baseline_emoji_events_24_sliver);
                                break;
                            case "Type2":
                                imgType.setImageResource(R.drawable.ic_baseline_emoji_events_24_gold);
                                break;
                            case "Type3":
                                imgType.setImageResource(R.drawable.ic_baseline_emoji_events_24_diamond);
                                break;
                        }
                        if(!customer.getImage().equals("")) Picasso.get().load(customer.getImage()).fit().into(imgCustomer);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Lấy dữ liệu đơn hàng:
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numDM = 0;
                numH = 0;
                numHT = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Order order = dataSnapshot.getValue(Order.class);
                    if (order.getAccountID().equals(accountID)) {
                        numDM++;
                        if (order.getStatus() == 8) {
                            numHT++;
                        } else if (order.getStatus() == 0) {
                            numH++;
                        }
                    }
                }
                // Đã mua:
                donDM.setText(numDM + "");
                // Hoàn thành:
                donHT.setText(numHT + "");
                // Huỷ:
                donH.setText(numH + "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v == lsDH) {
            intent = new Intent(ProfileScreenActivity.this, HistoryOrderActivity.class);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
        } else if (v == hoTro) {
            intent = new Intent(ProfileScreenActivity.this, SupportActivity.class);
            startActivity(intent);
        } else if (v == gioiThieu) {
            intent = new Intent(ProfileScreenActivity.this, IntroduceAppActivity.class);
            startActivity(intent);
        } else if (v == dangXuat) {
            intent = new Intent(ProfileScreenActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(ProfileScreenActivity.this, EditProfileActivity.class);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
        }
    }

    // Sự kiện click các item trong bottom navigation
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.mHome:
                intent = new Intent(ProfileScreenActivity.this, HomeScreenActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mCategory:
                intent = new Intent(ProfileScreenActivity.this, CategoryActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mCart:
                intent = new Intent(ProfileScreenActivity.this, CartActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                break;
            case R.id.mProfile:
                break;
            case R.id.mFavorite:
                intent = new Intent(ProfileScreenActivity.this, FavoriteActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            default:
                Toast.makeText(ProfileScreenActivity.this, "Vui lòng chọn chức năng khác", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}
