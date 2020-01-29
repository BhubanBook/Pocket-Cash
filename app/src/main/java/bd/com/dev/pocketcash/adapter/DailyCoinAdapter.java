package bd.com.dev.pocketcash.adapter;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import bd.com.dev.pocketcash.R;
import bd.com.dev.pocketcash.databinding.ModelDailyEarningItemBinding;
import bd.com.dev.pocketcash.model.Earning;

public class DailyCoinAdapter extends RecyclerView.Adapter<DailyCoinAdapter.ViewHolder> {

    private List<Earning> dailyEarningList;
    private Context context;

    public DailyCoinAdapter(List<Earning> dailyEarningList, Context context) {
        this.dailyEarningList = dailyEarningList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        ModelDailyEarningItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(viewGroup.getContext()), R.layout.model_daily_earning_item,viewGroup,false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Earning earning = dailyEarningList.get(i);
        viewHolder.binding.dateTV.setText(convertMStoString(earning.getTime()));
        viewHolder.binding.coinTV.setText(earning.getCoin()+" Coin");
    }

    @Override
    public int getItemCount() {
        return dailyEarningList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ModelDailyEarningItemBinding binding;

        public ViewHolder(ModelDailyEarningItemBinding binding) {
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
