package vn.edu.tdc.zuke_customer.activitys;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Account;
import vn.edu.tdc.zuke_customer.data_models.Order;

public class ForgotPasswordChangeActivity extends AppCompatActivity {
    EditText edtPass,edtPassConfirm;
    CircularProgressButton btnSubmit;
    String phoneNumber = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_forgot_password_change);
        changeStatusBarColor();
        UIinit();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: "+checkError());
                if(checkError() == 1){
                    DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("Account");
                    accountRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for(DataSnapshot node : snapshot.getChildren()){
                                Account account = node.getValue(Account.class);
                                if(account.getUsername().equals(String.valueOf(phoneNumber))){
                                    account.setPassword(String.valueOf(edtPass.getText()));
                                    accountRef.child(node.getKey()).setValue(account.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            showSuccesDialog("Cập nhật mật khẩu thành công");
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

    private void UIinit() {
        edtPass = findViewById(R.id.editTextPassword);
        edtPassConfirm = findViewById(R.id.editTextConfirmPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        phoneNumber = getIntent().getStringExtra("phone_number");
        phoneNumber = phoneNumber.replace("+84","0");
    }
    private int  checkError(){
        if(String.valueOf(edtPass.getText()).equals("")){

            showWarningDialog("Mật khẩu không được để trống");
            return -1;
        }

        if(!String.valueOf(edtPass.getText()).equals(String.valueOf(edtPassConfirm.getText()))){

            showWarningDialog("Mật khẩu xác nhận không trùng khớp");
            return -1;
        }
        return 1;
    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onBack(View view){
        startActivity(new Intent(this, OTPVerificationActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }
    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordChangeActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ForgotPasswordChangeActivity.this).inflate(
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
    private void showSuccesDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ForgotPasswordChangeActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ForgotPasswordChangeActivity.this).inflate(
                R.layout.layout_succes_dialog,
                findViewById(R.id.layoutDialogContainer)
        );
        builder.setView(view);
        TextView title = view.findViewById(R.id.textTitle);
        title.setText(R.string.title);
        TextView mess = view.findViewById(R.id.textMessage);
        mess.setText(notify);
        ((TextView) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.okay));

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setOnClickListener(v -> {
            alertDialog.dismiss();
            startActivity(new Intent(ForgotPasswordChangeActivity.this, LoginActivity.class));
        });

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }
}
