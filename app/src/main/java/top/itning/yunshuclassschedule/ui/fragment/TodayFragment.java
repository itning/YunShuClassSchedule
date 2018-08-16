package top.itning.yunshuclassschedule.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.ui.adapter.TodayRecyclerViewAdapter;

/**
 * 今天
 *
 * @author itning
 */
public class TodayFragment extends Fragment {
    private static final String TAG = "TodayFragment";

    private View view;

    static class ViewHolder {
        @BindView(R.id.rv)
        RecyclerView rv;
        @BindView(R.id.ll)
        LinearLayout ll;
        @BindView(R.id.nsv)
        NestedScrollView nsv;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
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

        holder.ll.setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getContext()), R.color.colorPrimary));

        holder.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        DaoSession daoSession = ((App) Objects.requireNonNull(getActivity()).getApplication()).getDaoSession();
        List<ClassSchedule> list = daoSession.getClassScheduleDao().queryBuilder().where(ClassScheduleDao.Properties.Week.eq("1")).list();
        holder.rv.setAdapter(new TodayRecyclerViewAdapter(list, getContext()));

        holder.rv.post(() -> view.post(() -> {
            int i = holder.rv.getHeight() / list.size();
            ViewGroup.LayoutParams lp;
            lp = holder.ll.getLayoutParams();
            lp.height = view.getHeight() - i;
            holder.ll.setLayoutParams(lp);
        }));

        RecyclerView.Adapter adapter = holder.rv.getAdapter();
        AtomicBoolean top = new AtomicBoolean(true);
        holder.nsv.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY <= 20 && !top.get()) {
                top.set(true);
                adapter.notifyItemMoved(3, 0);
            } else if (top.get() && scrollY == (holder.rv.getHeight() - holder.rv.getHeight() / list.size())) {
                top.set(false);
                adapter.notifyItemMoved(0, 3);
            }
        });
        return this.view;
    }

}
