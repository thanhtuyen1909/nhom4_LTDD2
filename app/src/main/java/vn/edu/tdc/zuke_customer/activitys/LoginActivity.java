package vn.edu.tdc.zuke_customer.activitys;


import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Account;

public class LoginActivity extends AppCompatActivity {
    private ImageView fbButton;
    private EditText edtPhone, edtPass;
    CircularProgressButton btnLogin;
    private final FirebaseDatabase db = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = db.getReference("Account");
    boolean checkPhone = false,checkPass = false,checkLock = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //for changing status bar icon colors
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.layout_login);
        fbButton = findViewById(R.id.btnFB);
        edtPhone = findViewById(R.id.editTextPhone);
        edtPass = findViewById(R.id.editTextPassword);
        btnLogin = findViewById(R.id.cirLoginButton);

        fbButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, FacebookAuthActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public int checkError() {
        if (String.valueOf(edtPhone.getText()).equals("")) {
            //Thông báo "Số điện thoại không được để trống"
            return -1;
        }
        if (String.valueOf(edtPass.getText()).equals("")) {
            //Thông báo "Mật khẩu không được để trống"
            return -1;
        }
        return 1;
    }

    public void onRegister(View View) {
        startActivity(new Intent(this, RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        finish();
    }

    public void onForgotPass(View View) {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        finish();
    }

    public void onSubmit(View View) {
        String phone = String.valueOf(edtPhone.getText());
        String pass = String.valueOf(edtPass.getText());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    Account account = node.getValue(Account.class);
                    if (account.getUsername().equals(phone)) {
                        checkPhone = true;
                        if (account.getPassword().equals(pass)) {
                            checkPass = true;
                            if (account.getStatus().equals("unlock")) {
                                checkLock = true;
                                Intent itent = new Intent(LoginActivity.this, HomeScreenActivity.class);
                                itent.putExtra("accountID", node.getKey());
                                startActivity(itent);
                                finish();
                            }
                        }
                    }
                }
                if(!checkPhone){
                    showWarningDialog("Số điện thoại không chính xác");
                }else  if(!checkPass){
                    showWarningDialog("Mật khẩu không chính xác");
                }else  if(!checkLock){
                    showWarningDialog("Tài khoản của bạn đã bị khoá");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(LoginActivity.this).inflate(
                R.layout.layout_warning_dialog,
                findViewById(R.id.layoutDialogContainer)
        );
        builder.setView(view);
        TextView title = view.findViewById(R.id.textTitle);
        title.setText(R.string.title);
        TextView mess = view.findViewById(R.id.textMessage);
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
