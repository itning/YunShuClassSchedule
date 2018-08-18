package top.itning.yunshuclassschedule.ui.fragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.ui.adapter.TodayRecyclerViewAdapter;
import top.itning.yunshuclassschedule.util.ClassScheduleUtils;
import top.itning.yunshuclassschedule.util.DateUtils;

/**
 * 今天
 *
 * @author itning
 */
public class TodayFragment extends Fragment {
    private static final String TAG = "TodayFragment";

    private View view;
    /**
     * 课程集合
     */
    private List<ClassSchedule> classScheduleList;
    /**
     * 是否滑动到顶部
     */
    private AtomicBoolean top;
    /**
     * 上次正在上的课程
     */
    private int lastClass = DateUtils.getWhichClassNow();
    /**
     * 向上滑动的临界值
     */
    public static final int SLIDE_UP_THRESHOLD = 20;
    /**
     * {@link TodayRecyclerViewAdapter}
     */
    private TodayRecyclerViewAdapter todayRecyclerViewAdapter;
    /**
     * 滑动时移动的坐标
     */
    private int finalIndex;
    /**
     * 标记是否已经Stop
     */
    private boolean stop;
    /**
     * 当Start时是否需要移动元素
     */
    private boolean needMoved;
    /**
     * 当前正在上的课
     */
    private int whichClassNow;

    static class ViewHolder {
        @BindView(R.id.rv)
        RecyclerView rv;
        @BindView(R.id.ll)
        LinearLayout ll;
        @BindView(R.id.rl)
        RelativeLayout rl;
        @BindView(R.id.nsv)
        NestedScrollView nsv;
        @BindView(R.id.tv_remind_time)
        TextView tvRemindTime;
        @BindView(R.id.tv_remind_remind)
        TextView tvRemindRemind;
        @BindView(R.id.tv_remind_name)
        TextView tvRemindName;
        @BindView(R.id.tv_remind_location)
        TextView tvRemindLocation;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case TIME_TICK_CHANGE: {
                //时间改变时,更新进度
                setViewProgress();
                //检查课程改变
                checkClassScheduleChange();
                //设置面板
                setPanelText((ViewHolder) view.getTag());
                break;
            }
            default:
        }
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onStart() {
        stop = false;
        if (needMoved) {
            ViewHolder holder = (ViewHolder) view.getTag();
            new Handler().postDelayed(() -> holder.rv.getAdapter().notifyItemMoved(0, finalIndex), 1000);
            needMoved = false;
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        stop = true;
        super.onStop();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.fragment_today, container, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        //初始化课程数据
        initClassScheduleListData();
        top = new AtomicBoolean(true);
        whichClassNow = DateUtils.getWhichClassNow();

        //LinearLayout背景颜色
        holder.ll.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimary));

        //RecyclerView初始化
        holder.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        todayRecyclerViewAdapter = new TodayRecyclerViewAdapter(classScheduleList, getContext());
        holder.rv.setAdapter(todayRecyclerViewAdapter);

        //设置LinearLayout的高度为总大小-RecyclerView的子项大小
        holder.rv.post(() -> view.post(() -> {
            int i = classScheduleList.size() == 0 ? holder.rv.getHeight() : holder.rv.getHeight() / classScheduleList.size();
            ViewGroup.LayoutParams lp;
            lp = holder.ll.getLayoutParams();
            lp.height = view.getHeight() - i;
            holder.ll.setLayoutParams(lp);
        }));

        //设置滑动索引
        setFinalIndex();
        //NestedScrollView滑动监听
        nestedScrollViewOnScrollChangeListener(holder);
        //设置面板文字
        setPanelText(holder);
        return this.view;
    }

    /**
     * 滑动监听
     *
     * @param holder {@link ViewHolder}
     */
    private void nestedScrollViewOnScrollChangeListener(ViewHolder holder) {
        RecyclerView.Adapter adapter = holder.rv.getAdapter();
        LinearLayout.LayoutParams pp = (LinearLayout.LayoutParams) holder.rl.getLayoutParams();
        holder.nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            //设置随滑动改变位置
            pp.topMargin = scrollY;
            holder.rl.setLayoutParams(pp);
            if (whichClassNow == -1) {
                return;
            }
            if (scrollY <= SLIDE_UP_THRESHOLD && !top.get()) {
                top.set(true);
                adapter.notifyItemMoved(finalIndex, 0);
            } else if (top.get() && scrollY == (holder.rv.getHeight() - holder.rv.getHeight() / classScheduleList.size())) {
                top.set(false);
                adapter.notifyItemMoved(0, finalIndex);
            }

        });
    }

    /**
     * 初始化课程数据
     */
    private void initClassScheduleListData() {
        DaoSession daoSession = ((App) Objects.requireNonNull(getActivity()).getApplication()).getDaoSession();
        classScheduleList = ClassScheduleUtils
                .orderListBySection(daoSession
                        .getClassScheduleDao()
                        .queryBuilder()
                        .where(ClassScheduleDao.Properties.Week.eq(DateUtils.getWeek()))
                        .list());
    }

    /**
     * 设置面板文字
     *
     * @param holder {@link ViewHolder}
     */
    private void setPanelText(ViewHolder holder) {
        ClassSchedule classSchedule = classScheduleList.get(0);
        holder.tvRemindRemind.setText("下节课");
        holder.tvRemindName.setText(classSchedule.getName());
        holder.tvRemindLocation.setText(classSchedule.getLocation());
        holder.tvRemindTime.setText("离上课还有14分钟");

        if (DateUtils.getWhichClassNow() == -1) {
            //
        }
    }

    /**
     * 设置滑动索引
     */
    private void setFinalIndex() {
        int index;
        whichClassNow = DateUtils.getWhichClassNow();
        if (whichClassNow != -1) {
            a:
            while (true) {
                if (whichClassNow == 0) {
                    index = 0;
                    break;
                }
                for (ClassSchedule c : classScheduleList) {
                    if (c.getSection() == whichClassNow) {
                        index = classScheduleList.indexOf(c);
                        break a;
                    }
                }
                whichClassNow--;
            }
            finalIndex = index;
        }
    }

    /**
     * 更新进度
     */
    private void setViewProgress() {
        View viewProgress = todayRecyclerViewAdapter.getViewProgress();
        if (viewProgress != null) {
            Display display = ((WindowManager) Objects.requireNonNull(Objects.requireNonNull(getContext()).getSystemService(Context.WINDOW_SERVICE))).getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            ViewGroup.LayoutParams layoutParams = viewProgress.getLayoutParams();
            layoutParams.width = DateUtils.getNowProgress(size.x, classScheduleList);
            viewProgress.setLayoutParams(layoutParams);
        }
    }

    /**
     * 检查课程改变
     */
    private void checkClassScheduleChange() {
        if (lastClass != DateUtils.getWhichClassNow()) {
            Log.d(TAG, "time changed,need update class schedule");
            lastClass = DateUtils.getWhichClassNow();
            classScheduleList = ClassScheduleUtils.orderListBySection(classScheduleList);
            ViewHolder holder = (ViewHolder) view.getTag();
            RecyclerView.Adapter adapter = holder.rv.getAdapter();
            adapter.notifyDataSetChanged();
            setFinalIndex();
            if (!top.get() && whichClassNow != -1) {
                if (stop) {
                    needMoved = true;
                }
                new Handler().postDelayed(() -> adapter.notifyItemMoved(0, finalIndex), 1000);
                top.set(false);
            }
        }
    }
}
