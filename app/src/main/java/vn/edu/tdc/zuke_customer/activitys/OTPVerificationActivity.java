package vn.edu.tdc.zuke_customer.activitys;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Account;
import vn.edu.tdc.zuke_customer.data_models.Customer;

public class OTPVerificationActivity extends AppCompatActivity {
    CircularProgressButton btnSubmit;
    EditText edt1, edt2, edt3, edt4, edt5, edt6;
    TextView btnSentAgain;
    String phoneNumber, verification_id, type, name;
    FirebaseAuth mAuth;
    PhoneAuthProvider.ForceResendingToken token;
    Account account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_otp_confirm);
        changeStatusBarColor();

        UIinit();
        btnSentAgain.setOnClickListener(v -> onClickSentOTPAgain());
        btnSubmit.setOnClickListener(v -> {
            String otp = String.valueOf(edt1.getText()).concat(String.valueOf(edt2.getText()).concat(String.valueOf(edt3.getText()).concat(String.valueOf(edt4.getText()).concat(String.valueOf(edt5.getText()).concat(String.valueOf(edt6.getText()))))));
            onClickSubmit(otp);
        });
        edt1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    edt2.requestFocus();
                }
            }
        });
        edt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    edt3.requestFocus();
                }
            }
        });
        edt3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    edt4.requestFocus();
                }
            }
        });
        edt4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    edt5.requestFocus();
                }
            }
        });
        edt5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    edt6.requestFocus();
                }
            }
        });
    }

    private void UIinit() {
        btnSentAgain = findViewById(R.id.btnSentAgain);
        btnSubmit = findViewById(R.id.btnSubmit);
        edt1 = findViewById(R.id.edt1);
        edt2 = findViewById(R.id.edt2);
        edt3 = findViewById(R.id.edt3);
        edt4 = findViewById(R.id.edt4);
        edt5 = findViewById(R.id.edt5);
        edt6 = findViewById(R.id.edt6);
        edt1.requestFocus();
        mAuth = FirebaseAuth.getInstance();
        phoneNumber = getIntent().getStringExtra("phone_number");
        verification_id = getIntent().getStringExtra("verification_id");
        type = getIntent().getStringExtra("type");
        if (type.equals("regis")) {
            account = getIntent().getParcelableExtra("account");
            name = getIntent().getStringExtra("name");
        }
    }

    private void onClickSentOTPAgain() {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPVerificationActivity.this)
                .setForceResendingToken(token)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verification_id = s;
                        token = forceResendingToken;
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void moveHomeScreen(String accountID) {
        Intent intent = new Intent(OTPVerificationActivity.this, HomeScreenActivity.class);
        intent.putExtra("accountID", accountID);
        startActivity(intent);
        finish();
    }

    private void onClickSubmit(String otp) {
        if (otp.length() != 6) {
            showWarningDialog("mã OTP không hợp lệ");
        } else {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification_id, otp);
            signInWithPhoneAuthCredential(credential);
        }

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                            if (type.equals("regis")) {
                                //lưu tài khoản vào database
                                DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("Account");
                                DatabaseReference childRef = accountRef.push();
                                childRef.setValue(account.toMap());
                                childRef.getKey();
                                //tạo 1 customer
                                Customer customer = new Customer();
                                customer.setCreated_at("");
                                customer.setImage("");
                                customer.setStatus("green");
                                customer.setName(name);
                                customer.setAccountID(childRef.getKey());
                                customer.setDob("");
                                customer.setEmail("");
                                customer.setType_id("Type");
                                DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer");
                                customerRef.push().setValue(customer);
                                //back to home
                                moveHomeScreen(childRef.getKey());
                            }
                            if (type.equals("forgot")) {
                                moveChangePasswordScreen();
                            }
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                showWarningDialog("mã OTP không hợp lệ");
                            }
                        }
                    }
                });
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }

    public void onBack(View view) {
        startActivity(new Intent(this, ForgotPasswordActivity.class));
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        finish();
    }

    private void moveChangePasswordScreen() {
        Intent intent = new Intent(OTPVerificationActivity.this, ForgotPasswordChangeActivity.class);
        intent.putExtra("phone_number", phoneNumber);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.stay);
        finish();
    }

    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(OTPVerificationActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(OTPVerificationActivity.this).inflate(
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
