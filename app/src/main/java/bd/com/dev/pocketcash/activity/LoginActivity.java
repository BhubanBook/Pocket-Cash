package bd.com.dev.pocketcash.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth firebaseAuth;
    private boolean isMatch = false;
    private Dialog dialog;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        init();

        if (firebaseAuth.getCurrentUser()!=null){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }

        binding.termsAndConditionCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.nextBtn.setVisibility(View.VISIBLE);
                } else {
                    binding.nextBtn.setVisibility(View.GONE);
                }
            }
        });

    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void next(View view) {
        String mNumber = binding.phoneNumberEtID.getText().toString();
        if (!mNumber.isEmpty() && mNumber.matches("01[0-9]{9}")) {
            checkUser("+88"+mNumber);
        } else {
            binding.phoneNumberEtID.setError("Enter your valid phone number");
            binding.phoneNumberEtID.requestFocus();
            binding.progressBarId.setVisibility(View.GONE);
            binding.nextBtn.setVisibility(View.VISIBLE);
        }
    }

    private void checkUser(final String mNumber) {

        binding.progressBarId.setVisibility(View.VISIBLE);
        binding.nextBtn.setVisibility(View.GONE);

        isMatch = false;
        final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference("users");

        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {

                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        if (data.child("userInfo").exists()){
                            userId = "";
                            String number = data.child("userInfo").child("mNumber").getValue().toString();
                            if (number.equals(mNumber)){
                                isMatch = true;
                                userId = data.child("userInfo").child("userId").getValue().toString();
                                break;
                            }
                        }
                    }
                    if (isMatch) {
                        userDB.child(userId).child("deviceIds").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()){
                                    int counter = 0;
                                    for (DataSnapshot data: dataSnapshot.getChildren()){
                                        counter++;
                                    }

                                    if (counter<3){
                                        startActivity(new Intent(LoginActivity.this, VerifyOTPActivity.class)
                                                .putExtra("mNumber", mNumber)
                                                .putExtra("user", "old_user"));
                                    }else {
                                        createDialog("You already try three device. You can't able to use more then three device. Thank you.");
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    } else {
                        startActivity(new Intent(LoginActivity.this, VerifyOTPActivity.class)
                                .putExtra("mNumber", mNumber)
                                .putExtra("user", "new_user"));
                    }

                    binding.progressBarId.setVisibility(View.GONE);
                    binding.nextBtn.setVisibility(View.VISIBLE);
                } else {
                    startActivity(new Intent(LoginActivity.this, VerifyOTPActivity.class)
                            .putExtra("mNumber", mNumber)
                            .putExtra("user", "new_user"));
                    binding.progressBarId.setVisibility(View.GONE);
                    binding.nextBtn.setVisibility(View.VISIBLE);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public Dialog createDialog(String message) {

        dialog = new ProgressDialog(this);
        try {
            dialog.show();
        } catch (WindowManager.BadTokenException e) {

        }
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.background_dialog));
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setLayout(width, height);
        dialog.setContentView(R.layout.dialog_suggestion);

        TextView messageTV = dialog.findViewById(R.id.messageTV);
        messageTV.setText(message);
        TextView okBtnTV = dialog.findViewById(R.id.okBtn);
        okBtnTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //dialog.setMessage("Please wait..");
        return dialog;
    }
}
