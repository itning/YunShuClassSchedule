package top.itning.yunshuclassschedule.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.entity.Score;
import top.itning.yunshuclassschedule.ui.fragment.checkscore.CheckScoreLoginFragment;
import top.itning.yunshuclassschedule.ui.fragment.checkscore.CheckScoreShowFragment;
import top.itning.yunshuclassschedule.util.EventReceiver;

/**
 * 查成绩
 *
 * @author itning
 */
public class CheckScoreFragment extends Fragment implements EventReceiver {
    private static final String TAG = "CheckScoreFragment";
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "on Destroy");
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_score, container, false);
        fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, new CheckScoreLoginFragment())
                .commit();
        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {
            case SCORE_LOGIN_SUCCESS: {
                if (fragmentManager == null) {
                    fragmentManager = getChildFragmentManager();
                }
                CheckScoreShowFragment checkScoreShowFragment = new CheckScoreShowFragment();
                Bundle bundle = new Bundle();
                @SuppressWarnings("unchecked")
                ArrayList<Score> scoreList = (ArrayList<Score>) eventEntity.getData();
                bundle.putParcelableArrayList("scoreList", scoreList);
                checkScoreShowFragment.setArguments(bundle);
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, checkScoreShowFragment)
                        .addToBackStack("checkScoreShowFragment")
                        .commit();
                break;
            }
            case RETURN_LOGIN_FRAGMENT: {
                if (fragmentManager == null) {
                    fragmentManager = getChildFragmentManager();
                }
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_container, new CheckScoreLoginFragment())
                        .commit();
                break;
            }
            default:
        }
    }

    @Override
    public boolean eventTrigger() {
        int backStackEntryCount = fragmentManager.getBackStackEntryCount();
        if (backStackEntryCount == 1) {
            fragmentManager.popBackStackImmediate();
            return true;
        }
        return false;
    }
}
