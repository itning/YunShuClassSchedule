package top.itning.yunshuclassschedule.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;

/**
 * 课程表
 *
 * @author itning
 */
public class ClassScheduleFragment extends Fragment {
    private static final String TAG = "ClassScheduleFragment";

    /**
     * Bind View
     */
    private View view;
    /**
     * 标题集合
     */
    private List<String> titleList;
    /**
     * 片段集合
     */
    private List<Fragment> fragmentList;

    {
        titleList = new ArrayList<>();
        titleList.add("今天");
        titleList.add("本周");
        fragmentList = new ArrayList<>();
        fragmentList.add(new TodayFragment());
        fragmentList.add(new ThisWeekFragment());
    }

    static class ViewHolder {
        @BindView(R.id.tl)
        TabLayout tl;
        @BindView(R.id.vp)
        ViewPager vp;

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
            view = inflater.inflate(R.layout.fragment_class_schedule, container, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        if (holder.vp.getAdapter() == null) {
            Log.d(TAG, "adapter is null,now loading adapter");
            //预加载
            holder.vp.setOffscreenPageLimit(fragmentList.size());
            holder.vp.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {

                @Override
                public int getCount() {
                    return fragmentList.size();
                }

                @Override
                public Fragment getItem(int position) {
                    return fragmentList.get(position);
                }

                @Nullable
                @Override
                public CharSequence getPageTitle(int position) {
                    return titleList.get(position);
                }
            });
            holder.tl.setupWithViewPager(holder.vp);
            //设置默认展示页面
            if (!"today".equals(PreferenceManager.getDefaultSharedPreferences(getContext()).getString("default_show_main_fragment", "today"))) {
                holder.vp.setCurrentItem(1);
                Objects.requireNonNull(holder.tl.getTabAt(1)).select();
            }
        }
        return view;
    }
}
