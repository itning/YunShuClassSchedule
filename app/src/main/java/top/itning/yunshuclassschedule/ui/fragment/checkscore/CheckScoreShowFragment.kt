package top.itning.yunshuclassschedule.ui.fragment.checkscore

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.Unbinder
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.entity.Score
import top.itning.yunshuclassschedule.ui.adapter.ScoreRecyclerViewAdapter

/**
 * 展示
 *
 * @author itning
 */
class CheckScoreShowFragment : Fragment() {
    @BindView(R.id.rv)
    lateinit var rv: RecyclerView
    private lateinit var unBinder: Unbinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
    }

    override fun onDestroy() {
        Log.d(TAG, "on Destroy")
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_check_score_show, container, false)
        unBinder = ButterKnife.bind(this, view)
        arguments?.getParcelableArrayList<Score>("scoreList")?.let {
            //RecyclerView初始化
            val layout = LinearLayoutManager(requireContext())
            //列表再底部开始展示，反转后由上面开始展示
            layout.stackFromEnd = true
            //列表翻转
            layout.reverseLayout = true
            rv.layoutManager = layout
            rv.adapter = ScoreRecyclerViewAdapter(it)
            return view
        } ?: kotlin.run {
            return view
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unBinder.unbind()
    }

    companion object {
        private const val TAG = "CheckScoreShowFragment"
    }
}
