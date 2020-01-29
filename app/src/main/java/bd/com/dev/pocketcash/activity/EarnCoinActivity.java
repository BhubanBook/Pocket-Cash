package bd.com.dev.pocketcash.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ActivityEarnCoinBinding;

public class EarnCoinActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private static final String TAG = "EarnCoinActivity";
    private static final SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
    private static final int ONE_TIME_EARNING = 10;
    private static final int PER_DAY_EARNING = 150;
    private static final int INTERSTITIAL_AWARD = 1;
    private static final int VIDEO_AWARD = 3;
    private static final int MAX_CLICKED_PER_DAY = 2;

    private RewardedVideoAd rewardedVideoAd;
    private InterstitialAd interstitialAd;

    private ActivityEarnCoinBinding binding;
    private Dialog dialog;
    private TextView hoursTV, minutesTV, secondsTV;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    private String userId;

    private AlertDialog clickedDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_earn_coin);

        init();


        DatabaseReference invalidClickedDB = firebaseDatabase.getReference().child("users").child(userId).child("invalidActivities");
        invalidClickedDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("todayClicked").exists()) {
                    int todayClicked = Integer.parseInt(dataSnapshot.child("todayClicked").getValue().toString());
                    if (todayClicked >= MAX_CLICKED_PER_DAY) {

                        DatabaseReference ref = firebaseDatabase.getReference();
                        Map map = new HashMap();
                        map.put("timestamp", ServerValue.TIMESTAMP);
                        ref.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    DatabaseReference timeRef = firebaseDatabase.getReference().child("timestamp");
                                    timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                long timestamp = Long.parseLong(dataSnapshot.getValue().toString());
                                                DatabaseReference earningDB = firebaseDatabase.getReference().child("users").child(userId)
                                                        .child("earning");

                                                Map<String, Object> map = new HashMap<>();
                                                map.put("blockTime", getMidNightDateValue(timestamp));
                                                map.put("todayEarning", 0);
                                                map.put("oneTimeEarning", 0);

                                                earningDB.updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            DatabaseReference invalidClickedDB = firebaseDatabase.getReference()
                                                                    .child("users").child(userId).child("invalidActivities").child("todayClicked");
                                                            invalidClickedDB.setValue(0);
                                                            checkValidity();
                                                        }
                                                    }
                                                });

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        checkValidity();
                    }
                } else {
                    checkValidity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        binding.tenCoinBtnTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rewardedVideoAd.isLoaded()) {
                    rewardedVideoAd.show();
                }
            }
        });

        binding.fiveCoinBtnTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }
        });


    }

    private void checkValidity() {

        DatabaseReference earningDB = firebaseDatabase.getReference().child("users").child(userId)
                .child("earning");
        earningDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("todayEarning").exists()
                        && dataSnapshot.child("blockTime").exists()
                        && dataSnapshot.child("oneTimeEarning").exists()) {

                    final long blockTime = Long.parseLong(dataSnapshot.child("blockTime").getValue().toString());
                    final int todayEarning = Integer.parseInt(dataSnapshot.child("todayEarning").getValue().toString());
                    final int oneTimeEarning = Integer.parseInt(dataSnapshot.child("oneTimeEarning").getValue().toString());

                    DatabaseReference ref = firebaseDatabase.getReference();
                    Map map = new HashMap();
                    map.put("timestamp", ServerValue.TIMESTAMP);
                    ref.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                DatabaseReference timeRef = firebaseDatabase.getReference().child("timestamp");
                                timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            long timestamp = Long.parseLong(dataSnapshot.getValue().toString());
                                            if (blockTime - timestamp > 0) {
                                                createDialog("Please come back after");
                                                binding.loadingLayout.setVisibility(View.GONE);
                                                startCountdownTimer(blockTime - timestamp);
                                            } else {
                                                if (todayEarning > PER_DAY_EARNING) {
                                                    blockForWholeDay(timestamp);
                                                } else if (oneTimeEarning > ONE_TIME_EARNING) {
                                                    blockForThirtyMinutes(timestamp);
                                                } else {
                                                    checkCurrentDate();
                                                }

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


                } else if (dataSnapshot.exists() && dataSnapshot.child("todayEarning").exists()
                        && dataSnapshot.child("oneTimeEarning").exists()) {
                    final int todayEarning = Integer.parseInt(dataSnapshot.child("todayEarning").getValue().toString());
                    final int oneTimeEarning = Integer.parseInt(dataSnapshot.child("oneTimeEarning").getValue().toString());

                    DatabaseReference ref = firebaseDatabase.getReference();
                    Map map = new HashMap();
                    map.put("timestamp", ServerValue.TIMESTAMP);
                    ref.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                DatabaseReference timeRef = firebaseDatabase.getReference().child("timestamp");
                                timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            long timestamp = Long.parseLong(dataSnapshot.getValue().toString());

                                            if (todayEarning > PER_DAY_EARNING) {
                                                blockForWholeDay(timestamp);
                                            } else if (oneTimeEarning > ONE_TIME_EARNING) {
                                                blockForThirtyMinutes(timestamp);
                                            } else {
                                                checkCurrentDate();
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

                } else {
                    checkCurrentDate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void blockForThirtyMinutes(long timestamp) {
        DatabaseReference earningDB = firebaseDatabase.getReference().child("users").child(userId)
                .child("earning");

        Map<String, Object> map = new HashMap<>();
        map.put("blockTime", (timestamp + 1800000));
        map.put("oneTimeEarning", 0);

        earningDB.updateChildren(map);
    }

    private void blockForWholeDay(long timestamp) {

        DatabaseReference earningDB = firebaseDatabase.getReference().child("users").child(userId)
                .child("earning");

        Map<String, Object> map = new HashMap<>();
        map.put("blockTime", getMidNightDateValue(timestamp));
        map.put("todayEarning", 0);
        map.put("oneTimeEarning", 0);

        earningDB.updateChildren(map);

    }

    private long getMidNightDateValue(long timestamp) {

        SimpleDateFormat SDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String myDate = year + "/" + month + "/" + day + " 23:59:59";
        Date date = null;
        try {
            date = SDF.parse(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date.getTime();

    }


    private void checkCurrentDate() {

        DatabaseReference earningDB = firebaseDatabase.getReference().child("users").child(userId)
                .child("earning");
        earningDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("lastActiveDate").exists()) {
                    final long lastActiveDate = Long.parseLong(dataSnapshot.child("lastActiveDate").getValue().toString());
                    DatabaseReference ref = firebaseDatabase.getReference();
                    Map map = new HashMap();
                    map.put("timestamp", ServerValue.TIMESTAMP);
                    ref.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                DatabaseReference timeRef = firebaseDatabase.getReference().child("timestamp");
                                timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            long timestamp = Long.parseLong(dataSnapshot.getValue().toString());
                                            if (getStringDate(timestamp).equals(getStringDate(lastActiveDate))) {
                                                initAds();
                                            } else {
                                                updateLastActiveDate(timestamp);
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
                } else {
                    DatabaseReference ref = firebaseDatabase.getReference();
                    Map map = new HashMap();
                    map.put("timestamp", ServerValue.TIMESTAMP);
                    ref.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                DatabaseReference timeRef = firebaseDatabase.getReference().child("timestamp");
                                timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            long timestamp = Long.parseLong(dataSnapshot.getValue().toString());

                                            updateLastActiveDate(timestamp);

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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateLastActiveDate(long timestamp) {
        Map<String, Object> currentMap = new HashMap<>();
        currentMap.put("todayEarning", 0);
        currentMap.put("lastActiveDate", timestamp);
        DatabaseReference earningDB1 = firebaseDatabase.getReference().child("users").child(userId)
                .child("earning");
        earningDB1.updateChildren(currentMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    initAds();
                }
            }
        });
    }

    private void initAds() {

        MobileAds.initialize(this,
                getResources().getString(R.string.app_id));

        //Reward Video ad
        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        rewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();


        //Interstitial ad
        interstitialAd = new InterstitialAd(this);
        setInterstitialListener();
        interstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_ad_id));
        loadInterstitialAd();

        checkEmpty();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    private void checkEmpty() {
        if (binding.loadingLayout.getVisibility() == View.GONE) {
            if (binding.tenCoinBtnTV.getVisibility() == View.GONE && binding.fiveCoinBtnTV.getVisibility() == View.GONE) {
                binding.noTaskLayout.setVisibility(View.VISIBLE);
            } else {
                binding.noTaskLayout.setVisibility(View.GONE);
            }
        } else {
            binding.noTaskLayout.setVisibility(View.GONE);
        }
    }


    // -------------------- For InterstitialAd Callback Start----------------------
    private void loadInterstitialAd() {
        interstitialAd.loadAd(new AdRequest.Builder().build());
    }

    private void setInterstitialListener() {
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                binding.fiveCoinBtnTV.setVisibility(View.VISIBLE);
                binding.loadingLayout.setVisibility(View.GONE);
                checkEmpty();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                checkEmpty();
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                checkEmpty();
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                checkEmpty();
                saveAdsClicked();
            }

            @Override
            public void onAdLeftApplication() {
                checkEmpty();
                //Toast.makeText(EarnCoinActivity.this, "Left", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdClosed() {
                getServerTime(INTERSTITIAL_AWARD);
                checkEmpty();
                binding.fiveCoinBtnTV.setVisibility(View.GONE);
                loadInterstitialAd();
            }
        });
    }
    // -------------------- For InterstitialAd Callback End----------------------


    // -------------------- For Reward Video Callback Start----------------------

    private void loadRewardedVideoAd() {
        rewardedVideoAd.loadAd(getResources().getString(R.string.reward_ads_id),
                new AdRequest.Builder().build());
    }

    @Override
    public void onRewarded(RewardItem reward) {
        checkEmpty();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        checkEmpty();
        saveAdsClicked();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        checkEmpty();
        binding.tenCoinBtnTV.setVisibility(View.GONE);
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        checkEmpty();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        checkEmpty();
        binding.tenCoinBtnTV.setVisibility(View.VISIBLE);
        binding.loadingLayout.setVisibility(View.GONE);
    }

    @Override
    public void onRewardedVideoAdOpened() {
        checkEmpty();
    }

    @Override
    public void onRewardedVideoStarted() {
        checkEmpty();
    }

    @Override
    public void onRewardedVideoCompleted() {
        getServerTime(VIDEO_AWARD);
        binding.tenCoinBtnTV.setVisibility(View.GONE);
        loadRewardedVideoAd();
        checkEmpty();
    }


    // -------------------- For Reward Video Callback End----------------------


    public void back(View view) {
        onBackPressed();
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
        hoursTV = dialog.findViewById(R.id.hoursTV);
        minutesTV = dialog.findViewById(R.id.minutesTV);
        secondsTV = dialog.findViewById(R.id.secondsTV);

        messageTV.setText(message);
        TextView okBtnTV = dialog.findViewById(R.id.okBtn);
        okBtnTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        return dialog;
    }


    private void startCountdownTimer(long timeInMS) {


        new CountDownTimer(timeInMS, 1000) {

            @Override

            public void onTick(long millisUntilFinished) {

                hoursTV.setText((TimeUnit.MILLISECONDS.toHours(millisUntilFinished) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millisUntilFinished))) + "h : ");

                minutesTV.setText((TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))) + "m : ");

                secondsTV.setText((TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))) + "s");
            }

            @Override

            public void onFinish() {
                finish();
            }
        }.start();
    }


    private void saveAdsClicked() {
        DatabaseReference userDB = firebaseDatabase.getReference().child("users").child(userId).child("invalidActivities");
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int totalClicked = Integer.parseInt(dataSnapshot.child("totalClicked").getValue().toString());
                    int todayClicked = Integer.parseInt(dataSnapshot.child("todayClicked").getValue().toString());

                    Map<String, Object> clickedMap = new HashMap<>();
                    clickedMap.put("totalClicked", (totalClicked + 1));
                    clickedMap.put("todayClicked", (todayClicked + 1));

                    saveClicked(clickedMap);
                } else {

                    Map<String, Object> clickedMap = new HashMap<>();
                    clickedMap.put("totalClicked", 1);
                    clickedMap.put("todayClicked", 1);

                    saveClicked(clickedMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void saveClicked(Map<String, Object> clickedMap) {
        DatabaseReference userDB = firebaseDatabase.getReference().child("users").child(userId).child("invalidActivities");
        userDB.updateChildren(clickedMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showClickedErrorDialog();
            }
        });
    }


    private void getServerTime(final int coin) {

        DatabaseReference ref = firebaseDatabase.getReference();
        Map map = new HashMap();
        map.put("timestamp", ServerValue.TIMESTAMP);
        ref.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    DatabaseReference timeRef = firebaseDatabase.getReference().child("timestamp");
                    timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                long timestamp = Long.parseLong(dataSnapshot.getValue().toString());
                                saveToDB(coin, timestamp);
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

    private void saveToDB(final int coin, final long timestamp) {
        final DatabaseReference earningDB = firebaseDatabase.getReference().child("users").child(userId)
                .child("earning");
        earningDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.child("totalEarning").exists()
                        && dataSnapshot.child("todayEarning").exists()
                        && dataSnapshot.child("currentBalance").exists()
                        && dataSnapshot.child("oneTimeEarning").exists()) {
                    int totalEarning = Integer.parseInt(dataSnapshot.child("totalEarning").getValue().toString());
                    int todayEarning = Integer.parseInt(dataSnapshot.child("todayEarning").getValue().toString());
                    int currentBalance = Integer.parseInt(dataSnapshot.child("currentBalance").getValue().toString());
                    int oneTimeEarning = Integer.parseInt(dataSnapshot.child("oneTimeEarning").getValue().toString());

                    Map<String, Object> earningMap = new HashMap<>();
                    earningMap.put("totalEarning", (totalEarning + coin));
                    earningMap.put("todayEarning", (todayEarning + coin));
                    earningMap.put("currentBalance", (currentBalance + coin));
                    earningMap.put("oneTimeEarning", (oneTimeEarning + coin));
                    earningDB.updateChildren(earningMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateDailyInfo(coin, timestamp);
                            }
                        }
                    });

                } else {
                    Map<String, Object> earningMap = new HashMap<>();
                    earningMap.put("totalEarning", coin);
                    earningMap.put("todayEarning", coin);
                    earningMap.put("oneTimeEarning", coin);
                    earningMap.put("currentBalance", coin);
                    earningDB.updateChildren(earningMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                updateDailyInfo(coin, timestamp);
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void updateDailyInfo(final int coin, long timestamp) {
        DatabaseReference dailyEarningDB = firebaseDatabase.getReference().child("users")
                .child(userId).child("earning").child("dailyEarning").child(getStringDate(timestamp));
        String id = dailyEarningDB.push().getKey();
        Map<String, Object> dailyCoinMap = new HashMap<>();
        dailyCoinMap.put("coin", coin);
        dailyCoinMap.put("id", id);
        dailyCoinMap.put("time", timestamp);
        dailyEarningDB.child(id).setValue(dailyCoinMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showEarnCoinDialog(coin);
                }
            }
        });
    }

    private void showEarnCoinDialog(int coin) {
        if (clickedDialog!=null && clickedDialog.isShowing()){

        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View mView = getLayoutInflater().inflate(R.layout.dialog_earn_success, null);

            Button okBtn = mView.findViewById(R.id.okBtnID);
            TextView coinTV = mView.findViewById(R.id.coinTV);
            coinTV.setText("+" + coin + " Coin");

            builder.setView(mView);
            final AlertDialog successDialog = builder.create();
            successDialog.show();

            okBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    successDialog.dismiss();
                }
            });
        }

    }

    private void showClickedErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_clicked_alert, null);

        Button okBtn = mView.findViewById(R.id.okBtnID);

        builder.setView(mView);
        clickedDialog = builder.create();
        clickedDialog.show();
        clickedDialog.setCancelable(false);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickedDialog.dismiss();
                finish();
                startActivity(getIntent());
            }
        });
    }


    private String getStringDate(long milliSeconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
