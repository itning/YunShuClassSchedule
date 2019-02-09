package top.itning.yunshuclassschedule.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.annotation.Px
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.fragment_class_schedule.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.activity.MainActivity
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment.Companion.DEFAULT_SHOW_MAIN_FRAGMENT
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.util.*

/**
 * 课程表
 *
 * @author itning
 */
class ClassScheduleFragment : Fragment() {

    /**
     * 标题集合
     */
    private val titleList: MutableList<String>
    /**
     * 片段集合
     */
    private val fragmentList: MutableList<Fragment>

    init {
        titleList = ArrayList()
        titleList.add("今天")
        titleList.add("本周")
        fragmentList = ArrayList()
        fragmentList.add(TodayFragment())
        fragmentList.add(ThisWeekFragment())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "on Create View")
        return inflater.inflate(R.layout.fragment_class_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ThemeChangeUtil.setTabLayoutColor(requireContext(), tl)
        initData()
        //设置默认展示页面
        if (TODAY != PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(DEFAULT_SHOW_MAIN_FRAGMENT, TODAY)) {
            vp.currentItem = 1
            tl.getTabAt(1)?.select()
        }
    }

    private fun initData() {
        //预加载
        vp.offscreenPageLimit = fragmentList.size
        vp.adapter = object : FragmentStatePagerAdapter(childFragmentManager) {

            override fun getCount(): Int {
                return fragmentList.size
            }

            override fun getItem(position: Int): Fragment {
                return fragmentList[position]
            }

            @Nullable
            override fun getPageTitle(position: Int): CharSequence {
                return titleList[position]
            }

            override fun getItemPosition(`object`: Any): Int {
                Log.d(TAG, "getItemPosition: $`object`")
                return PagerAdapter.POSITION_NONE
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
                try {
                    super.restoreState(state, loader)
                } catch (e: Exception) {
                    // null caught
                }

            }
        }
        vp.clearOnPageChangeListeners()
        vp.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, @Px positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.TOOLBAR_TITLE_CHANGE, MainActivity.ACTION_BAR_TITLE_FORMAT.format(Date())))
                } else {
                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.TOOLBAR_TITLE_CHANGE, "第${MainActivity.tempNumberOfWeek}周"))
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        tl.setupWithViewPager(vp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "on Create")
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(eventEntity: EventEntity) {
        when (eventEntity.id) {
            ConstantPool.Int.APP_COLOR_CHANGE -> {
                ThemeChangeUtil.setTabLayoutColor(requireContext(), tl)
            }
            ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT -> {
                val adapter = vp.adapter
                if (adapter == null) {
                    Toast.makeText(requireContext(), "未找到适配器，尝试重新打开APP解决此问题", Toast.LENGTH_LONG).show()
                    return
                }
                adapter.notifyDataSetChanged()
            }
            else -> {
            }
        }
    }

    companion object {
        private const val TAG = "ClassScheduleFragment"
        private const val TODAY = "today"
    }
}
