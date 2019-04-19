package top.itning.yunshuclassschedule.ui.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import top.itning.yunshuclassschedule.R
import top.itning.yunshuclassschedule.entity.Score

/**
 * @author itning
 */
class ScoreRecyclerViewAdapter(@param:NonNull private val scoreList: List<Score>) : RecyclerView.Adapter<ScoreRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_score_rv, parent, false))
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
        val tvId: AppCompatTextView = itemView.findViewById(R.id.tv_id)

        val tvName: AppCompatTextView = itemView.findViewById(R.id.tv_name)

        val tvSemester: AppCompatTextView = itemView.findViewById(R.id.tv_semester)

        val tvGrade: AppCompatTextView = itemView.findViewById(R.id.tv_grade)

        val tvCredit: AppCompatTextView = itemView.findViewById(R.id.tv_credit)
    }
}
