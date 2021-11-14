package vn.edu.tdc.zuke_customer.activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.CommentAdapter;
import vn.edu.tdc.zuke_customer.adapters.ProductAdapter;
import vn.edu.tdc.zuke_customer.data_models.Cart;
import vn.edu.tdc.zuke_customer.data_models.CartDetail;
import vn.edu.tdc.zuke_customer.data_models.Favorite;
import vn.edu.tdc.zuke_customer.data_models.OfferDetail;
import vn.edu.tdc.zuke_customer.data_models.Product;
import vn.edu.tdc.zuke_customer.data_models.Rating;

public class DetailProductActivity extends AppCompatActivity implements View.OnClickListener {
    // Khai báo biến:
    String accountID = "", cartID = "";
    Handler handler = new Handler();
    boolean check = true;
    Toolbar toolbar;
    Product item = null;
    Intent intent;
    TextView subtitleAppbar, price, sold, price_main, name, description, rating, detail, title, mess;
    ImageView imgProduct, addCart, hotline;
    ToggleButton button_favorite;
    Button btnMuaNgay;
    RatingBar simpleRatingBar;
    RecyclerView relateProduct, rcvComment;
    ConstraintLayout conRating;
    ProductAdapter productRelate;
    ArrayList<Product> listRelate;
    ArrayList<Rating> listComment;
    CommentAdapter commentAdapter;
    private static final int REQUEST_PHONE_CALL = 1;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference offerDetailRef = db.getReference("Offer_Details");
    DatabaseReference proRef = db.getReference("Products");
    DatabaseReference ratingRef = db.getReference("Rating");
    DatabaseReference favoriteRef = db.getReference("Favorite");
    DatabaseReference cartRef = db.getReference("Cart");
    DatabaseReference cartDetailRef = db.getReference("Cart_Detail");
    Query queryComment, queryRelate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_product);

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleCTSP);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Lấy dữ liệu được gửi sang:
        intent = getIntent();
        item = intent.getParcelableExtra("item");
        accountID = intent.getStringExtra("accountID");

        // Khởi tạo biến:
        imgProduct = findViewById(R.id.imgProduct);
        price = findViewById(R.id.price);
        button_favorite = findViewById(R.id.button_favorite);
        sold = findViewById(R.id.sold);
        price_main = findViewById(R.id.price_main);
        name = findViewById(R.id.name);
        description = findViewById(R.id.description);
        simpleRatingBar = findViewById(R.id.simpleRatingBar);
        rating = findViewById(R.id.rating);
        relateProduct = findViewById(R.id.relateProduct);
        btnMuaNgay = findViewById(R.id.btnMuaNgay);
        addCart = findViewById(R.id.addCart);
        hotline = findViewById(R.id.hotline);
        detail = findViewById(R.id.detail);
        rcvComment = findViewById(R.id.rcvComment);
        conRating = findViewById(R.id.conRating);
        listComment = new ArrayList<>();
        listRelate = new ArrayList<>();
        productRelate = new ProductAdapter(this, listRelate);
        productRelate.setItemClickListener(itemClick);
        commentAdapter = new CommentAdapter(this, listComment);

        // Sự kiện cho click cho các đối tượng:
        btnMuaNgay.setOnClickListener(this);
        button_favorite.setOnClickListener(this);
        detail.setOnClickListener(this);
        hotline.setOnClickListener(this);
        addCart.setOnClickListener(this);

        // Recycleview:
        rcvComment.setHasFixedSize(true);
        relateProduct.setHasFixedSize(true);

        // Đổ dữ liệu vào recyclerView:
        relateProduct.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relateProduct.setAdapter(productRelate);

        rcvComment.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rcvComment.setAdapter(commentAdapter);

        // Kiểm tra nếu nhận được đối tượng tiến hành đổ dữ liệu:
        if (item != null) {
            data();
            // Truyền thông tin product vào các trường
            // Thông tin sản phẩm:
            // Tên:
            name.setText(item.getName());
            // Load ảnh
            StorageReference imageRef = FirebaseStorage.getInstance()
                    .getReference("images/products/" + item.getName() + "/" + item.getImage());
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri).fit().into(imgProduct));

            //Giá
            offerDetailRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int maxSale = 0;
                    for (DataSnapshot node1 : snapshot.getChildren()) {
                        OfferDetail detail = node1.getValue(OfferDetail.class);
                        if (detail.getProductID().equals(item.getKey())) {
                            if (detail.getPercentSale() > maxSale) {
                                maxSale = detail.getPercentSale();
                            }
                        }
                    }
                    if (maxSale != 0) {
                        int discount = item.getPrice() / 100 * (100 - maxSale);
                        price.setText(formatPrice(discount));
                        price_main.setText(formatPrice(item.getPrice()));
                        price_main.setPaintFlags(price_main.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        price.setText(formatPrice(item.getPrice()));
                        price_main.setVisibility(View.GONE);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            //Rating:
            if (item.getRating() > 0) {
                rating.setText(item.getRating() + "/5");
                simpleRatingBar.setRating(item.getRating());
            } else {
                conRating.setVisibility(View.GONE);
            }

            //Mô tả:
            description.setText(item.getDescription());

            //Đã bán:
            if (item.getSold() > 0) {
                sold.setText(item.getSold() + " đã bán");
            } else {
                sold.setVisibility(View.GONE);
            }
            // Kiểm tra trong yêu thích:
            favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot node : snapshot.getChildren()) {
                        if (node.child("userId").getValue(String.class).equals(accountID)
                                && node.child("productId").getValue(String.class).equals(item.getKey())) {
                            button_favorite.setChecked(true);
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

    private final ProductAdapter.ItemClick itemClick = new ProductAdapter.ItemClick() {
        @Override
        public void getDetailProduct(Product item) {
            intent = new Intent(DetailProductActivity.this, DetailProductActivity.class);
            intent.putExtra("item", item);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
            finish();
        }
    };

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHONE_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + "0123456789"));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
                }
                startActivity(intent);
            }
        }
    }

    private void data() {
        // Lấy list comment limit 3:
        queryComment = ratingRef.orderByChild("productID").equalTo(item.getKey()).limitToLast(3);
        queryComment.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listComment.clear();
                for (DataSnapshot node : snapshot.getChildren()) {
                    Rating rating = node.getValue(Rating.class);
                    if (rating.getProductID().equals(item.getKey())) {
                        listComment.add(rating);
                    }
                }
                commentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Lấy list sản phẩm liên quan:
        queryRelate = proRef.orderByChild("category_id").equalTo(item.getCategory_id()).limitToLast(5);
        queryRelate.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listRelate.clear();
                for (DataSnapshot node : snapshot.getChildren()) {
                    Product product = node.getValue(Product.class);
                    product.setKey(node.getKey());
                    if (product.getCategory_id().equals(item.getCategory_id()) &&
                            !product.getKey().equals(item.getKey()) && product.getStatus() == 0) {
                        listRelate.add(product);
                    }
                }
                productRelate.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == btnMuaNgay) {
            addCart();
            startActivity(new Intent(DetailProductActivity.this, PaymentActivity.class)
                    .putExtra("accountID", accountID));
        }
        else if (v == hotline) {
            intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + "0123456789"));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
            }
            startActivity(intent);
        }
        else if (v == addCart) {
            if(item.getQuantity() > 0) {
                addCart();
            }
            else showWarningDialog("Sản phẩm đã hết hàng, vui lòng quay lại sau!");
        }
        else if (v == button_favorite) {
            if (!button_favorite.isChecked()) {
                favoriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot node : snapshot.getChildren()) {
                            if (node.child("userId").getValue(String.class).equals(accountID)
                                    && node.child("productId").getValue(String.class).equals(item.getKey())) {
                                favoriteRef.child(node.getKey()).removeValue().addOnSuccessListener(unused -> showSuccesDialog("Xoá sản phẩm khỏi yêu thích thành công!")).addOnFailureListener(e -> showWarningDialog("Xoá sản phẩm khỏi yêu thích thất bại!"));
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
            else {
                Favorite favorite = new Favorite();
                favorite.setProductId(item.getKey());
                favorite.setUserId(accountID);
                favoriteRef.push().setValue(favorite).addOnSuccessListener(unused -> showSuccesDialog("Thêm sản phẩm vào yêu thích thành công!")).addOnFailureListener(e -> showWarningDialog("Thêm sản phẩm vào yêu thích thất bại!"));
            }
        }
        else {
            // Xem tất cả bình luận
            startActivity(new Intent(DetailProductActivity.this, ListCommentProductActivity.class)
                    .putExtra("productID", item.getKey())
                    .putExtra("accountID", accountID));
        }
    }

    private void addCart() {
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
                                for (DataSnapshot dataSnapshot1 : snapshot1.getChildren()) {
                                    CartDetail cartDetail = dataSnapshot1.getValue(CartDetail.class);
                                    cartDetail.setKey(dataSnapshot1.getKey());
                                    if (cartDetail.getCartID().equals(cartID) && cartDetail.getProductID().equals(item.getKey())) {
                                        check = false;
                                        int amount = cartDetail.getAmount() + 1;
                                        cartDetailRef.child(cartDetail.getKey()).child("amount").setValue(amount);
                                        cartDetailRef.child(cartDetail.getKey()).child("price").setValue(formatInt(price.getText().toString()));
                                        cartRef.child(cartID).child("total").setValue(total + formatInt(price.getText().toString()));
                                        break;
                                    }
                                }
                                if (check) {
                                    CartDetail cartDetail = new CartDetail(cartID, item.getKey(), 1, formatInt(price.getText().toString()));
                                    cartDetailRef.push().setValue(cartDetail);
                                    cartRef.child(cartID).child("total").setValue(total + formatInt(price.getText().toString()));
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
                    cartRef.child(key).child("total").setValue(formatInt(price.getText().toString()));
                    CartDetail cartDetail = new CartDetail(key, item.getKey(), 1, formatInt(price.getText().toString()));
                    cartDetailRef.push().setValue(cartDetail);
                    cartRef.child(key).child("total").setValue(formatInt(price.getText().toString()));
                }

                showSuccesDialog("Thêm vào giỏ hàng thành công!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private int formatInt(String price) {
        return Integer.parseInt(price.substring(0, price.length() - 2).replace(",", ""));
    }

    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailProductActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(DetailProductActivity.this).inflate(
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetailProductActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(DetailProductActivity.this).inflate(
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
