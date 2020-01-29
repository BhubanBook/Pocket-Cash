package bd.com.dev.pocketcash.activity;

import android.databinding.DataBindingUtil;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        binding.deviceId.setText(deviceId);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (binding.ButtonOneId.getVisibility() == View.VISIBLE) {
                    binding.ButtonOneId.setVisibility(View.GONE);

                    handler.postDelayed(runnable, 3000);
                } else {
                    binding.ButtonOneId.setVisibility(View.VISIBLE);

                    handler.postDelayed(runnable, 3000);
                }
            }
        };


        handler.postDelayed(runnable, 3000);
        binding.clickedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCountdownTimer();
            }
        });

    }

    private void startCountdownTimer() {

        new CountDownTimer(60000, 1000) {

            @Override

            public void onTick(long millisUntilFinished) {
                /*            converting the milliseconds into days, hours, minutes and seconds and displaying it in textviews             */

                binding.days.setText(TimeUnit.HOURS.toDays(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)) + "");

                binding.hours.setText((TimeUnit.MILLISECONDS.toHours(millisUntilFinished) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millisUntilFinished))) + "");

                binding.minutes.setText((TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished))) + "");

                binding.seconds.setText((TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))) + "");
            }

            @Override

            public void onFinish() {
                /*            clearing all fields and displaying countdown finished message             */

                binding.days.setText("Count down completed");
                binding.hours.setText("");
                binding.minutes.setText("");
                binding.seconds.setText("");
            }
        }.start();
    }
}
