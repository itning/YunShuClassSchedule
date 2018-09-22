package top.itning.yunshuclassschedule.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import top.itning.yunshuclassschedule.R;
import top.itning.yunshuclassschedule.entity.Score;

/**
 * @author itning
 */
public class ScoreRecyclerViewAdapter extends RecyclerView.Adapter {
    private final List<Score> scoreList;

    public ScoreRecyclerViewAdapter(@NonNull List<Score> scoreList) {
        this.scoreList = scoreList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ScoreRecyclerViewAdapter.ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score_rv, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Score score = scoreList.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.tvId.setText(score.getId());
        viewHolder.tvName.setText(score.getName());
        viewHolder.tvSemester.setText(score.getSemester());
        viewHolder.tvGrade.setText(score.getGrade());
        viewHolder.tvCredit.setText(score.getCredit());
    }

    @Override
    public int getItemCount() {
        return scoreList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_id)
        AppCompatTextView tvId;
        @BindView(R.id.tv_name)
        AppCompatTextView tvName;
        @BindView(R.id.tv_semester)
        AppCompatTextView tvSemester;
        @BindView(R.id.tv_grade)
        AppCompatTextView tvGrade;
        @BindView(R.id.tv_credit)
        AppCompatTextView tvCredit;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
