package top.itning.yunshuclassschedule.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.ui.view.RoundBackChange;

/**
 * @author itning
 */
public class TodayRecyclerViewAdapter extends RecyclerView.Adapter {
    private List<ClassSchedule> scheduleList;
    private int[] colorArray = new int[7];
    private final ArrayList<Integer> showColorList;

    public TodayRecyclerViewAdapter(@NonNull List<ClassSchedule> scheduleList, Context context) {
        this.scheduleList = scheduleList;
        colorArray[0] = ContextCompat.getColor(context, R.color.class_color_1);
        colorArray[1] = ContextCompat.getColor(context, R.color.class_color_2);
        colorArray[2] = ContextCompat.getColor(context, R.color.class_color_3);
        colorArray[3] = ContextCompat.getColor(context, R.color.class_color_4);
        colorArray[4] = ContextCompat.getColor(context, R.color.class_color_5);
        colorArray[5] = ContextCompat.getColor(context, R.color.class_color_6);
        colorArray[6] = ContextCompat.getColor(context, R.color.class_color_7);

        Random random = new Random();
        showColorList = new ArrayList<>();
        do {
            int number = random.nextInt(colorArray.length);
            if (!showColorList.contains(number)) {
                showColorList.add(number);
            }
        } while (showColorList.size() != colorArray.length);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_rv, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ClassSchedule classSchedule = scheduleList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tvName.setText(classSchedule.getName());
        viewHolder.tvLocation.setText(classSchedule.getLocation());
        viewHolder.tvTime.setText("09:50-12:30");
        viewHolder.round.setBackColor(colorArray[showColorList.get(position)]);
        if (position == 0) {
            viewHolder.flNo.setVisibility(View.INVISIBLE);
            viewHolder.viewBottom.setVisibility(View.VISIBLE);
            viewHolder.viewTop.setVisibility(View.VISIBLE);
            viewHolder.viewLeft.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return scheduleList == null ? 0 : scheduleList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.view_left)
        View viewLeft;
        @BindView(R.id.view_top)
        View viewTop;
        @BindView(R.id.view_bottom)
        View viewBottom;
        @BindView(R.id.round)
        RoundBackChange round;
        @BindView(R.id.fl_no)
        FrameLayout flNo;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_location)
        TextView tvLocation;
        @BindView(R.id.tv_time)
        TextView tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
