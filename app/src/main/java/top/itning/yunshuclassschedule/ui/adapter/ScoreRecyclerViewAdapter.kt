package top.itning.yunshuclassschedule.ui.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.entity.Score

/**
 * @author itning
 */
class ScoreRecyclerViewAdapter(@param:NonNull private val scoreList: List<Score>) : RecyclerView.Adapter<ScoreRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ScoreRecyclerViewAdapter.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_score_rv, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val score = scoreList[position]
        holder.tvId.text = score.id
        holder.tvName.text = score.name
        holder.tvSemester.text = score.semester
        holder.tvGrade.text = score.grade
        holder.tvCredit.text = score.credit
    }

    override fun getItemCount(): Int {
        return scoreList.size
    }

    class ViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @BindView(R.id.tv_id)
        lateinit var tvId: AppCompatTextView
        @BindView(R.id.tv_name)
        lateinit var tvName: AppCompatTextView
        @BindView(R.id.tv_semester)
        lateinit var tvSemester: AppCompatTextView
        @BindView(R.id.tv_grade)
        lateinit var tvGrade: AppCompatTextView
        @BindView(R.id.tv_credit)
        lateinit var tvCredit: AppCompatTextView

        init {
            ButterKnife.bind(this, itemView)
        }
    }
}
