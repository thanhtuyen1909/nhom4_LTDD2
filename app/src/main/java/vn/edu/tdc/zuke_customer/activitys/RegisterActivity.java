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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.TimeUnit;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Account;

public class RegisterActivity extends AppCompatActivity {
    EditText edtName, edtPhone, edtPassword, edtConfirmPass;
    CircularProgressButton btnRegis;
    FirebaseAuth mAuth;
    boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_register);
        changeStatusBarColor();
        UIinit();
        btnRegis.setOnClickListener(v -> {
            checkPhone();
            if (checkError() == 1) {
                if (check == true) {
                    Account account = new Account();
                    account.setUsername(edtPhone.getText().toString());
                    account.setPassword(edtPassword.getText().toString());
                    account.setRole_id(1);
                    account.setStatus("unlock");
                    sendOTPCode(account, edtName.getText().toString());
                } else {
                    showWarningDialog("Số điện thoại đã được đăng ký");

                }
            }
        });
    }

    private void sendOTPCode(Account account, String name) {
        String phoneNumber = String.valueOf(edtPhone.getText());
        phoneNumber = "+84".concat(phoneNumber.substring(1, phoneNumber.length()));
        Log.d("TAG", "sendOTPCode: " + phoneNumber);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(RegisterActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        Log.d("TAG", " completed");
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                        moveHomeScreen();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.d("TAG", " " + e.getMessage());
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        Log.d("TAG", "onCodeSent: ");
                        moveOPTActivity(account, s, name);

                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
        Log.d("TAG", "sendOTPCode:");
    }

    private void UIinit() {
        btnRegis = findViewById(R.id.cirRegisterButton);
        edtName = findViewById(R.id.editTextName);
        edtPhone = findViewById(R.id.editTextPhone);
        edtPassword = findViewById(R.id.editTextPassword);
        edtConfirmPass = findViewById(R.id.editTextConfirmPassword);
        mAuth = FirebaseAuth.getInstance();
    }

    private void moveOPTActivity(Account account, String verification_id, String name) {
        Intent intent = new Intent(RegisterActivity.this, OTPVerificationActivity.class);
        intent.putExtra("phone_number", account.getUsername());
        intent.putExtra("verification_id", verification_id);
        intent.putExtra("type", "regis");
        intent.putExtra("account", account);
        intent.putExtra("name", name);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
    }

    private int checkError() {
        if (String.valueOf(edtName.getText()).equals("")) {
            showWarningDialog("Họ và tên không được để trống");
            return -1;
        }
        if (String.valueOf(edtPhone.getText()).equals("")) {
            showWarningDialog("Số điện thoại không được để trống");
            return -1;
        }
        if (String.valueOf(edtPassword.getText()).equals("")) {
            showWarningDialog("Mật khẩu không được để trống");
            return -1;
        }

        if (!String.valueOf(edtConfirmPass.getText()).equals(String.valueOf(edtPassword.getText()))) {
            showWarningDialog("Mật khẩu xác nhận không trùng khớp");
            return -1;
        }
        return 1;

    }

    private void checkPhone() {
        DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("Account");
        accountRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot node : snapshot.getChildren()) {
                    Account account = node.getValue(Account.class);
                    if (account.getUsername().equals(String.valueOf(edtPhone.getText()))) {
                        check = false;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = task.getResult().getUser();
                        // Update UI
                        moveHomeScreen();
                    } else {
                        // Sign in failed, display a message and update the UI
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            //Thông báo OTP sai
                        }
                    }
                });
    }

    private void moveHomeScreen() {
        startActivity(new Intent(RegisterActivity.this, HomeScreenActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(RegisterActivity.this).inflate(
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

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onLogin(View view) {
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }
}
