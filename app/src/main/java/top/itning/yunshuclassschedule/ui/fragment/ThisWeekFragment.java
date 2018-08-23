package top.itning.yunshuclassschedule.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ClassScheduleUtils;


/**
 * 本周
 *
 * @author itning
 */
public class ThisWeekFragment extends Fragment {
    private static final String TAG = "ThisWeekFragment";

    private View view;

    static class ViewHolder {
        @BindView(R.id.schedule_gridlayout)
        GridLayout scheduleGridlayout;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on Create View");
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.fragment_this_week, container, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
        List<ClassSchedule> classScheduleList = daoSession.getClassScheduleDao().loadAll();
        ClassScheduleUtils.loadingView(classScheduleList, holder.scheduleGridlayout, requireContext(), requireActivity());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on Destroy");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case REFRESH_WEEK_FRAGMENT_DATA: {
                DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
                List<ClassSchedule> classScheduleList = daoSession.getClassScheduleDao().loadAll();
                ClassScheduleUtils.loadingView(classScheduleList, ((ViewHolder) view.getTag()).scheduleGridlayout, requireContext(), requireActivity());
                break;
            }
            default:
        }
    }
}
