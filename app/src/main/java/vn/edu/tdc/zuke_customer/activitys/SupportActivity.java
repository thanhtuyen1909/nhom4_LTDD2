package vn.edu.tdc.zuke_customer.activitys;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.adapters.SupportAdapter;
import vn.edu.tdc.zuke_customer.data_models.TheCustomerConslutant;

public class SupportActivity extends AppCompatActivity {
    // Khai báo biến:
    Toolbar toolbar;
    TextView subtitleAppbar;
    ListView lvQuestion;
    SupportAdapter conslutantAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_support);

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleHT);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setControl();
        addData();
        lvQuestion.setOnItemClickListener((parent, view, position, id) -> {
            TheCustomerConslutant conslutant = conslutantAdapter.getItem(position);
            AlertDialog.Builder builder = new AlertDialog.Builder(SupportActivity.this);
            builder.setTitle("Trả lời");
            builder.setMessage(conslutant.getAnswer());
            builder.setPositiveButton("Đã hiểu", (dialog, id1) -> dialog.dismiss());
            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void addData() {
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question1), getString(R.string.answer1)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question2), getString(R.string.answer2)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question3), getString(R.string.answer3)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question4), getString(R.string.answer4)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question5), getString(R.string.answer5)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question6), getString(R.string.answer6)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question7), getString(R.string.answer7)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question8), getString(R.string.answer8)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question9), getString(R.string.answer9)));
        conslutantAdapter.add(new TheCustomerConslutant(getString(R.string.question10), getString(R.string.answer10)));
    }

    private void setControl() {
        lvQuestion = findViewById(R.id.lvQuestion);
        conslutantAdapter = new SupportAdapter(this, R.layout.item_the_customer_conslutant);
        lvQuestion.setAdapter(conslutantAdapter);
    }
}
