package bd.com.dev.pocketcash.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.adapter.WithdrawAdapter;
import bd.com.dev.pocketcash.databinding.FragmentWithdrawBinding;
import bd.com.dev.pocketcash.model.Withdraw;

public class WithdrawFragment extends Fragment {

    private FragmentWithdrawBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private WithdrawAdapter withdrawAdapter;

    private String userId;
    private List<Withdraw> withdrawList;

    public WithdrawFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_withdraw, container, false);
        View view = binding.getRoot();

        withdrawList = new ArrayList<>();
        init();

        getWithdrawFromDB();


        return view;
    }

    private void getWithdrawFromDB() {
        binding.noTaskLayout.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        DatabaseReference withdrawDB = firebaseDatabase.getReference().child("users").child(userId).child("withdraws");
        withdrawDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        Withdraw withdraw = data.getValue(Withdraw.class);
                        withdrawList.add(withdraw);
                    }
                    withdrawAdapter.notifyDataSetChanged();

                    if (withdrawList.size() == 0) {
                        binding.noTaskLayout.setVisibility(View.VISIBLE);
                        binding.progressBar.setVisibility(View.GONE);
                    } else {
                        binding.noTaskLayout.setVisibility(View.GONE);
                        binding.progressBar.setVisibility(View.GONE);
                    }

                    withdrawAdapter.notifyDataSetChanged();

                } else {
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

        binding.withdrawRV.setLayoutManager(new LinearLayoutManager(getActivity()));
        withdrawAdapter = new WithdrawAdapter(getActivity(), withdrawList);
        binding.withdrawRV.setAdapter(withdrawAdapter);
    }

}
