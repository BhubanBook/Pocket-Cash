package bd.com.dev.pocketcash.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.adapter.DailyCoinAdapter;
import bd.com.dev.pocketcash.databinding.FragmentDailyEarningBinding;
import bd.com.dev.pocketcash.model.Earning;

public class DailyEarningFragment extends Fragment {


    private FragmentDailyEarningBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DailyCoinAdapter dailyCoinAdapter;

    private String userId;
    private List<Earning> dailyEarningList;

    public DailyEarningFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_daily_earning, container, false);
        View view = binding.getRoot();
        dailyEarningList = new ArrayList<>();

        init();

        getDataFromDB();

        binding.earningRV.setAdapter(dailyCoinAdapter);

        return view;
    }

    private void getDataFromDB() {
        binding.noTaskLayout.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        DatabaseReference dailyEarningDB = firebaseDatabase.getReference().child("users")
                .child(userId).child("earning").child("dailyEarning");

        dailyEarningDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dailyEarningList.clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        long time = getMSDate(data.getKey());
                        int coin = 0;
                        for (DataSnapshot data1: data.getChildren()){
                            Earning earning = data1.getValue(Earning.class);
                            coin = coin+earning.getCoin();
                        }

                        dailyEarningList.add(new Earning("",time,coin));

                    }
                    if (dailyEarningList.size()==0){
                        binding.noTaskLayout.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                    }else {
                        binding.noTaskLayout.setVisibility(View.GONE);
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    dailyCoinAdapter.notifyDataSetChanged();

                }else {
                    binding.noTaskLayout.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
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
        userId = firebaseAuth.getCurrentUser().getUid();

        binding.earningRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        dailyCoinAdapter = new DailyCoinAdapter(dailyEarningList, getActivity());
    }

    private long getMSDate(String stringDate) {
        SimpleDateFormat SDF = new SimpleDateFormat("ddMMyyyy");
        Date date = null;
        try {
            date = SDF.parse(stringDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date.getTime();
    }

}
