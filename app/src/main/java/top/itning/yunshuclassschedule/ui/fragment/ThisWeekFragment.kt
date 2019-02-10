package top.itning.yunshuclassschedule.ui.fragment

import android.graphics.Point
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.android.synthetic.main.fragment_this_week.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.ui.adapter.ClassScheduleItemLongClickListener
import top.itning.yunshuclassschedule.ui.fragment.setting.SettingsFragment
import top.itning.yunshuclassschedule.util.ClassScheduleUtils
import top.itning.yunshuclassschedule.util.ClassScheduleUtils.COPY_LIST
import top.itning.yunshuclassschedule.util.GlideApp


/**
 * 本周
 *
 * @author itning
 */
class ThisWeekFragment : Fragment() {
    private lateinit var mView: View
    private lateinit var clickListener: ClassScheduleItemLongClickListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "on Create View")
        mView = inflater.inflate(R.layout.fragment_this_week, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setViewBackground()
        val nowWeekNum = (PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!.toInt() - 1).toString()
        val daoSession = (requireActivity().application as App).daoSession
        val classScheduleList = daoSession.classScheduleDao.loadAll()
                .filter { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
                .toMutableList()
        clickListener = ClassScheduleItemLongClickListener(requireActivity(), classScheduleList, COPY_LIST)
        ClassScheduleUtils.loadingView(classScheduleList, schedule_gridlayout, clickListener, requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
            ConstantPool.Int.REFRESH_WEEK_FRAGMENT_DATA -> {
                val nowWeekNum = (PreferenceManager.getDefaultSharedPreferences(context).getString(SettingsFragment.NOW_WEEK_NUM, "1")!!.toInt() - 1).toString()
                val daoSession = (requireActivity().application as App).daoSession
                val classScheduleList = daoSession.classScheduleDao.loadAll()
                        .filter { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
                        .toMutableList()
                clickListener = ClassScheduleItemLongClickListener(requireActivity(), classScheduleList, COPY_LIST)
                ClassScheduleUtils.loadingView(classScheduleList, schedule_gridlayout, clickListener, requireActivity())
            }
            ConstantPool.Int.APP_COLOR_CHANGE -> {
                clickListener.updateBtnBackgroundTintList()
            }
            ConstantPool.Int.CLASS_WEEK_CHANGE -> {
                val nowWeekNum = (eventEntity.msg!!.toInt() - 1).toString()
                val daoSession = (requireActivity().application as App).daoSession
                val classScheduleList = daoSession.classScheduleDao.loadAll()
                        .filter { ClassScheduleUtils.isThisWeekOfClassSchedule(it, nowWeekNum) }
                        .toMutableList()
                clickListener = ClassScheduleItemLongClickListener(requireActivity(), classScheduleList, COPY_LIST)
                ClassScheduleUtils.loadingView(classScheduleList, schedule_gridlayout, clickListener, requireActivity())
            }
            else -> {
            }
        }
    }

    /**
     * 设置视图背景
     */
    private fun setViewBackground() {
        val file = requireContext().getFileStreamPath("background_img")
        if (file.exists() && file.isFile && file.length() != 0L) {
            val display = requireActivity().windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            Log.d(TAG, "screen width:" + size.x + " height:" + size.y)
            GlideApp
                    .with(this)
                    .load(file)
                    .override(size.x, size.y)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(object : CustomViewTarget<View, Drawable>(mView) {

                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            Log.d(TAG, "on Load Failed : $errorDrawable")
                            Toast.makeText(requireContext(), "图片加载失败", Toast.LENGTH_LONG).show()
                        }

                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            Log.d(TAG, "on Resource Ready : $resource")
                            view.background = resource
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {
                            Log.d(TAG, "on Resource Cleared : $placeholder")
                            view.background = placeholder
                        }
                    })
        } else {
            Log.d(TAG, "file is not exists , now use default background")
            mView.setBackgroundResource(R.drawable.this_week_background)
        }
    }

    companion object {
        private const val TAG = "ThisWeekFragment"
    }
}
