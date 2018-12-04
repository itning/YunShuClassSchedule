package top.itning.yunshuclassschedule.ui.fragment;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import static top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.DEFAULT_SHOW_MAIN_FRAGMENT;

/**
 * 课程表
 *
 * @author itning
 */
public class ClassScheduleFragment extends Fragment {
    private static final String TAG = "ClassScheduleFragment";

    private static final String TODAY = "today";

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
        Log.d(TAG, "on Create View");
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.fragment_class_schedule, container, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        ThemeChangeUtil.setTabLayoutColor(requireContext(), holder.tl);
        initData(holder);
        //设置默认展示页面
        if (!TODAY.equals(PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(DEFAULT_SHOW_MAIN_FRAGMENT, TODAY))) {
            holder.vp.setCurrentItem(1);
            Objects.requireNonNull(holder.tl.getTabAt(1)).select();
        }
        return view;
    }

    private void initData(ViewHolder holder) {
        //预加载
        holder.vp.setOffscreenPageLimit(fragmentList.size());
        holder.vp.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager()) {

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

            @Override
            public int getItemPosition(@NonNull Object object) {
                Log.d(TAG, "getItemPosition: " + object);
                return POSITION_NONE;
            }

            @Override
            public void restoreState(Parcelable state, ClassLoader loader) {
                try {
                    super.restoreState(state, loader);
                } catch (NullPointerException e) {
                    // null caught
                }
            }
        });
        holder.tl.setupWithViewPager(holder.vp);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "on Create");
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
            case APP_COLOR_CHANGE: {
                ThemeChangeUtil.setTabLayoutColor(requireContext(), ((ViewHolder) view.getTag()).tl);
                break;
            }
            case REFRESH_CLASS_SCHEDULE_FRAGMENT: {
                ViewHolder holder = (ViewHolder) view.getTag();
                PagerAdapter adapter = holder.vp.getAdapter();
                if (adapter == null) {
                    Toast.makeText(requireContext(), "未找到适配器，尝试重新打开APP解决此问题", Toast.LENGTH_LONG).show();
                    break;
                }
                adapter.notifyDataSetChanged();
                break;
            }
            default:
        }
    }
}
