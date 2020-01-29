package bd.com.dev.pocketcash.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipView;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ActivityWithdrawBinding;
import bd.com.dev.pocketcash.model.Withdraw;
import bd.com.dev.pocketcash.utills.PaymentType;
import bd.com.dev.pocketcash.utills.Status;

public class WithdrawActivity extends AppCompatActivity {

    private static final int MINIMUM_AMOUNT_FOR_BR = 500;
    private static final int MINIMUM_COIN_FOR_BR = 1000;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private AlertDialog clickedDialog;

    private ActivityWithdrawBinding binding;
    private ToolTipView myToolTipView;
    private int coinRatePerThousand = 0;
    private int currentBalance = 0;
    private int amount = 0;
    private String userId;

    private Snackbar snackbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw);

        init();

        if (getIntent() != null) {
            coinRatePerThousand = getIntent().getIntExtra("coinRate", 0);
            currentBalance = getIntent().getIntExtra("balance", 0);
        }

        binding.currentBalanceTV.setText(currentBalance + " Coin");

        amount = (coinRatePerThousand * currentBalance) / 1000;


        if (currentBalance < MINIMUM_COIN_FOR_BR) {
            binding.submitBtn.setEnabled(false);
            binding.submitBtn.setBackgroundColor(getResources().getColor(R.color.gray));
            createSnackbar("You need minimum 1000 coin for withdraw");
        } else if (amount < MINIMUM_AMOUNT_FOR_BR) {
            binding.bKashRB.setEnabled(false);
            binding.rocketRB.setEnabled(false);
        }


        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == binding.rocketRB.getId()) {
                    int maxLength = 12;
                    InputFilter[] fArray = new InputFilter[1];
                    fArray[0] = new InputFilter.LengthFilter(maxLength);
                    binding.mNumberET.setFilters(fArray);
                    binding.mNumberConfirmET.setFilters(fArray);
                } else {
                    int length = binding.mNumberET.getText().length();
                    if (length > 11) {
                        binding.mNumberET.getText().delete(length - 1, length);
                    }
                    int length2 = binding.mNumberConfirmET.getText().length();
                    if (length2 > 11) {
                        binding.mNumberConfirmET.getText().delete(length - 1, length);
                    }

                    int maxLength = 11;
                    InputFilter[] fArray = new InputFilter[1];
                    fArray[0] = new InputFilter.LengthFilter(maxLength);
                    binding.mNumberET.setFilters(fArray);
                    binding.mNumberConfirmET.setFilters(fArray);
                }
            }
        });

    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        userId = firebaseAuth.getCurrentUser().getUid();
    }


    public void openTooltip(View view) {
        if (coinRatePerThousand > 0) {
            if (myToolTipView == null) {
                ToolTip toolTip = new ToolTip()
                        .withText("Current coin rate is " + coinRatePerThousand + " tk per 1000 coin.")
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


    public void submit(View view) {

        binding.submitBtn.setEnabled(false);

        String paymentType = "";
        if (binding.rechargeRB.isChecked()) {
            paymentType = PaymentType.RECHARGE;
        } else if (binding.bKashRB.isChecked()) {
            paymentType = PaymentType.BKASH;
        } else if (binding.rocketRB.isChecked()) {
            paymentType = PaymentType.ROCKET;
        }
        String mNumber = binding.mNumberET.getText().toString();
        String mNumberConfirm = binding.mNumberConfirmET.getText().toString();


        if (mNumber.equals("") || mNumber.isEmpty()) {
            binding.mNumberET.setError("Enter a valid mobile number.");
            binding.mNumberET.requestFocus();
            binding.submitBtn.setEnabled(true);
        } else if (mNumberConfirm.equals("") || mNumberConfirm.isEmpty()) {
            binding.mNumberConfirmET.setError("Confirm your mobile number.");
            binding.mNumberConfirmET.requestFocus();
            binding.submitBtn.setEnabled(true);
        } else if (!mNumber.equals(mNumberConfirm)) {
            binding.mNumberConfirmET.setError("Mobile number didn't match.");
            binding.mNumberConfirmET.requestFocus();
            binding.submitBtn.setEnabled(true);
        } else {
            if (paymentType.equals(PaymentType.ROCKET)) {
                if (mNumber.matches("01[0-9]{10}")) {
                    saveRequestToDB(paymentType, mNumber);
                } else {
                    binding.mNumberET.setError("Enter a valid mobile number for rocket.");
                    binding.mNumberET.requestFocus();
                    binding.submitBtn.setEnabled(true);
                }
            } else {
                if (mNumber.matches("01[0-9]{9}")) {
                    saveRequestToDB(paymentType, mNumber);
                } else {
                    binding.mNumberET.setError("Enter a valid mobile number.");
                    binding.mNumberET.requestFocus();
                    binding.submitBtn.setEnabled(true);
                }
            }
        }


    }

    private void saveRequestToDB(String paymentType, String mNumber) {
        DatabaseReference userWithdrawDB = firebaseDatabase.getReference().child("users").child(userId).child("withdraws");
        final String paymentId = userWithdrawDB.push().getKey();

        Withdraw withdraw = new Withdraw(paymentId, paymentType, mNumber, Status.PENDING,
                System.currentTimeMillis(), currentBalance, amount, coinRatePerThousand);

        userWithdrawDB.child(paymentId).setValue(withdraw).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    DatabaseReference userWithdrawDB = firebaseDatabase.getReference().
                            child("users").child(userId).child("earning").child("currentBalance");
                    userWithdrawDB.setValue(0).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                showSuccessDialog();

                            } else {
                                DatabaseReference currentUserWithdrawDB = firebaseDatabase.getReference().child("users")
                                        .child(userId).child("withdraws").child(paymentId);
                                currentUserWithdrawDB.removeValue();
                                Intent intent = new Intent(WithdrawActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });

    }



    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_withdraw, null);

        Button okBtn = mView.findViewById(R.id.okBtnID);

        builder.setView(mView);
        clickedDialog = builder.create();
        clickedDialog.show();
        clickedDialog.setCancelable(false);

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WithdrawActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }


    public void back(View view) {
        onBackPressed();
    }


}
