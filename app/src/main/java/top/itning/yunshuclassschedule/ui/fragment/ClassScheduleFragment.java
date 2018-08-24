package top.itning.yunshuclassschedule.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 课程表
 *
 * @author itning
 */
public class ClassScheduleFragment extends Fragment {
    private static final String TAG = "ClassScheduleFragment";
    private static final String TODAY = "today";
    private static final String DEFAULT_SHOW_MAIN_FRAGMENT = "default_show_main_fragment";

    /**
     * Bind View
     */
    private View view;
    /**
     * 标题集合
     */
    private final List<String> titleList;
    /**
     * 片段集合
     */
    private final List<Fragment> fragmentList;

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
        ThemeChangeUtil.setTabLayoutColor(requireContext(), holder.tl);
        if (holder.vp.getAdapter() == null) {
            Log.d(TAG, "adapter is null,now loading adapter");
            //预加载
            holder.vp.setOffscreenPageLimit(fragmentList.size());
            holder.vp.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {

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
            if (!TODAY.equals(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(DEFAULT_SHOW_MAIN_FRAGMENT, TODAY))) {
                holder.vp.setCurrentItem(1);
                Objects.requireNonNull(holder.tl.getTabAt(1)).select();
            }
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case APP_COLOR_CHANGE: {
                ThemeChangeUtil.setTabLayoutColor(requireContext(), ((ViewHolder) view.getTag()).tl);
                break;
            }
            default:
        }
    }
}
