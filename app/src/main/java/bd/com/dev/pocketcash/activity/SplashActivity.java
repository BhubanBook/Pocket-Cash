package bd.com.dev.pocketcash.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import bd.com.dev.pocketcash.R;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private String mNumber;
    private boolean isMatch = false;
    private String userId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        init();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (firebaseAuth.getCurrentUser() == null) {

                    final String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    DatabaseReference userDR = firebaseDatabase.getReference().child("users");
                    userDR.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {

                                for (DataSnapshot data : dataSnapshot.getChildren()) {

                                    isMatch = false;
                                    if (data.child("deviceIds").exists()) {
                                        for (DataSnapshot data1 : data.child("deviceIds").getChildren()) {
                                            if (data1.child("deviceId").getValue().toString().equals(deviceId)) {
                                                isMatch = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (isMatch == true) {
                                        mNumber = data.child("userInfo").child("mNumber").getValue().toString();
                                        break;
                                    }
                                }

                                if (isMatch == true) {
                                    Intent intent = new Intent(SplashActivity.this, VerifyOTPActivity.class);
                                    intent.putExtra("mNumber", mNumber);
                                    intent.putExtra("user", "old_user");
                                    startActivity(intent);
                                    finish();


                                } else {
                                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }

                            } else {
                                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                } else {

                    DatabaseReference userDB = firebaseDatabase.getReference().child("users").child(userId).child("userInfo");
                    userDB.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()){
                                if (dataSnapshot.child("name").exists()){
                                    Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else {
                                    Intent intent = new Intent(SplashActivity.this, UpdateProfileActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
            }
        }, 1000);
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        if (firebaseAuth.getCurrentUser()!=null){
            userId = firebaseAuth.getCurrentUser().getUid();
        }

    }

}
