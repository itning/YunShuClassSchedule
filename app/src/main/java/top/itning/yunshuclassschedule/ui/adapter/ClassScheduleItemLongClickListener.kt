package top.itning.yunshuclassschedule.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.greenrobot.eventbus.EventBus
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.common.App
import top.itning.yunshuclassschedule.common.ConstantPool
import top.itning.yunshuclassschedule.entity.ClassSchedule
import top.itning.yunshuclassschedule.entity.ClassScheduleDao
import top.itning.yunshuclassschedule.entity.EventEntity
import top.itning.yunshuclassschedule.util.ThemeChangeUtil
import java.util.*

/**
 * 每节课长按监听
 *
 * @author itning
 */
class ClassScheduleItemLongClickListener
constructor(@param:NonNull private val activity: Activity, private val classScheduleList: List<ClassSchedule>?, @param:NonNull private val copyList: MutableList<String>) : View.OnLongClickListener {
    private val classScheduleDao: ClassScheduleDao = (activity.application as App).daoSession!!.classScheduleDao
    @SuppressLint("InflateParams")
    private val inflate: View = LayoutInflater.from(activity).inflate(R.layout.dialog_class_schedule, null)
    private var selectClassSchedule: ClassSchedule? = null
    private lateinit var alertDialog: AlertDialog
    private lateinit var copyBtn: AppCompatButton
    private lateinit var pasteBtn: AppCompatButton

    init {
        initAlertDialog()
    }

    override fun onLongClick(v: View): Boolean {
        Log.d(TAG, "onLongClick:the view instance is $v")
        val tag = v.tag
        Log.d(TAG, "onLongClick:the view tag is " + tag!!)
        val classSplit = tag.toString().split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val tvteacher = inflate.findViewById<TextInputEditText>(R.id.tv_teacher)
        val tvlocation = inflate.findViewById<TextInputEditText>(R.id.tv_location)
        val tvname = inflate.findViewById<TextInputEditText>(R.id.tv_name)
        val tlname = inflate.findViewById<TextInputLayout>(R.id.tl_name)
        val tllocation = inflate.findViewById<TextInputLayout>(R.id.tl_location)
        val tlteacher = inflate.findViewById<TextInputLayout>(R.id.tl_teacher)
        //设置内容文字
        setText(tvteacher, tvlocation, tvname, classSplit)
        alertDialog.setTitle(StringBuilder(7).append("星期").append(classSplit[1]).append("第").append(classSplit[0]).append("节课"))
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener { _ ->
            if (isInputError(tvteacher, tvlocation, tvname, tlname, tllocation, tlteacher)) {
                return@setOnClickListener
            }
            if (selectClassSchedule != null) {
                updateData(tvteacher, tvlocation, tvname)
            } else {
                insertData(classSplit, tvteacher, tvlocation, tvname)
            }
            alertDialog.dismiss()
            EventBus.getDefault().post(EventEntity(ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT))
        }
        return true
    }

    private fun isInputError(tvteacher: TextInputEditText, tvlocation: TextInputEditText, tvname: TextInputEditText, tlname: TextInputLayout, tllocation: TextInputLayout, tlteacher: TextInputLayout): Boolean {
        tlname.error = null
        tllocation.error = null
        tlteacher.error = null
        if ("" == tvname.text.toString()) {
            tlname.error = "请输入课程名"
            return true
        }
        if ("" == tvlocation.text.toString()) {
            tllocation.error = "请输入地点"
            return true
        }
        if ("" == tvteacher.text.toString()) {
            tlteacher.error = "请输入教师"
            return true
        }
        return false
    }

    private fun updateData(tvteacher: TextInputEditText, tvlocation: TextInputEditText, tvname: TextInputEditText) {
        selectClassSchedule!!.name = tvname.text.toString().trim { it <= ' ' }
        selectClassSchedule!!.location = tvlocation.text.toString().trim { it <= ' ' }
        selectClassSchedule!!.teacher = tvteacher.text.toString().trim { it <= ' ' }
        classScheduleDao.update(selectClassSchedule)
    }

    private fun insertData(classSplit: Array<String>, tvteacher: TextInputEditText, tvlocation: TextInputEditText, tvname: TextInputEditText) {
        val classSchedule = ClassSchedule()
        classSchedule.id = UUID.randomUUID().toString()
        classSchedule.name = tvname.text.toString().trim { it <= ' ' }
        classSchedule.location = tvlocation.text.toString().trim { it <= ' ' }
        classSchedule.teacher = tvteacher.text.toString().trim { it <= ' ' }
        classSchedule.section = Integer.parseInt(classSplit[0])
        classSchedule.week = Integer.parseInt(classSplit[1])
        classScheduleDao.insert(classSchedule)
    }

    private fun initAlertDialog() {
        copyBtn = inflate.findViewById(R.id.btn_copy)
        pasteBtn = inflate.findViewById(R.id.btn_paste)
        val tvteacher = inflate.findViewById<TextInputEditText>(R.id.tv_teacher)
        val tvlocation = inflate.findViewById<TextInputEditText>(R.id.tv_location)
        val tvname = inflate.findViewById<TextInputEditText>(R.id.tv_name)
        initBtnAction(tvteacher, tvlocation, tvname)
        alertDialog = AlertDialog.Builder(activity)
                .setView(inflate)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null)
                .setNeutralButton("删除") { _, _ ->
                    if (selectClassSchedule != null) {
                        AlertDialog.Builder(activity)
                                .setTitle("删除确认")
                                .setMessage("确定删除星期" + selectClassSchedule!!.week + "的第" + selectClassSchedule!!.section + "节课么?")
                                .setPositiveButton("确定") { _, _ ->
                                    classScheduleDao.delete(selectClassSchedule)
                                    EventBus.getDefault().post(EventEntity(ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT))
                                    selectClassSchedule = null
                                }
                                .setNegativeButton("取消", null)
                                .show()
                    }
                }.create()
    }

    private fun setText(tvteacher: TextInputEditText, tvlocation: TextInputEditText, tvname: TextInputEditText, classSplit: Array<String>) {
        tvlocation.text = null
        tvname.text = null
        tvteacher.text = null
        if (classScheduleList != null && !classScheduleList.isEmpty()) {
            selectClassSchedule = null
            for (classSchedule in classScheduleList) {
                if (classSchedule.section.toString() + "" == classSplit[0] && classSchedule.week.toString() + "" == classSplit[1]) {
                    selectClassSchedule = classSchedule
                    tvteacher.setText(classSchedule.teacher)
                    tvname.setText(classSchedule.name)
                    tvlocation.setText(classSchedule.location)
                }
            }
        }
    }

    private fun initBtnAction(tvteacher: TextInputEditText, tvlocation: TextInputEditText, tvname: TextInputEditText) {
        updateBtnBackgroundTintList()
        copyBtn.setOnClickListener { _ ->
            copyList.clear()
            copyList.add(tvname.text.toString().trim { it <= ' ' })
            copyList.add(tvlocation.text.toString().trim { it <= ' ' })
            copyList.add(tvteacher.text.toString().trim { it <= ' ' })
            Toast.makeText(activity, "已复制", Toast.LENGTH_SHORT).show()
        }
        pasteBtn.setOnClickListener { _ ->
            if (copyList.size == COPY_SIZE) {
                tvname.setText(copyList[0])
                tvlocation.setText(copyList[1])
                tvteacher.setText(copyList[2])
                Toast.makeText(activity, "已粘贴", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateBtnBackgroundTintList() {
        val colorStateList = ColorStateList.valueOf(ThemeChangeUtil.getNowThemeColorAccent(activity))
        copyBtn.backgroundTintList = colorStateList
        pasteBtn.backgroundTintList = colorStateList
    }

    companion object {
        private const val TAG = "CSLongClickListener"
        private const val COPY_SIZE = 3
    }
}
