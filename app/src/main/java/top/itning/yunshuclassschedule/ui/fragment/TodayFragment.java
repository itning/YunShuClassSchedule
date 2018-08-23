package top.itning.yunshuclassschedule.ui.fragment;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.ui.adapter.TodayRecyclerViewAdapter;
import top.itning.yunshuclassschedule.util.ClassScheduleUtils;
import top.itning.yunshuclassschedule.util.DateUtils;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

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
            case APP_COLOR_CHANGE: {
                Log.d(TAG, "app color change , now afresh view");
                ViewHolder viewHolder = (ViewHolder) view.getTag();
                viewHolder.nsv.scrollTo(0, 0);
                ThemeChangeUtil.setBackgroundResources(requireContext(), viewHolder.ll);
                viewHolder.rv.getAdapter().notifyDataSetChanged();
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
        ThemeChangeUtil.setBackgroundResources(requireContext(), holder.ll);

        //RecyclerView初始化
        holder.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        todayRecyclerViewAdapter = new TodayRecyclerViewAdapter(classScheduleList, requireContext());
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
            if (whichClassNow == -1 || !ClassScheduleUtils.haveClassAfterTime(classScheduleList)) {
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
        try {
            String line1 = "", line2 = "", line3 = "", line4 = "";
            if (classScheduleList != null && !classScheduleList.isEmpty()) {
                int whichClassNow = DateUtils.getWhichClassNow();
                ClassSchedule classSchedule = classScheduleList.get(0);
                if (whichClassNow == -1) {
                    ClassSchedule lastCs = classScheduleList.get(classScheduleList.size() - 1);
                    String[] timeArray = DateUtils.getTimeList().get(lastCs.getSection() - 1).split("-");
                    if (!DateUtils.isInDateInterval(timeArray[0], timeArray[1])) {
                        //一节课没上判定
                        String[] firstTimeArray = DateUtils.getTimeList().get(classSchedule.getSection() - 1).split("-");
                        if (DateUtils.DF.parse(DateUtils.DF.format(new Date())).getTime() <= DateUtils.DF.parse(firstTimeArray[0]).getTime()) {
                            int restOfTheTime = DateUtils.getTheRestOfTheTime(firstTimeArray[0]);
                            line1 = "下节课";
                            line2 = classSchedule.getName();
                            line3 = classSchedule.getLocation();
                            line4 = "还有" + restOfTheTime + "分钟上课";
                            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_UP_TIME_CHANGE, "" + restOfTheTime));
                        } else {
                            line2 = "今天课全都上完了";
                            line3 = "(๑•̀ㅂ•́)و✧";
                        }
                    }
                } else {
                    //两种情况:正在上课,即将上课
                    String[] timeArray = DateUtils.getTimeList().get(classSchedule.getSection() - 1).split("-");
                    line1 = "下节课";
                    if (DateUtils.isInDateInterval(timeArray[0], timeArray[1])) {
                        //正在上课 非最后一节课
                        int restOfTheTime = DateUtils.getTheRestOfTheTime(timeArray[1]);
                        for (ClassSchedule c : classScheduleList) {
                            if (c.getSection() == classSchedule.getSection()) {
                                //当前循环的和正在上的一样
                                continue;
                            }
                            String start = DateUtils.getTimeList().get(c.getSection() - 1).split("-")[0];
                            if (ClassScheduleUtils.haveClassAfterTime(classScheduleList) && DateUtils.DF.parse(timeArray[0]).getTime() < DateUtils.DF.parse(start).getTime()) {
                                line2 = c.getName();
                                line3 = c.getLocation();
                                line4 = "还有" + restOfTheTime + "分钟下课";
                                EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_DOWN_TIME_CHANGE, "" + restOfTheTime));
                                break;
                            } else {
                                if (!ClassScheduleUtils.haveClassAfterTime(classScheduleList)) {
                                    line1 = "";
                                    line2 = "今天课全都上完了";
                                    line3 = "(๑•̀ㅂ•́)و✧";
                                    line4 = "";
                                    break;
                                }
                            }
                        }
                        //循环结束,没有下节课
                        if ("".equals(line4)) {
                            //最后一节课 正在上
                            line1 = "";
                            line2 = "这是最后一节课";
                            line3 = "还有" + restOfTheTime + "分钟下课";
                            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_DOWN_TIME_CHANGE, "" + restOfTheTime));
                        }
                    } else {
                        if (ClassScheduleUtils.haveClassAfterTime(classScheduleList)) {
                            //即将上课
                            int restOfTheTime = DateUtils.getTheRestOfTheTime(timeArray[0]);
                            line2 = classSchedule.getName();
                            line3 = classSchedule.getLocation();
                            line4 = "还有" + restOfTheTime + "分钟上课";
                            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.CLASS_UP_TIME_CHANGE, "" + restOfTheTime));
                        } else {
                            line1 = "";
                            line2 = "今天课全都上完了";
                            line3 = "(๑•̀ㅂ•́)و✧";
                            line4 = "";
                        }
                    }
                }
            } else {
                DaoSession daoSession = ((App) Objects.requireNonNull(getActivity()).getApplication()).getDaoSession();
                if (daoSession.getClassScheduleDao().count() == 0) {
                    line1 = "Oh! Shit!";
                    line2 = "没有课程数据";
                    line3 = "检查网络状态稍后再试";
                    line4 = "(ಥ﹏ಥ)";
                } else {
                    line2 = "今天没有课";
                    line3 = "ヾ(≧∇≦*)ゝ";
                }
            }
            holder.tvRemindRemind.setText(line1);
            holder.tvRemindName.setText(line2);
            holder.tvRemindLocation.setText(line3);
            holder.tvRemindTime.setText(line4);
        } catch (ParseException e) {
            Log.e(TAG, "pasrse exception ", e);
        }
    }

    /**
     * 设置滑动索引
     */
    private void setFinalIndex() {
        int index;
        whichClassNow = DateUtils.getWhichClassNow();
        if (whichClassNow != -1 && ClassScheduleUtils.haveClassAfterTime(classScheduleList)) {
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

        } else {
            index = 0;
        }
        finalIndex = index;
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
                } else {
                    new Handler().postDelayed(() -> adapter.notifyItemMoved(0, finalIndex), 1000);
                }
                top.set(false);
            }
        }
    }
}
