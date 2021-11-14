package vn.edu.tdc.zuke_customer.activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.CustomBottomNavigationView;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.FavoriteAdapter;
import vn.edu.tdc.zuke_customer.data_models.Cart;
import vn.edu.tdc.zuke_customer.data_models.CartDetail;
import vn.edu.tdc.zuke_customer.data_models.Favorite;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class FavoriteActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener{
    String accountID = "", cartID = "";
    boolean check = true;
    RecyclerView recyclerView;
    ArrayList<Favorite> list;
    FavoriteAdapter adapter;
    TextView title, mess, subtitleAppbar;
    CustomBottomNavigationView customBottomNavigationView;
    Intent intent;
    Toolbar toolbar;
    ImageView buttonAction;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference cartRef = db.getReference("Cart");
    DatabaseReference cartDetailRef = db.getReference("Cart_Detail");
    DatabaseReference favoriteRef = db.getReference("Favorite");
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_favorite);

        // Nhận dữ liệu từ intent:
        intent = getIntent();
        accountID = intent.getStringExtra("accountID");

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleDSYT);
        buttonAction = findViewById(R.id.buttonAction);
        buttonAction.setBackground(getResources().getDrawable(R.drawable.ic_round_notifications_24));
        buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(FavoriteActivity.this, NotificationActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
            }
        });

        // Bottom navigation:
        customBottomNavigationView = findViewById(R.id.customBottomBar);
        customBottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        customBottomNavigationView.setOnItemSelectedListener(this);
        customBottomNavigationView.getMenu().findItem(R.id.mFavorite).setChecked(true);

        //RecycleView
        recyclerView = findViewById(R.id.list);
        recyclerView.setHasFixedSize(true);
        list = new ArrayList<>();
        adapter = new FavoriteAdapter(this, list);
        data();
        adapter.setItemClickListener(itemClick);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private final FavoriteAdapter.ItemClick itemClick = new FavoriteAdapter.ItemClick() {
        @Override
        public void addCart(String productID, int price) {
            // Kiểm tra đã có giỏ hàng chưa?
            cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    cartID = "";
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        if (dataSnapshot.child("accountID").getValue(String.class).equals(accountID)) {
                            // Nếu có thì? -> lấy CartID
                            cartID = dataSnapshot.getKey();
                            cartDetailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                    check = true;
                                    int total = dataSnapshot.child("total").getValue(Integer.class);
                                    for(DataSnapshot dataSnapshot1 : snapshot1.getChildren()) {
                                        CartDetail cartDetail = dataSnapshot1.getValue(CartDetail.class);
                                        cartDetail.setKey(dataSnapshot1.getKey());
                                        if(cartDetail.getCartID().equals(cartID) && cartDetail.getProductID().equals(productID)) {
                                            check = false;
                                            int amount = cartDetail.getAmount() + 1;
                                            cartDetailRef.child(cartDetail.getKey()).child("amount").setValue(amount);
                                            cartDetailRef.child(cartDetail.getKey()).child("price").setValue(price);
                                            cartRef.child(cartID).child("total").setValue(total + price);
                                            break;
                                        }
                                    }
                                    if(check) {
                                        CartDetail cartDetail = new CartDetail(cartID, productID, 1, price);
                                        cartDetailRef.push().setValue(cartDetail);
                                        cartRef.child(cartID).child("total").setValue(total + price);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            break;
                        }
                    }
                    // Nếu chưa thì? -> tạo mới
                    if (cartID.equals("")) {
                        Cart cart = new Cart(accountID, 0);
                        String key = cartRef.push().getKey();
                        cartRef.child(key).setValue(cart);
                        cartRef.child(key).child("total").setValue(price);
                        CartDetail cartDetail = new CartDetail(key, productID, 1, price);
                        cartDetailRef.push().setValue(cartDetail);
                        cartRef.child(key).child("total").setValue(price);
                    }

                    showSuccesDialog("Thêm vào giỏ hàng thành công!");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void deleteFavorite(String key) {
            favoriteRef.child(key).removeValue();
            adapter.notifyDataSetChanged();
        }

        @Override
        public void detailProduct(Product product) {
            intent = new Intent(FavoriteActivity.this, DetailProductActivity.class);
            intent.putExtra("item", product);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
        }
    };

    public void data() {
        favoriteRef.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot node : snapshot.getChildren()) {
                    Favorite favorite = node.getValue(Favorite.class);
                    favorite.setKey(node.getKey());
                    if (favorite.getUserId().equals(accountID)) {
                        list.add(favorite);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(FavoriteActivity.this).inflate(
                R.layout.layout_warning_dialog,
                findViewById(R.id.layoutDialogContainer)
        );
        builder.setView(view);
        title = view.findViewById(R.id.textTitle);
        title.setText(R.string.title);
        mess = view.findViewById(R.id.textMessage);
        mess.setText(notify);
        ((TextView) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.yes));

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(v -> {
            alertDialog.dismiss();
        });


        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void showSuccesDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FavoriteActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(FavoriteActivity.this).inflate(
                R.layout.layout_succes_dialog,
                findViewById(R.id.layoutDialogContainer)
        );
        builder.setView(view);
        title = view.findViewById(R.id.textTitle);
        title.setText(R.string.title);
        mess = view.findViewById(R.id.textMessage);
        mess.setText(notify);
        ((TextView) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.okay));

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setVisibility(View.GONE);

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        handler.postDelayed(alertDialog::dismiss, 1500);
    }

    // Sự kiện click các item trong bottom navigation
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.mHome:
                intent = new Intent(FavoriteActivity.this, HomeScreenActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mCategory:
                intent = new Intent(FavoriteActivity.this, CategoryActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mCart:
                intent = new Intent(FavoriteActivity.this, CartActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                break;
            case R.id.mProfile:
                intent = new Intent(FavoriteActivity.this, ProfileScreenActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mFavorite:
                break;
            default:
                Toast.makeText(FavoriteActivity.this, "Vui lòng chọn chức năng khác", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }
}
