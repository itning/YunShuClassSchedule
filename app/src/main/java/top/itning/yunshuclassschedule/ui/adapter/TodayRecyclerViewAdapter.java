package top.itning.yunshuclassschedule.ui.adapter;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.ui.view.RoundBackChange;
import top.itning.yunshuclassschedule.util.ClassScheduleUtils;
import top.itning.yunshuclassschedule.util.DateUtils;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 今天课程列表适配器
 *
 * @author itning
 */
public class TodayRecyclerViewAdapter extends RecyclerView.Adapter {
    private static final String TAG = "TodayRecyclerAdapter";
    /**
     * 列表数据集合
     */
    private final List<ClassSchedule> scheduleList;
    /**
     * 颜色数组
     */
    private final int[] colorArray = new int[7];
    /**
     * {@link Context}
     */
    private final Context context;
    /**
     * 随机好的颜色集合
     */
    private final ArrayList<Integer> showColorList;
    private View viewProgress;

    public TodayRecyclerViewAdapter(@NonNull List<ClassSchedule> scheduleList, @NonNull Context context) {
        Log.d(TAG, "new Today Recycler View Adapter");
        this.context = context;
        this.scheduleList = scheduleList;
        //数组赋值
        colorArray[0] = ContextCompat.getColor(context, R.color.class_color_1);
        colorArray[1] = ContextCompat.getColor(context, R.color.class_color_2);
        colorArray[2] = ContextCompat.getColor(context, R.color.class_color_3);
        colorArray[3] = ContextCompat.getColor(context, R.color.class_color_4);
        colorArray[4] = ContextCompat.getColor(context, R.color.class_color_5);
        colorArray[5] = ContextCompat.getColor(context, R.color.class_color_6);
        colorArray[6] = ContextCompat.getColor(context, R.color.class_color_7);
        //随机颜色集合构建
        Random random = new Random();
        showColorList = new ArrayList<>(colorArray.length);
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
        Log.d(TAG, "onCreateViewHolder");
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_class_rv, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder pos->" + position);
        ClassSchedule classSchedule = scheduleList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tvName.setText(classSchedule.getName());
        viewHolder.tvLocation.setText(classSchedule.getLocation());
        viewHolder.tvTime.setText(DateUtils.getTimeList().get(classSchedule.getSection() - 1));
        viewHolder.round.setBackColor(colorArray[showColorList.get(position)]);
        //显示设置可见性
        viewHolder.flNo.setVisibility(View.VISIBLE);
        viewHolder.viewBottom.setVisibility(View.INVISIBLE);
        viewHolder.viewTop.setVisibility(View.INVISIBLE);
        viewHolder.viewLeft.setVisibility(View.INVISIBLE);
        viewHolder.viewProgress.setVisibility(View.INVISIBLE);
        ThemeChangeUtil.setProgressBackgroundResource(context, viewHolder.viewProgress);
        ThemeChangeUtil.setBackgroundResources(context, viewHolder.viewBottom, viewHolder.viewTop, viewHolder.viewLeft, viewHolder.viewCenter);
        if (position == 0 && ClassScheduleUtils.haveClassAfterTime(scheduleList)) {
            //是当前正在或要上的课程
            viewHolder.flNo.setVisibility(View.INVISIBLE);
            viewHolder.viewBottom.setVisibility(View.VISIBLE);
            viewHolder.viewTop.setVisibility(View.VISIBLE);
            viewHolder.viewLeft.setVisibility(View.VISIBLE);
            viewHolder.viewProgress.setVisibility(View.VISIBLE);
            Display display = ((WindowManager) Objects.requireNonNull(context.getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            ViewGroup.LayoutParams layoutParams = viewHolder.viewProgress.getLayoutParams();
            layoutParams.width = DateUtils.getNowProgress(size.x, scheduleList);
            viewHolder.viewProgress.setLayoutParams(layoutParams);
            viewProgress = viewHolder.viewProgress;
        }
    }

    public View getViewProgress() {
        return viewProgress;
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
        @BindView(R.id.view_center)
        View viewCenter;
        @BindView(R.id.view_progress)
        View viewProgress;
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
