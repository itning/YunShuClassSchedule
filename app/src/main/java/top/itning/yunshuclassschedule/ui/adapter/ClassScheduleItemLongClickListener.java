package top.itning.yunshuclassschedule.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.tencent.bugly.crashreport.CrashReport;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.UUID;

import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.ClassScheduleDao;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.util.ThemeChangeUtil;

/**
 * 每节课长按监听
 *
 * @author itning
 */
public final class ClassScheduleItemLongClickListener implements View.OnLongClickListener {
    private static final String TAG = "CSLongClickListener";

    private static final int COPY_SIZE = 3;
    private final Activity activity;
    private final ClassScheduleDao classScheduleDao;
    private final List<String> copyList;
    private final List<ClassSchedule> classScheduleList;
    private final View inflate;
    private ClassSchedule selectClassSchedule;
    private AlertDialog alertDialog;
    private AppCompatButton copyBtn;
    private AppCompatButton pasteBtn;

    @SuppressLint("InflateParams")
    public ClassScheduleItemLongClickListener(@NonNull final Activity activity, final List<ClassSchedule> classScheduleList, @NonNull final List<String> copyList) {
        this.activity = activity;
        this.classScheduleDao = ((App) activity.getApplication()).getDaoSession().getClassScheduleDao();
        this.copyList = copyList;
        this.classScheduleList = classScheduleList;
        this.inflate = LayoutInflater.from(activity).inflate(R.layout.dialog_class_schedule, null);
        initAlertDialog();
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick:the view instance is " + v);
        Object tag = v.getTag();
        Log.d(TAG, "onLongClick:the view tag is " + tag);
        if (tag == null) {
            CrashReport.postCatchedException(new Throwable("on long clicked , but view tag is null"));
            Log.e(TAG, "on long clicked , but view tag is null");
            Toast.makeText(activity, "获取TAG失败,请联系开发者", Toast.LENGTH_SHORT).show();
            return false;
        }
        String[] classSplit = tag.toString().split("-");
        TextInputEditText tvteacher = inflate.findViewById(R.id.tv_teacher);
        TextInputEditText tvlocation = inflate.findViewById(R.id.tv_location);
        TextInputEditText tvname = inflate.findViewById(R.id.tv_name);
        TextInputLayout tlname = inflate.findViewById(R.id.tl_name);
        TextInputLayout tllocation = inflate.findViewById(R.id.tl_location);
        TextInputLayout tlteacher = inflate.findViewById(R.id.tl_teacher);
        //设置内容文字
        setText(tvteacher, tvlocation, tvname, classSplit);
        alertDialog.setTitle(new StringBuilder(7).append("星期").append(classSplit[1]).append("第").append(classSplit[0]).append("节课"));
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(vv -> {
            if (isInputError(tvteacher, tvlocation, tvname, tlname, tllocation, tlteacher)) {
                return;
            }
            if (selectClassSchedule != null) {
                updateData(tvteacher, tvlocation, tvname);
            } else {
                insertData(classSplit, tvteacher, tvlocation, tvname);
            }
            alertDialog.dismiss();
            EventBus.getDefault().post(new EventEntity(ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT));
        });
        return true;
    }

    private boolean isInputError(TextInputEditText tvteacher, TextInputEditText tvlocation, TextInputEditText tvname, TextInputLayout tlname, TextInputLayout tllocation, TextInputLayout tlteacher) {
        tlname.setError(null);
        tllocation.setError(null);
        tlteacher.setError(null);
        if ("".equals(tvname.getText().toString())) {
            tlname.setError("请输入课程名");
            return true;
        }
        if ("".equals(tvlocation.getText().toString())) {
            tllocation.setError("请输入地点");
            return true;
        }
        if ("".equals(tvteacher.getText().toString())) {
            tlteacher.setError("请输入教师");
            return true;
        }
        return false;
    }

    private void updateData(TextInputEditText tvteacher, TextInputEditText tvlocation, TextInputEditText tvname) {
        selectClassSchedule.setName(tvname.getText().toString().trim());
        selectClassSchedule.setLocation(tvlocation.getText().toString().trim());
        selectClassSchedule.setTeacher(tvteacher.getText().toString().trim());
        classScheduleDao.update(selectClassSchedule);
    }

