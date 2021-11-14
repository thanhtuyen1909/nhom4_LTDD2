package vn.edu.tdc.zuke_customer.activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.CartDetailTTAdapter;
import vn.edu.tdc.zuke_customer.data_models.Area;
import vn.edu.tdc.zuke_customer.data_models.Cart;
import vn.edu.tdc.zuke_customer.data_models.CartDetail;
import vn.edu.tdc.zuke_customer.data_models.Customer;
import vn.edu.tdc.zuke_customer.data_models.DiscountCode;
import vn.edu.tdc.zuke_customer.data_models.DiscountCode_Customer;
import vn.edu.tdc.zuke_customer.data_models.Order;
import vn.edu.tdc.zuke_customer.data_models.OrderDetail;

public class PaymentActivity extends AppCompatActivity {
    Toolbar toolbar;
    Intent intent;
    ImageView btnMap;
    Button btnSubmit;
    TextInputEditText edtAddress, edtName, edtPhone, edtDiscountCode, edtNote;
    RecyclerView productRecyclerView;
    TextView txtTotal, txtTransportFee, txtDiscount, txtRemain, title, mess, subtitleAppbar;
    ArrayList<CartDetail> listCart;
    CartDetailTTAdapter cartAdapter;
    int total = 0;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference cartRef = db.getReference("Cart");
    DatabaseReference detailRef = db.getReference("Cart_Detail");
    DatabaseReference code_cusRef = db.getReference("DiscountCode_Customer");
    DatabaseReference customerRef = db.getReference("Customer");
    DatabaseReference discountcodeRef = db.getReference("DiscountCode");
    DatabaseReference areaRef = db.getReference("Area");
    String address = "", accountID = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_payment);

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

        //get control
        edtAddress = findViewById(R.id.editTextAddress);
        edtName = findViewById(R.id.editTextName);
        edtPhone = findViewById(R.id.editTextPhone);
        productRecyclerView = findViewById(R.id.listProduct);
        edtDiscountCode = findViewById(R.id.editTextDiscountCode);
        edtNote = findViewById(R.id.editTextMessage);
        txtTotal = findViewById(R.id.txt_tongtien);
        txtRemain = findViewById(R.id.txt_conlai);
        txtTransportFee = findViewById(R.id.txt_phivanchuyen);
        txtDiscount = findViewById(R.id.txt_dathanhtoan);
        btnSubmit = findViewById(R.id.buttonTTXacNhan);
        btnMap = findViewById(R.id.btnMap);
        listCart = new ArrayList<>();
        cartAdapter = new CartDetailTTAdapter(this, listCart);

        if (getIntent().getStringExtra("address") != null) {
            address = getIntent().getStringExtra("address");
            edtAddress.setText(address);
        }

        // Gọi hàm lấy dữ liệu:
        data();

        // Recyclerview hiển thị dữ liệu:
        productRecyclerView.setAdapter(cartAdapter);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Xử lý sự kiện cho focus edtDiscountCode và edtAddress:
        edtDiscountCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (String.valueOf(edtAddress.getText()).equals("")) {
                    showWarningDialog("Vui lòng chọn địa chỉ giao hàng trước");
                    edtAddress.requestFocus();
                }
            } else {
                customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot node : snapshot.getChildren()) {
                            Customer customer = node.getValue(Customer.class);
                            customer.setKey(node.getKey());
                            if (customer.getAccountID().equals(accountID)) {
                                code_cusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        for (DataSnapshot node1 : snapshot1.getChildren()) {
                                            DiscountCode_Customer temp = node1.getValue(DiscountCode_Customer.class);
                                            if (temp.getCustomer_id().equals(customer.getKey())) {
                                                discountcodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot2) {
                                                        boolean check = false;
                                                        for (DataSnapshot node2 : snapshot2.getChildren()) {
                                                            DiscountCode code = node2.getValue(DiscountCode.class);
                                                            if (code.getCode().equals(String.valueOf(edtDiscountCode.getText()))) {
                                                                check = true;
                                                                if (code.getType().equals("%")) {

                                                                    int value = code.getValue();
                                                                    int totalPrice = toPrice(String.valueOf(txtTotal.getText()));
                                                                    int discount = totalPrice / 100 * value;
                                                                    int transportFee = toPrice(String.valueOf(txtTransportFee.getText()));
                                                                    txtDiscount.setText(formatPrice(discount));
                                                                    txtRemain.setText(formatPrice(totalPrice + transportFee - discount));
                                                                } else if (code.getType().equals("VND")) {
                                                                    int value = code.getValue();
                                                                    int totalPrice = toPrice(String.valueOf(txtTotal.getText()));
                                                                    int discount = value;
                                                                    int transportFee = toPrice(String.valueOf(txtTransportFee.getText()));
                                                                    txtDiscount.setText(formatPrice(discount));
                                                                    txtRemain.setText(formatPrice(totalPrice + transportFee - discount));
                                                                } else if (code.getType().equals("Free ship")) {
                                                                    int transportFee = toPrice(String.valueOf(txtTransportFee.getText()));
                                                                    txtDiscount.setText(formatPrice(transportFee));
                                                                    int totalPrice = toPrice(String.valueOf(txtTotal.getText()));
                                                                    int discount = toPrice(String.valueOf(txtDiscount.getText()));
                                                                    txtRemain.setText(formatPrice(totalPrice + transportFee - discount));

                                                                }

                                                            }
                                                        }
                                                        if (!check) {
                                                            txtDiscount.setText(formatPrice(0));
                                                            int total = toPrice(String.valueOf(txtTotal.getText()));
                                                            int transportFee = toPrice(String.valueOf(txtTransportFee.getText()));
                                                            txtRemain.setText(formatPrice(total + transportFee));
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
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        edtAddress.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!String.valueOf(edtAddress.getText()).equals("")) {
                    String address = String.valueOf(edtAddress.getText());
                    String[] split = address.split(",");
                    if (split.length > 3) {
                        String city = split[split.length - 2];
                        if (city.trim().equals("Thành phố Hồ Chí Minh")) {
                            String quan = split[split.length - 3];
                            areaRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot node : snapshot.getChildren()) {
                                        Area area = node.getValue(Area.class);
                                        if (area.getArea().toLowerCase().trim().contains(quan.toLowerCase().trim())) {
                                            txtTransportFee.setText(formatPrice(area.getTransport_fee()));
                                            int total = toPrice(String.valueOf(txtTotal.getText()));
                                            int discount = toPrice(String.valueOf(txtDiscount.getText()));
                                            txtRemain.setText(formatPrice(total + area.getTransport_fee() - discount));
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }

                            });
                        } else {
                            txtTransportFee.setText(formatPrice(60000));
                            int total = toPrice(String.valueOf(txtTotal.getText()));
                            int discount = toPrice(String.valueOf(txtDiscount.getText()));
                            txtRemain.setText(formatPrice(total + 60000 - discount));
                        }
                    } else {
                        showWarningDialog("Vui lòng nhập địa chỉ chi tiết hơn");
                        edtAddress.setText("");

                    }
                }
            }
        });

        // Xử lý sự kiện click mở map cho btnMap:
        btnMap.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentActivity.this, MapActivity.class);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
            finish();
        });

        // Xử lý sự kiện xác nhận đặt hàng:
        btnSubmit.setOnClickListener(v -> {
            DatabaseReference orderRef = db.getReference("Order");
            DatabaseReference orderdetailRef = db.getReference("Order_Details");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
            Date now = new Date();
            String key = "DH" + sdf.format(now).replace("/", "").replace(":", "").replace(" ", "");
            if (checkError() == 1) {
                Order order = new Order();
                order.setAccountID(accountID);
                order.setAddress(String.valueOf(edtAddress.getText()));
                order.setCreated_at(sdf.format(now));
                order.setName(String.valueOf(edtName.getText()));
                order.setNote(String.valueOf(edtNote.getText()));
                order.setPhone(String.valueOf(edtPhone.getText()));
                order.setShipperID("null");
                order.setStatus(1);
                order.setTotal(toPrice(String.valueOf(txtRemain.getText())));
                orderRef.child(key).setValue(order).addOnSuccessListener(unused -> {
                    order.setOrderID(key);
                    showSuccesDialog("Đặt hàng thành công!\nCảm ơn bạn đã sử dụng dịch vụ của chúng tôi.", order);
                });
                for (CartDetail detail : listCart) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrderID(key);
                    orderDetail.setAmount(detail.getAmount());
                    orderDetail.setPrice(detail.getPrice());
                    orderDetail.setProductID(detail.getProductID());
                    orderdetailRef.push().setValue(orderDetail);
                }
                //Clear Cart
                cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot node : snapshot.getChildren()) {
                            Cart cart = node.getValue(Cart.class);
                            if (cart.getAccountID().equals(accountID)) {
                                cart.setTotal(0);
                                cartRef.child(node.getKey()).setValue(cart);
                                detailRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                        for (DataSnapshot node1 : snapshot1.getChildren()) {
                                            CartDetail cartDetail = node1.getValue(CartDetail.class);
                                            if (cartDetail.getCartID().equals(node.getKey())) {
                                                detailRef.child(node1.getKey()).removeValue();
                                            }
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
                //Delete Discount code
                if (!String.valueOf(txtDiscount.getText()).equals("")) {
                    customerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot node : snapshot.getChildren()) {
                                Customer customer = node.getValue(Customer.class);
                                if (customer.getAccountID().equals(accountID)) {
                                    code_cusRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                            for (DataSnapshot node1 : snapshot1.getChildren()) {
                                                DiscountCode_Customer temp = node1.getValue(DiscountCode_Customer.class);
                                                if (temp.getCustomer_id().equals(node.getKey()) && temp.getCode().equals(edtDiscountCode.getText() + "")) {
                                                    code_cusRef.child(node1.getKey()).removeValue();
                                                    break;
                                                }
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
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Get data:
    private void data() {
        txtTotal.setText(formatPrice(0));
        txtDiscount.setText(formatPrice(0));
        txtTransportFee.setText(formatPrice(0));
        txtRemain.setText(formatPrice(0));
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    Cart cart = node.getValue(Cart.class);
                    if (cart.getAccountID().equals(accountID)) {
                        detailRef.addValueEventListener(new ValueEventListener() {
                            @SuppressLint("NotifyDataSetChanged")
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                listCart.clear();
                                total = 0;
                                for (DataSnapshot node1 : snapshot1.getChildren()) {
                                    CartDetail detail = node1.getValue(CartDetail.class);
                                    if (detail.getCartID().equals(node.getKey()) && detail.getAmount() > 0) {
                                        listCart.add(detail);
                                        total += detail.getPrice() * detail.getAmount();
                                    }
                                }
                                txtTotal.setText(formatPrice(total));
                                int discount = toPrice(String.valueOf(txtDiscount.getText()));
                                int transportFee = toPrice(String.valueOf(txtTransportFee.getText()));
                                txtRemain.setText(formatPrice(total + transportFee - discount));
                                cartAdapter.notifyDataSetChanged();
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
        txtDiscount.setText(formatPrice(0));
        txtTransportFee.setText(formatPrice(0));
        txtRemain.setText(formatPrice(total));
    }

    // Kiểm tra lỗi
    private int checkError() {
        if (String.valueOf(edtAddress.getText()).equals("")) {
            showWarningDialog("Địa chỉ không được để trống");
            return -1;
        }
        if (String.valueOf(edtName.getText()).equals("")) {

            showWarningDialog("Tên người nhận không được để trống");
            return -1;
        }
        if (String.valueOf(edtPhone.getText()).equals("")) {

            showWarningDialog("Số điện thoại không được để trống");
            return -1;
        }
        return 1;
    }

    // Format tiền
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

    // Chuyển tiền sang dạng int
    private int toPrice(String price) {
        price = price.substring(0, price.length() - 2).replace(",", "");

        int totalPrice = Integer.parseInt(price);
        return totalPrice;
    }

    // Các hàm thông báo:
    private void showSuccesDialog(String notify, Order item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(PaymentActivity.this).inflate(
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

        view.findViewById(R.id.buttonAction).setOnClickListener(v -> {
            alertDialog.dismiss();
            startActivity(new Intent(PaymentActivity.this, DetailHistoryOrderActivity.class)
                    .putExtra("item", item)
                    .putExtra("from", "payment")
                    .putExtra("accountID", accountID));
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }
    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(PaymentActivity.this).inflate(
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
}
