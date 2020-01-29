package bd.com.dev.pocketcash.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.flags.impl.DataUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ActivityHomeBinding;
import bd.com.dev.pocketcash.model.User;
import bd.com.dev.pocketcash.utills.CustomProgressDialog;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";

    private ActivityHomeBinding binding;
    private ToolTipView myToolTipView;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private Snackbar snackbar;

    private String userId;
    private int coinRate = 0;
    private int currentBalance = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        setSupportActionBar(binding.toolbar);

        init();

        progressDialog = createCustomProgressDialog();

        getCurrentCoinRate();
        getUserInfo();


        binding.earnCoinLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, EarnCoinActivity.class));
            }
        });

        binding.withdrawLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, WithdrawActivity.class)
                        .putExtra("balance", currentBalance)
                        .putExtra("coinRate", coinRate));


            }
        });

        binding.playGameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, PlayGameActivity.class));
            }
        });

        binding.historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
            }
        });


    }

    private void getCurrentCoinRate() {
        DatabaseReference adminDB = firebaseDatabase.getReference().child("adminSection");
        adminDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    coinRate = Integer.parseInt(dataSnapshot.child("coinRate").getValue().toString());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            userId = firebaseAuth.getCurrentUser().getUid();
        }
    }

    private void getUserInfo() {

        DatabaseReference userDB = firebaseDatabase.getReference().child("users").child(userId);
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userInfo").exists()) {

                    if (dataSnapshot.child("userInfo").child("name").exists()) {
                        User user = dataSnapshot.child("userInfo").getValue(User.class);

                        binding.nameTV.setText(user.getName());
                        binding.numberTV.setText(user.getmNumber());
                        binding.referralCodeTV.setText(user.getReferralCode());

                        Glide.with(getApplicationContext()).applyDefaultRequestOptions(new RequestOptions()
                                .placeholder(R.drawable.person_image_icon)).load(user.getPhotoUrl()).into(binding.profileImageIV);

                        progressDialog.dismiss();
                    }
                    if (dataSnapshot.child("earning").child("currentBalance").exists()) {
                        String currentCoin = dataSnapshot.child("earning").child("currentBalance").getValue().toString();
                        currentBalance = Integer.parseInt(currentCoin);
                        binding.currentBalanceTV.setText(currentCoin + " Coin");
                    } else {
                        binding.currentBalanceTV.setText("0 Coin");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.termsAndCondition:
                startActivity(new Intent(HomeActivity.this, TermsAndConditionActivity.class));
                break;
            case R.id.contactUs:
                startActivity(new Intent(HomeActivity.this, ContactUsActivity.class));
                break;
        }

        return false;
    }

    public void openTooltip(View view) {
        if (coinRate > 0) {
            if (myToolTipView == null) {
                ToolTip toolTip = new ToolTip()
                        .withText("Current coin rate is " + coinRate + " tk per 1000 coin.")
                        .withColor(getResources().getColor(R.color.red))
                        .withTextColor(getResources().getColor(R.color.white))
                        .withShadow()
                        .withAnimationType(ToolTip.AnimationType.FROM_TOP);
                myToolTipView = binding.toolTipRelativeLayout.showToolTipForView(toolTip, binding.coinRateInfoIV);
                myToolTipView.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
                    @Override
                    public void onToolTipViewClicked(ToolTipView toolTipView) {
                        if (myToolTipView == null) {
                        } else {
                            myToolTipView.remove();
                            myToolTipView = null;
                        }
                    }
                });
            } else {
                myToolTipView.remove();
                myToolTipView = null;
            }
        }

    }

    private ProgressDialog createCustomProgressDialog() {
        ProgressDialog progressDialog = CustomProgressDialog.createProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.isIndeterminate();
        return progressDialog;
    }


    public String getCurrentSsid(Context context) {

        WifiManager wifiManager1 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager1.getConnectionInfo();
        String ssId = wifiInfo.getSSID();
        int networkId = wifiInfo.getNetworkId();
        Log.d(TAG, "SSID: " + wifiInfo.getIpAddress() + " networkId: " + networkId);


        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }

    private void createSnackbar(String title) {

        snackbar = Snackbar
                .make(binding.rootLayoutId, title, Snackbar.LENGTH_INDEFINITE);

        View sbView = snackbar.getView();
        snackbar.setActionTextColor(getResources().getColor(R.color.white));
        sbView.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
        TextView tv = sbView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                snackbar.dismiss();
            }
        });
        snackbar.show();

    }

}
