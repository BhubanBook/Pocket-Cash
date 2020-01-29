package bd.com.dev.pocketcash.activity;

import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.adapter.ViewPagerAdapter;
import bd.com.dev.pocketcash.databinding.ActivityHistoryBinding;
import bd.com.dev.pocketcash.fragment.DailyEarningFragment;
import bd.com.dev.pocketcash.fragment.WithdrawFragment;

public class HistoryActivity extends AppCompatActivity {

    private ViewPagerAdapter adapter;
    private ActivityHistoryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_history);

        adapter= new ViewPagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new DailyEarningFragment(),"Daily Earned");
        adapter.addFragment(new WithdrawFragment(),"Withdraw");

        binding.viewPagerId.setOffscreenPageLimit(2);
        binding.viewPagerId.setSaveFromParentEnabled(false);
        binding.viewPagerId.setAdapter(adapter);

        binding.tabLayoutId.setupWithViewPager(binding.viewPagerId);

    }

    public void back(View view) {
        onBackPressed();
    }
}
