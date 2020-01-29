package bd.com.dev.pocketcash.activity;

import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bd.com.dev.pocketcash.R;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText otp1Et, otp2Et, otp3Et, otp4Et, otp5Et, otp6Et;
    private TextView showNumberTV;

    private String mNumber, userType, phoneNumberVerificationId, userId;
    private String otp1, otp2, otp3, otp4, otp5, otp6;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private Button continueBtn;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        if (getIntent() != null) {
            mNumber = getIntent().getStringExtra("mNumber");
            userType = getIntent().getStringExtra("user");
        } else {
            onBackPressed();
        }

        init();
        onClick();
        sendVerificationCode(mNumber);

    }

    private void init() {

        showNumberTV = findViewById(R.id.showNumberTVID);
        progressBar = findViewById(R.id.progressBarId);
        continueBtn = findViewById(R.id.continueBtn);
        showNumberTV.setText(mNumber);
        otp1Et = findViewById(R.id.otp1EtID);
        otp2Et = findViewById(R.id.otp2EtID);
        otp3Et = findViewById(R.id.otp3EtID);
        otp4Et = findViewById(R.id.otp4EtID);
        otp5Et = findViewById(R.id.otp5EtID);
        otp6Et = findViewById(R.id.otp6EtID);
        mAuth = FirebaseAuth.getInstance();
    }

    private void onClick() {

        otp1Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otp1Et.getText().toString().length() >= 1) {
                    otp2Et.requestFocus();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp2Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otp2Et.getText().toString().length() == 1) {
                    otp3Et.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp3Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otp3Et.getText().toString().length() == 1) {
                    otp4Et.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp4Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otp4Et.getText().toString().length() == 1) {
                    otp5Et.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp5Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otp5Et.getText().toString().length() == 1) {
                    otp6Et.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        otp6Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (otp6Et.getText().toString().length() == 1) {

                    otp1 = otp1Et.getText().toString();
                    otp2 = otp2Et.getText().toString();
                    otp3 = otp3Et.getText().toString();
                    otp4 = otp4Et.getText().toString();
                    otp5 = otp5Et.getText().toString();
                    otp6 = otp6Et.getText().toString();

                    String otp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6;
                    if (otp1.equals("")
                            || otp2.equals("")
                            || otp3.equals("")
                            || otp4.equals("")
                            || otp5.equals("")
                            || otp6.equals("")
                            || otp.length() < 6) {
                        Toast.makeText(VerifyOTPActivity.this, "Please insert 6 digits code properly.", Toast.LENGTH_SHORT).show();
                    } else {
                        verifyPhoneCode(otp);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }


    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                this,
                mCallbacks);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {

            String mCode = credential.getSmsCode();
            if (mCode != null) {

                otp1Et.setText(String.valueOf(mCode.charAt(0)));
                otp2Et.setText(String.valueOf(mCode.charAt(1)));
                otp3Et.setText(String.valueOf(mCode.charAt(2)));
                otp4Et.setText(String.valueOf(mCode.charAt(3)));
                otp5Et.setText(String.valueOf(mCode.charAt(4)));
                otp6Et.setText(String.valueOf(mCode.charAt(5)));
                verifyPhoneCode(mCode);
            }


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {

            phoneNumberVerificationId = verificationId;

        }
    };

    private void verifyPhoneCode(String code) {

        continueBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(phoneNumberVerificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            userId = mAuth.getCurrentUser().getUid();

                            if (userType.equals("new_user")) {
                                Map<String,Object> userInfo = new HashMap<>();
                                userInfo.put("userId",userId);
                                userInfo.put("mNumber",mNumber);
                                userInfo.put("createdDate", ServerValue.TIMESTAMP);

                                FirebaseDatabase.getInstance().getReference("users").child(userId).child("userInfo").setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            Map<String,Object> deviceMap = new HashMap<>();
                                            deviceMap.put("deviceId",deviceId);
                                            deviceMap.put("lastActiveDate", ServerValue.TIMESTAMP);
                                            FirebaseDatabase.getInstance().getReference("users").child(userId).child("deviceIds").child(deviceId).setValue(deviceMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Intent intent = new Intent(VerifyOTPActivity.this, UpdateProfileActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        Toast.makeText(VerifyOTPActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        }
                                    }
                                });

                            } else {
                                Map<String,Object> deviceMap = new HashMap<>();
                                deviceMap.put("deviceId",deviceId);
                                deviceMap.put("lastActiveDate", ServerValue.TIMESTAMP);
                                FirebaseDatabase.getInstance().getReference("users").child(userId).child("deviceIds").child(deviceId).setValue(deviceMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("userInfo");
                                            userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.exists()){
                                                        if (dataSnapshot.child("name").exists()){
                                                            Intent intent = new Intent(VerifyOTPActivity.this, HomeActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            Toast.makeText(VerifyOTPActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                                                        }else {
                                                            Intent intent = new Intent(VerifyOTPActivity.this, UpdateProfileActivity.class);
                                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(intent);
                                                            Toast.makeText(VerifyOTPActivity.this, "Welcome back", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    }
                                });

                            }

                        } else {
                            Toast.makeText(VerifyOTPActivity.this, "Invalid code", Toast.LENGTH_SHORT).show();
                            continueBtn.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });
    }


    public void continueNext(View view) {


        otp1 = otp1Et.getText().toString();
        otp2 = otp2Et.getText().toString();
        otp3 = otp3Et.getText().toString();
        otp4 = otp4Et.getText().toString();
        otp5 = otp5Et.getText().toString();
        otp6 = otp6Et.getText().toString();

        String otp = otp1 + otp2 + otp3 + otp4 + otp5 + otp6;
        if (otp.length() < 6) {
            Toast.makeText(this, "Please insert 6 digits code properly.", Toast.LENGTH_SHORT).show();
        } else {
            verifyPhoneCode(otp);
        }

    }

    public void backLoginActivity(View view) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
