package vn.edu.tdc.zuke_customer.activitys;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import vn.edu.tdc.zuke_customer.R;
import vn.edu.tdc.zuke_customer.data_models.Account;
import vn.edu.tdc.zuke_customer.data_models.Customer;

public class FacebookAuthActivity extends LoginActivity {
    CallbackManager callbackManager;
    FirebaseAuth mAuth;
    boolean check = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_facebook_auth);
        mAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleFacebookAccessToken(AccessToken token) {

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("TAG", "onComplete:  " + user.getUid());
                            Log.d("TAG", "onComplete:  " + user.getPhoneNumber());
                            Log.d("TAG", "onComplete:  " + user.getDisplayName());
                            DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("Account");
                            accountRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot node : snapshot.getChildren()) {
                                        if (node.getKey().equals(user.getUid())) {
                                            check = false;
                                            if (node.getValue(Account.class).getStatus().equals("unlock")) {
                                                updateUI(user);
                                            } else {
                                                showWarningDialog("Tài khoản của bạn đã bị khoá, vui lòng liên hệ: 0123 456 789 để được hỗ trợ mở khoá tài khoản !!!");
                                            }
                                        }
                                    }
                                    if (check) {
                                        Account account = new Account();
                                        if (user.getPhoneNumber() != null) {
                                            account.setUsername(user.getPhoneNumber());
                                        } else {
                                            account.setUsername("");
                                        }

                                        account.setStatus("unlock");
                                        account.setPassword("");
                                        account.setRole_id(1);
                                        accountRef.child(user.getUid()).setValue(account.toMap());

                                        //tạo 1 customer
                                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                                        Customer customer = new Customer();
                                        customer.setCreated_at(sdf.format(new Date()));
                                        customer.setImage(user.getPhotoUrl().toString());
                                        customer.setStatus("green");
                                        customer.setName(user.getDisplayName());
                                        customer.setAccountID(user.getUid());
                                        customer.setDob("");
                                        customer.setEmail("");
                                        customer.setType_id("Type");
                                        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference("Customer");
                                        customerRef.push().setValue(customer);
                                        updateUI(user);
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });


                        } else {

                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(FacebookAuthActivity.this, HomeScreenActivity.class);
        intent.putExtra("accountID", user.getUid());
        startActivity(intent);
        finish();
    }

    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(FacebookAuthActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(FacebookAuthActivity.this).inflate(
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
            startActivity(new Intent(FacebookAuthActivity.this, LoginActivity.class));
            finish();
        });


        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }
}
