package top.itning.yunshuclassschedule.ui.fragment;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.util.ClassScheduleUtils;


/**
 * 本周
 *
 * @author itning
 */
public class ThisWeekFragment extends Fragment {
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
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.fragment_this_week, container, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        ClassScheduleUtils.loadingView(holder.scheduleGridlayout, Objects.requireNonNull(getContext()), Objects.requireNonNull(getActivity()));
        return view;
    }
}