    private void insertData(String[] classSplit, TextInputEditText tvteacher, TextInputEditText tvlocation, TextInputEditText tvname) {
        ClassSchedule classSchedule = new ClassSchedule();
        classSchedule.setId(UUID.randomUUID().toString());
        classSchedule.setName(tvname.getText().toString().trim());
        classSchedule.setLocation(tvlocation.getText().toString().trim());
        classSchedule.setTeacher(tvteacher.getText().toString().trim());
        classSchedule.setSection(Integer.parseInt(classSplit[0]));
        classSchedule.setWeek(Integer.parseInt(classSplit[1]));
        classScheduleDao.insert(classSchedule);
    }

    private void initAlertDialog() {
        if (alertDialog == null) {
            copyBtn = inflate.findViewById(R.id.btn_copy);
            pasteBtn = inflate.findViewById(R.id.btn_paste);
            TextInputEditText tvteacher = inflate.findViewById(R.id.tv_teacher);
            TextInputEditText tvlocation = inflate.findViewById(R.id.tv_location);
            TextInputEditText tvname = inflate.findViewById(R.id.tv_name);
            initBtnAction(tvteacher, tvlocation, tvname);
            alertDialog = new AlertDialog.Builder(activity)
                    .setView(inflate)
                    .setPositiveButton("确定", null)
                    .setNegativeButton("取消", null)
                    .setNeutralButton("删除", (dialog, which) -> {
                        if (selectClassSchedule != null) {
                            new AlertDialog.Builder(activity)
                                    .setTitle("删除确认")
                                    .setMessage("确定删除星期" + selectClassSchedule.getWeek() + "的第" + selectClassSchedule.getSection() + "节课么?")
                                    .setPositiveButton("确定", (a, b) -> {
                                        classScheduleDao.delete(selectClassSchedule);
                                        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.REFRESH_CLASS_SCHEDULE_FRAGMENT));
                                        selectClassSchedule = null;
                                    })
                                    .setNegativeButton("取消", null)
                                    .show();
                        }
                    }).create();
        }
    }

    private void setText(TextInputEditText tvteacher, TextInputEditText tvlocation, TextInputEditText tvname, String[] classSplit) {
        tvlocation.setText(null);
        tvname.setText(null);
        tvteacher.setText(null);
        if (classScheduleList != null && !classScheduleList.isEmpty()) {
            selectClassSchedule = null;
            for (ClassSchedule classSchedule : classScheduleList) {
                if ((classSchedule.getSection() + "").equals(classSplit[0]) && (classSchedule.getWeek() + "").equals(classSplit[1])) {
                    selectClassSchedule = classSchedule;
                    tvteacher.setText(classSchedule.getTeacher());
                    tvname.setText(classSchedule.getName());
                    tvlocation.setText(classSchedule.getLocation());
                }
            }
        }
    }

    private void initBtnAction(TextInputEditText tvteacher, TextInputEditText tvlocation, TextInputEditText tvname) {
        updateBtnBackgroundTintList();
        copyBtn.setOnClickListener(v1 -> {
            copyList.clear();
            copyList.add(tvname.getText().toString().trim());
            copyList.add(tvlocation.getText().toString().trim());
            copyList.add(tvteacher.getText().toString().trim());
            Toast.makeText(activity, "已复制", Toast.LENGTH_SHORT).show();
        });
        pasteBtn.setOnClickListener(v1 -> {
            if (copyList.size() == COPY_SIZE) {
                tvname.setText(copyList.get(0));
                tvlocation.setText(copyList.get(1));
                tvteacher.setText(copyList.get(2));
                Toast.makeText(activity, "已粘贴", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void updateBtnBackgroundTintList() {
        ColorStateList colorStateList = ColorStateList.valueOf(ThemeChangeUtil.getNowThemeColorAccent(activity));
        copyBtn.setBackgroundTintList(colorStateList);
        pasteBtn.setBackgroundTintList(colorStateList);
    }
}
