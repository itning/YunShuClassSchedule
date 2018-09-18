package top.itning.yunshuclassschedule.ui.fragment.checkscore;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.common.ConstantPool;
import top.itning.yunshuclassschedule.entity.EventEntity;
import top.itning.yunshuclassschedule.entity.Score;
import top.itning.yunshuclassschedule.ui.adapter.ScoreRecyclerViewAdapter;

/**
 * 展示
 *
 * @author itning
 */
public class CheckScoreShowFragment extends Fragment {
    private static final String TAG = "CheckScoreShowFragment";
    @BindView(R.id.rv)
    RecyclerView rv;
    Unbinder unbinder;
    @BindView(R.id.btn_return)
    AppCompatButton btnReturn;
    private List<Score> scoreList;

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventEntity eventEntity) {
        switch (eventEntity.getId()) {

            default:
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_score_show, container, false);
        unbinder = ButterKnife.bind(this, view);
        Bundle bundle = getArguments();
        if (bundle != null) {
            scoreList = bundle.getParcelableArrayList("scoreList");
        }
        if (scoreList != null) {
            //RecyclerView初始化
            LinearLayoutManager layout = new LinearLayoutManager(requireContext());
            //列表再底部开始展示，反转后由上面开始展示
            layout.setStackFromEnd(true);
            //列表翻转
            layout.setReverseLayout(true);
            rv.setLayoutManager(layout);
            rv.setAdapter(new ScoreRecyclerViewAdapter(scoreList));
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @OnClick(R.id.btn_return)
    public void onReturnBtnClicked() {
        EventBus.getDefault().post(new EventEntity(ConstantPool.Int.RETURN_LOGIN_FRAGMENT));
    }
}
