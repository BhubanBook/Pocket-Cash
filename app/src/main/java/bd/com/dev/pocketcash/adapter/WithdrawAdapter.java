package bd.com.dev.pocketcash.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ModelWithdrawItemBinding;
import bd.com.dev.pocketcash.model.Withdraw;

public class WithdrawAdapter extends RecyclerView.Adapter<WithdrawAdapter.ViewHolder> {
    private Context context;
    private List<Withdraw> withdrawList;

    public WithdrawAdapter(Context context, List<Withdraw> withdrawList) {
        this.context = context;
        this.withdrawList = withdrawList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ModelWithdrawItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()),
                R.layout.model_withdraw_item,viewGroup,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Withdraw withdraw = withdrawList.get(i);
        viewHolder.binding.dateTV.setText(convertMStoString(withdraw.getRequestDate()));
        viewHolder.binding.mobileNumberTV.setText(withdraw.getmNumber()+" ("+withdraw.getPaymentType()+")");
        viewHolder.binding.coinTV.setText(withdraw.getRequestCoin()+" Coin");
        viewHolder.binding.statusTV.setText("• "+withdraw.getPaymentStatus());
    }

    @Override
    public int getItemCount() {
        return withdrawList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ModelWithdrawItemBinding binding;
        public ViewHolder(ModelWithdrawItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    private String convertMStoString(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
