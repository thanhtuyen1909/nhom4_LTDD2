package vn.edu.tdc.zuke_customer.activitys;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

import vn.edu.tdc.zuke_customer.R;

public class ChangePasswordActivity extends AppCompatActivity {
    Toolbar toolbar;
    TextView subtitleAppbar;
    String accountID = "abc05684428156";
    Intent intent;
    TextView title, mess;
    Button btnSubmit;
    Handler handler = new Handler();
    TextInputEditText editTextPasswordConfirm, editTextPassword, editTextNewPassword;
    DatabaseReference accountRef = FirebaseDatabase.getInstance().getReference("Account/" + accountID);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_change_password);

        intent = getIntent();
        accountID = intent.getStringExtra("accountID");

        // Toolbar:
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subtitleAppbar = findViewById(R.id.subtitleAppbar);
        subtitleAppbar.setText(R.string.titleTDMK);
        assert getSupportActionBar() != null;   //null check
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Khởi tạo biển:
        btnSubmit = findViewById(R.id.buttonChangePass);
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);

        // Xử lý sự kiện click btnSubmit:
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accountRef.child("password").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (checkError() == 1) {
                            // Nếu đúng thì thay đổi dữ liệu + thông báo success -> đóng dialog
                            if (snapshot.getValue(String.class).equals(editTextPassword.getText() + "")) {
                                accountRef.child("password").setValue(editTextPassword.getText() + "").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        showSuccesDialog();
                                    }
                                });
                            }
                            // Nếu sai thì thông báo warning
                            else {
                                showWarningDialog("Vui lòng kiểm tra lại mật khẩu!");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Hàm kiểm tra lỗi
    private int checkError() {
        if (editTextPassword.getText().equals("")) {
            showWarningDialog("Mật khẩu hiện tại không được để trống!");
            return -1;
        } else if (editTextNewPassword.getText().equals("")) {
            showWarningDialog("Mật khẩu mới không được để trống!");
            return -1;
        } else if (!(editTextNewPassword.getText().toString().equals(editTextPasswordConfirm.getText().toString()))) {
            showWarningDialog("Mật khẩu mới và xác nhận mật khẩu là không trùng khớp!");
            return -1;
        }
        return 1;
    }

    private void showSuccesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ChangePasswordActivity.this).inflate(
                R.layout.layout_succes_dialog,
                findViewById(R.id.layoutDialogContainer)
        );
        builder.setView(view);
        title = view.findViewById(R.id.textTitle);
        title.setText(R.string.title);
        mess = view.findViewById(R.id.textMessage);
        mess.setText("Đổi mật khẩu thành công!");
        ((TextView) view.findViewById(R.id.buttonAction)).setText(getResources().getString(R.string.okay));

        final AlertDialog alertDialog = builder.create();

        view.findViewById(R.id.buttonAction).setVisibility(View.GONE);

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();

        handler.postDelayed(alertDialog::dismiss, 1500);
    }

    private void showWarningDialog(String notify) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChangePasswordActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from(ChangePasswordActivity.this).inflate(
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
