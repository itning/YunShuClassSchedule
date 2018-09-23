package top.itning.yunshuclassschedule.ui.fragment;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.App;
import top.itning.yunshuclassschedule.entity.ClassSchedule;
import top.itning.yunshuclassschedule.entity.DaoSession;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.ui.adapter.ClassScheduleItemLongClickListener;
import top.itning.yunshuclassschedule.util.ClassScheduleUtils;
import top.itning.yunshuclassschedule.util.FileUtils;
import top.itning.yunshuclassschedule.util.GlideApp;

import static top.itning.yunshuclassschedule.util.ClassScheduleUtils.COPY_LIST;
import static top.itning.yunshuclassschedule.util.FileUtils.MAX_IMAGE_FILE_SIZE;


/**
 * 本周
 *
 * @author itning
 */
public class ThisWeekFragment extends Fragment {
    private static final String TAG = "ThisWeekFragment";

    private View view;
    private ClassScheduleItemLongClickListener clickListener;

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
        Log.d(TAG, "on Create View");
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.fragment_this_week, container, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        setViewBackground();
        DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
        List<ClassSchedule> classScheduleList = daoSession.getClassScheduleDao().loadAll();
        clickListener = new ClassScheduleItemLongClickListener(requireActivity(), classScheduleList, COPY_LIST);
        ClassScheduleUtils.loadingView(classScheduleList, holder.scheduleGridlayout, clickListener, requireActivity());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on Destroy");
        EventBus.getDefault().unregister(this);
        if (clickListener != null) {
            clickListener = null;
        }
        super.onDestroy();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case REFRESH_WEEK_FRAGMENT_DATA: {
                DaoSession daoSession = ((App) requireActivity().getApplication()).getDaoSession();
                List<ClassSchedule> classScheduleList = daoSession.getClassScheduleDao().loadAll();
                clickListener = new ClassScheduleItemLongClickListener(requireActivity(), classScheduleList, COPY_LIST);
                ClassScheduleUtils.loadingView(classScheduleList, ((ViewHolder) view.getTag()).scheduleGridlayout, clickListener, requireActivity());
                break;
            }
            case NOTIFICATION_BACKGROUND_CHANGE: {
                FileUtils.transferFile(requireContext(), (Uri) eventEntity.getData(), "background_img");
                setViewBackground();
                break;
            }
            case APP_COLOR_CHANGE: {
                clickListener.updateBtnBackgroundTintList();
                break;
            }
            default:
        }
    }

    /**
     * 设置视图背景
     */
    private void setViewBackground() {
        File file = requireContext().getFileStreamPath("background_img");
        if (file.exists() && file.isFile() && file.length() != 0) {
            // Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
            long fileSizeInKB = file.length() / 1024;
            // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            long fileSizeInMB = fileSizeInKB / 1024;
            Log.d(TAG, "file size :" + fileSizeInKB + "KB");
            if (fileSizeInMB > MAX_IMAGE_FILE_SIZE) {
                boolean delete = file.delete();
                Log.d(TAG, "delete :" + delete);
                view.setBackgroundResource(R.drawable.this_week_background);
                return;
            }
            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Log.d(TAG, "screen width:" + size.x + " height:" + size.y);
            GlideApp
                    .with(this)
                    .load(file)
                    .override(size.x, size.y)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(new CustomViewTarget<View, Drawable>(view) {

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            Log.d(TAG, "on Load Failed : " + errorDrawable);
                            Toast.makeText(requireContext(), "图片加载失败", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            Log.d(TAG, "on Resource Ready : " + resource);
                            view.setBackground(resource);
                        }

                        @Override
                        protected void onResourceCleared(@Nullable Drawable placeholder) {
                            Log.d(TAG, "on Resource Cleared : " + placeholder);
                            view.setBackground(placeholder);
                        }
                    });
        } else {
            Log.d(TAG, "file is not exists , now use default background");
            view.setBackgroundResource(R.drawable.this_week_background);
        }
    }
}
