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
import kotlinx.android.synthetic.main.fragment_check_score_show.*
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.entity.Score
import top.itning.yunshuclassschedule.ui.adapter.ScoreRecyclerViewAdapter

/**
 * 展示
 *
 * @author itning
 */
class CheckScoreShowFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_check_score_show, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.getParcelableArrayList<Score>("scoreList")?.let {
            //RecyclerView初始化
            val layout = LinearLayoutManager(requireContext())
            //列表再底部开始展示，反转后由上面开始展示
            layout.stackFromEnd = true
            //列表翻转
            layout.reverseLayout = true
            rv.layoutManager = layout
            rv.adapter = ScoreRecyclerViewAdapter(it)
        }
    }

    companion object {
        private const val TAG = "CheckScoreShowFragment"
    }
}
