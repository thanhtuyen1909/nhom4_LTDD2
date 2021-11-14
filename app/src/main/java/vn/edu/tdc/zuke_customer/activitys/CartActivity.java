package vn.edu.tdc.zuke_customer.activitys;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
import java.util.HashMap;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.CartDetailAdapter;
import vn.edu.tdc.zuke_customer.data_models.Cart;
import vn.edu.tdc.zuke_customer.data_models.CartDetail;
import vn.edu.tdc.zuke_customer.data_models.OfferDetail;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class CartActivity extends AppCompatActivity {
    Toolbar toolbar;
    Intent intent;
    TextView subtitleAppbar, title, mess;
    ImageView buttonAction;
    String accountID = "";
    RecyclerView cartRecycleView;
    Button btnPayment;
    ArrayList<CartDetail> listCart;
    CartDetailAdapter cartAdapter;
    Handler handler = new Handler();

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference proRef = db.getReference("Products");
    DatabaseReference ref = db.getReference("Cart");
    DatabaseReference detailRef = db.getReference("Cart_Detail");
    int total = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_list_cart);

        // Nhận dữ liệu từ intent:
        intent = getIntent();
        accountID = intent.getStringExtra("accountID");

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleGH);
        buttonAction = findViewById(R.id.buttonAction);
        buttonAction.setBackground(getResources().getDrawable(R.drawable.ic_baseline_delete_24));
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonAction.setOnClickListener(v -> {
            showErrorDialog("Bạn có muốn làm sạch giỏ hàng?");
        });

        // Khởi tạo biến:
        btnPayment = findViewById(R.id.buttonThanhToan);
        cartRecycleView = findViewById(R.id.listProduct);
        cartRecycleView.setHasFixedSize(true);
        listCart = new ArrayList<>();
        cartAdapter = new CartDetailAdapter(this, listCart);
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
            }
        });

        // Gán dữ liệu:
        data();
        cartAdapter.setItemClickListener(itemClickListener);
        cartRecycleView.setAdapter(cartAdapter);
        cartRecycleView.setLayoutManager(new LinearLayoutManager(this));

        // Trượt xoá giỏ hàng:
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        cartRecycleView.addItemDecoration(itemDecoration);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Lấy dữ liệu:
    private void data() {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    Cart cart = node.getValue(Cart.class);
                    cart.setCartID(node.getKey());
                    if (cart.getAccountID().equals(accountID)) {
                        detailRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                listCart.clear();
                                total = 0;
                                for (DataSnapshot node1 : snapshot.getChildren()) {
                                    CartDetail detail = node1.getValue(CartDetail.class);
                                    detail.setKey(node1.getKey());
                                    if (cart.getCartID().equals(detail.getCartID())) {
                                        listCart.add(detail);
                                        total += detail.getPrice();
                                    }
                                }
                                btnPayment.setText("Tổng tiền : " + formatPrice(cart.getTotal()));
                                cartAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Xử lý click đối tượng
    private CartDetailAdapter.ItemClickListener itemClickListener = new CartDetailAdapter.ItemClickListener() {
        @Override
        public void changeQuantity(CartDetail item, int value) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("cartID", item.getCartID());
            map.put("amount", value);
            map.put("productID", item.getProductID());
            proRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                    for (DataSnapshot node : snapshot.getChildren()) {
                        Product product = node.getValue(Product.class);
                        if (node.getKey().equals(item.getProductID())) {
                            DatabaseReference promoRef = FirebaseDatabase.getInstance().getReference("Offer_Details");
                            promoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    int maxSale = 0;
                                    for (DataSnapshot node1 : snapshot.getChildren()) {
                                        OfferDetail detail = node1.getValue(OfferDetail.class);
                                        if (detail.getProductID().equals(item.getProductID())) {
                                            if (detail.getPercentSale() > maxSale) {
                                                maxSale = detail.getPercentSale();
                                            }
                                        }
                                    }
                                    if (maxSale != 0) {
                                        int priceDiscount = product.getPrice() / 100 * (100 - maxSale);
                                        item.setPrice(priceDiscount);
                                        map.put("price", item.getPrice());
                                        DatabaseReference detailRef = db.getReference("Cart_Detail");
                                        detailRef.child(item.getKey()).updateChildren(map);
                                        cartAdapter.notifyDataSetChanged();
                                    } else {
                                        item.setPrice(product.getPrice());
                                        map.put("price", item.getPrice());
                                        DatabaseReference detailRef = db.getReference("Cart_Detail");
                                        detailRef.child(item.getKey()).updateChildren(map);
                                        cartAdapter.notifyDataSetChanged();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            //update Cart total
            updateCartTotal(item.getCartID());

        }

        @Override
        public void delete(String id, int price) {
            detailRef.child(id).removeValue().addOnSuccessListener(unused -> ref.orderByChild("accountID").equalTo(accountID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        Cart cart = snapshot1.getValue(Cart.class);
                        cart.setTotal(cart.getTotal() - price);
                        ref.child(snapshot1.getKey()).setValue(cart);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            }));
        }
    };

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

    private void updateCartTotal(String cartID) {
        detailRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                for (DataSnapshot node : snapshot.getChildren()) {
                    CartDetail detail = node.getValue(CartDetail.class);
                    if (detail.getCartID().equals(cartID)) {
                        total += detail.getPrice() * detail.getAmount();
                    }
                }
                HashMap<String, Object> map = new HashMap<>();
                map.put("accountID", accountID);
                map.put("total", total);
                ref.child(cartID).updateChildren(map);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Thông báo:
    private void showErrorDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(CartActivity.this).inflate(
                R.layout.layout_error_dialog,
                findViewById(R.id.layoutDialogContainer)
        );
        builder.setView(view);
        title = view.findViewById(R.id.textTitle);
        title.setText(R.string.title);
        mess = view.findViewById(R.id.textMessage);
        mess.setText(notify);
        ((TextView) view.findViewById(R.id.buttonYes)).setText(getResources().getString(R.string.yes));
        ((TextView) view.findViewById(R.id.buttonNo)).setText(getResources().getString(R.string.no));

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonYes).setOnClickListener(v -> {
            alertDialog.dismiss();
            // Xoá hết:
            ref.orderByChild("accountID").equalTo(accountID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                        String cartID = snapshot1.getKey();
                        ref.child(cartID).child("total").setValue(0);
                        detailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot node : snapshot.getChildren()) {
                                    CartDetail detail = node.getValue(CartDetail.class);
                                    detail.setKey(node.getKey());
                                    if (detail.getCartID().equals(cartID)) {
                                        detailRef.child(detail.getKey()).removeValue();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            showSuccesDialog("Đã làm sạch giỏ hàng, hãy quay lại trang chủ để tiếp tục mua hàng!");
        });

        view.findViewById(R.id.buttonNo).setOnClickListener(v -> alertDialog.dismiss());

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private void showSuccesDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(CartActivity.this).inflate(
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
}
