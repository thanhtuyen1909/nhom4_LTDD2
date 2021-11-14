package vn.edu.tdc.zuke_customer.activitys;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.RangeSlider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vn.edu.tdc.zuke_customer.CustomBottomNavigationView;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.Product2Adapter;
import vn.edu.tdc.zuke_customer.data_models.Category;
import vn.edu.tdc.zuke_customer.data_models.Manufactures;
import vn.edu.tdc.zuke_customer.data_models.Product;

public class SearchActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    String accountID = "", query = "";
    Toolbar toolbar;
    TextView subtitleAppbar;
    ImageView buttonAction, imgFilter;
    Intent intent;
    DrawerLayout navDrawer;
    SearchView searchView;
    Product2Adapter adapter;
    RecyclerView recyclerView;
    RangeSlider priceRange;
    Spinner spinCate, spinManu;
    RatingBar ratingBar;
    ArrayList<Product> list;
    ArrayList<Category> listCate;
    ArrayList<Manufactures> listManu;
    ArrayAdapter<Category> cateAdapter;
    ArrayAdapter<Manufactures> manuAdapter;
    float minPrice = -1.0f, maxPrice = -1.0f, rating = -1.0f;
    String cate_id = "", manu_id = "";
    Button btnReset, btnApply;
    int iDem = 0;
    TextView txtFilter;
    private CustomBottomNavigationView customBottomNavigationView;
    DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("Products");

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_search);
        UIinit();
        setEvent();
    }

    private void setEvent() {
        imgFilter.setOnClickListener(v -> {
            if (!navDrawer.isDrawerOpen(Gravity.RIGHT)) navDrawer.openDrawer(Gravity.RIGHT);
            else navDrawer.closeDrawer(Gravity.LEFT);
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProduct(newText, minPrice, maxPrice, rating, cate_id, manu_id);
                return false;
            }
        });
        buttonAction.setOnClickListener(v -> {
            intent = new Intent(SearchActivity.this, NotificationActivity.class);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
        });
        customBottomNavigationView.setOnItemSelectedListener(this);
        adapter.setItemClickListener(itemClick1);
        btnApply.setOnClickListener(v -> {

            float minPrice = priceRange.getValues().get(0);
            float maxPrice = priceRange.getValues().get(1);

            query = String.valueOf(searchView.getQuery());
            manu_id = ((Manufactures) spinManu.getSelectedItem()).getKey();
            cate_id = ((Category) spinCate.getSelectedItem()).getKey();
            rating = ratingBar.getRating();
            filterProduct(query, minPrice, maxPrice, rating, cate_id, manu_id);
            navDrawer.closeDrawer(Gravity.RIGHT);
        });
        btnReset.setOnClickListener(v -> {
            minPrice = -1.0f;
            maxPrice = -1.0f;
            manu_id = "";
            cate_id = "";
            rating = -1.0f;
            filterProduct(query, minPrice, maxPrice, rating, cate_id, manu_id);
            List<Float> value = priceRange.getValues();
            value.set(0, 0.0f);
            value.set(1, 0.0f);
            navDrawer.closeDrawer(Gravity.RIGHT);
        });
    }

    private void UIinit() {
        //get data from intent
        intent = getIntent();
        accountID = intent.getStringExtra("accountID");
        if (intent.getStringExtra("query") != null) {
            query = intent.getStringExtra("query");
        }
        if (intent.getStringExtra("key") != null) {
            cate_id = intent.getStringExtra("key");
            filterProduct(query, minPrice, maxPrice, rating, cate_id, manu_id);
        }

        if (!query.equals("")) {
            filterProduct(query, minPrice, maxPrice, rating, cate_id, manu_id);
        }

        //get control
        toolbar = findViewById(R.id.toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        searchView = findViewById(R.id.searchView);
        searchView.setQuery(query, false);
        buttonAction = findViewById(R.id.buttonAction);
        txtFilter = findViewById(R.id.txtFilter);
        customBottomNavigationView = findViewById(R.id.customBottomBar);
        recyclerView = findViewById(R.id.listFilter);
        list = new ArrayList<>();
        listCate = new ArrayList<>();
        listManu = new ArrayList<>();
        spinCate = findViewById(R.id.spinCate);
        spinManu = findViewById(R.id.spinManu);
        cateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listCate);
        cateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCate.setAdapter(cateAdapter);
        manuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, listManu);
        manuAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinManu.setAdapter(manuAdapter);
        imgFilter = findViewById(R.id.imgFilter);
        navDrawer = findViewById(R.id.activity_main_drawer);
        priceRange = findViewById(R.id.priceRange);
        ratingBar = findViewById(R.id.ratingBar);
        btnApply = findViewById(R.id.btnApply);
        btnReset = findViewById(R.id.btnReset);
        // Toolbar:
        setSupportActionBar(toolbar);
        subtitleAppbar.setText(R.string.titleTK);
        buttonAction.setBackground(getResources().getDrawable(R.drawable.ic_round_notifications_24));

        // Bottom navigation:
        customBottomNavigationView.inflateMenu(R.menu.bottom_nav_menu);
        customBottomNavigationView.getMenu().findItem(R.id.mHome).setChecked(false);
        //RecycleView

        recyclerView.setHasFixedSize(true);
        adapter = new Product2Adapter(this, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        Log.d("TAG", "name: ");

        //drawer
        navDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //priceRangeSlider

        setValuePriceRange();
        priceRange.setLabelFormatter(new LabelFormatter() {
            @NonNull
            @Override
            public String getFormattedValue(float value) {
                return formatPrice((int) value);
            }
        });

        ratingBar.setRating(0.0f);
        loadDataCate();
        loadDataManu();
    }

    private void loadDataCate() {
        DatabaseReference cateRef = FirebaseDatabase.getInstance().getReference("Categories");
        cateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listCate.clear();
                listCate.add(new Category("", "Tất cả", ""));
                for (DataSnapshot node : snapshot.getChildren()) {
                    Category cate = node.getValue(Category.class);
                    cate.setKey(node.getKey());
                    listCate.add(cate);
                }
                cateAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        for (int i = 0; i < spinCate.getCount(); i++) {
            Category cate = (Category) spinCate.getItemAtPosition(i);
            if (cate.getKey().equals(cate_id)) {
                spinCate.setSelection(i);
                break;
            }
        }
    }

    private void loadDataManu() {
        DatabaseReference cateRef = FirebaseDatabase.getInstance().getReference("Manufactures");
        cateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listManu.clear();
                listManu.add(new Manufactures("", "Tất cả", ""));
                for (DataSnapshot node : snapshot.getChildren()) {
                    Manufactures manu = node.getValue(Manufactures.class);
                    manu.setKey(node.getKey());
                    listManu.add(manu);
                }
                manuAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void setValuePriceRange() {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int max = 0;
                for (DataSnapshot node : snapshot.getChildren()) {
                    Product product = node.getValue(Product.class);
                    if (max < product.getPrice()) {
                        max = product.getPrice();
                    }
                }
                float maxValue = Float.parseFloat(String.valueOf(max));
                priceRange.setValueFrom(0.0f);
                priceRange.setValueTo(maxValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private final Product2Adapter.ItemClick itemClick1 = new Product2Adapter.ItemClick() {
        @Override
        public void getDetailProduct(Product item) {
            intent = new Intent(SearchActivity.this, DetailProductActivity.class);
            intent.putExtra("item", item);
            intent.putExtra("accountID", accountID);
            startActivity(intent);
        }
    };

    private void filterProduct(String query, float minPrice, float maxPrice, float rating, String cate_id, String manu_id) {
        productRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot node : snapshot.getChildren()) {
                    Product product = node.getValue(Product.class);
                    product.setKey(node.getKey());
                    if(product.getStatus() == 0) {
                        list.add(product);
                    }
                }
                if (!query.equals("")) {
                    for (int i = 0; i < list.size(); i++) {
                        Product product = list.get(i);
                        if (!product.getName().toLowerCase().contains(query.toLowerCase().trim())) {
                            ;
                            list.remove(i);
                            i--;
                        }
                    }
                }

                if (maxPrice != -1.0f && minPrice != -1.0f) {
                    iDem = 0;
                    for (int i = 0; i < list.size(); i++) {
                        iDem = i;
                        Product product = list.get(i);
                        if (product.getPrice() > maxPrice || product.getPrice() < minPrice) {
                            list.remove(iDem);
                            iDem--;
                        }
                    }
                }
                if (rating != -1.0f) {
                    for (int i = 0; i < list.size(); i++) {
                        Product product = list.get(i);
                        if (product.getRating() < rating) {
                            list.remove(i);
                            i--;
                        }
                    }
                }
                if (!cate_id.equals("")) {
                    for (int i = 0; i < list.size(); i++) {
                        Product product = list.get(i);
                        if (!product.getCategory_id().equals(cate_id)) {
                            list.remove(i);
                            i--;
                        }
                    }
                }
                if (!manu_id.equals("")) {
                    for (int i = 0; i < list.size(); i++) {
                        Product product = list.get(i);
                        if (!product.getManu_id().equals(manu_id)) {
                            list.remove(i);
                            i--;
                        }
                    }
                }
                txtFilter.setText("Kết quả : " + list.size() + " sản phẩm");

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // Sự kiện click các item trong bottom navigation
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.mHome:
                intent = new Intent(SearchActivity.this, HomeScreenActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mCategory:
                intent = new Intent(SearchActivity.this, CategoryActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mCart:
                intent = new Intent(SearchActivity.this, CartActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                break;
            case R.id.mProfile:
                intent = new Intent(SearchActivity.this, ProfileScreenActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            case R.id.mFavorite:
                intent = new Intent(SearchActivity.this, FavoriteActivity.class);
                intent.putExtra("accountID", accountID);
                startActivity(intent);
                finish();
                break;
            default:
                Toast.makeText(SearchActivity.this, "Vui lòng chọn chức năng khác", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

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
}
