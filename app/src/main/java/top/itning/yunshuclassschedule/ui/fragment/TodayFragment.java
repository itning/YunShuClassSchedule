package top.itning.yunshuclassschedule.ui.fragment;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
                //时间改变时
                if (lastClass != DateUtils.getWhichClassNow()) {
                    Log.d(TAG, "time changed,need update class schedule");
                    lastClass = DateUtils.getWhichClassNow();
                    classScheduleList = ClassScheduleUtils.orderListBySection(classScheduleList);
                    ViewHolder holder = (ViewHolder) view.getTag();
                    RecyclerView.Adapter adapter = holder.rv.getAdapter();
                    adapter.notifyDataSetChanged();
                    if (!top.get()) {
                        new Handler().postDelayed(() -> adapter.notifyItemMoved(0, lastClass), 1000);
                        top.set(false);
                    }
                }
                break;
            }
            default:
        }
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
        DaoSession daoSession = ((App) Objects.requireNonNull(getActivity()).getApplication()).getDaoSession();
        classScheduleList = ClassScheduleUtils
                .orderListBySection(daoSession
                        .getClassScheduleDao()
                        .queryBuilder()
                        .where(ClassScheduleDao.Properties.Week.eq(DateUtils.getWeek()))
                        .list());
        //LinearLayout背景颜色
        holder.ll.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimary));
        //RecyclerView初始化
        holder.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        holder.rv.setAdapter(new TodayRecyclerViewAdapter(classScheduleList, getContext()));
        holder.rv.getAdapter().notifyDataSetChanged();
        //设置LinearLayout的高度为总大小-RecyclerView的子项大小
        holder.rv.post(() -> view.post(() -> {
            int i = classScheduleList.size() == 0 ? holder.rv.getHeight() : holder.rv.getHeight() / classScheduleList.size();
            ViewGroup.LayoutParams lp;
            lp = holder.ll.getLayoutParams();
            lp.height = view.getHeight() - i;
            holder.ll.setLayoutParams(lp);
        }));
        //NestedScrollView滑动监听
        RecyclerView.Adapter adapter = holder.rv.getAdapter();
        top = new AtomicBoolean(true);
        LinearLayout.LayoutParams pp = (LinearLayout.LayoutParams) holder.rl.getLayoutParams();
        holder.nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            //设置随滑动改变位置
            pp.topMargin = scrollY;
            holder.rl.setLayoutParams(pp);
            int whichClassNow = DateUtils.getWhichClassNow();
            if (whichClassNow == -1) {
                return;
            }
            if (scrollY <= 20 && !top.get()) {
                top.set(true);
                adapter.notifyItemMoved(whichClassNow, 0);
            } else if (top.get() && scrollY == (holder.rv.getHeight() - holder.rv.getHeight() / classScheduleList.size())) {
                top.set(false);
                adapter.notifyItemMoved(0, whichClassNow);
            }

        });
        return this.view;
    }
}
