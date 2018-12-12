package top.itning.yunshuclassschedule.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
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
     * Bind View
     */
    private var mView: View? = null
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

    internal class ViewHolder(view: View) {
        @BindView(R.id.tl)
        lateinit var tl: TabLayout
        @BindView(R.id.vp)
        lateinit var vp: ViewPager

        init {
            ButterKnife.bind(this, view)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "on Create View")
        val holder: ViewHolder
        if (mView != null) {
            holder = mView!!.tag as ViewHolder
        } else {
            mView = inflater.inflate(R.layout.fragment_class_schedule, container, false)
            holder = ViewHolder(mView!!)
            mView!!.tag = holder
        }
        ThemeChangeUtil.setTabLayoutColor(requireContext(), holder.tl)
        initData(holder)
        //设置默认展示页面
        if (TODAY != PreferenceManager.getDefaultSharedPreferences(requireContext()).getString(DEFAULT_SHOW_MAIN_FRAGMENT, TODAY)) {
            holder.vp.currentItem = 1
            holder.tl.getTabAt(1)!!.select()
        }
        return mView!!
    }

    private fun initData(holder: ViewHolder) {
        //预加载
        holder.vp.offscreenPageLimit = fragmentList.size
        holder.vp.adapter = object : FragmentStatePagerAdapter(childFragmentManager) {

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
        holder.tl.setupWithViewPager(holder.vp)
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
                ThemeChangeUtil.setTabLayoutColor(requireContext(), (mView!!.tag as ViewHolder).tl)
            }
            ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT -> {
                val holder = mView!!.tag as ViewHolder
                val adapter = holder.vp.adapter
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
