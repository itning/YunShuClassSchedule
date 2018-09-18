package top.itning.yunshuclassschedule.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 分数实体
 *
 * @author itning
 */
public class Score implements Parcelable {
    /**
     * 序号
     */
    private String id;
    /**
     * 学期
     */
    private String semester;
    /**
     * 科目名称
     */
    private String name;
    /**
     * 成绩
     */
    private String grade;
    /**
     * 学分
     */
    private String credit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id='" + id + '\'' +
                ", semester='" + semester + '\'' +
                ", name='" + name + '\'' +
                ", grade='" + grade + '\'' +
                ", credit='" + credit + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(semester);
        dest.writeString(name);
        dest.writeString(grade);
        dest.writeString(credit);
    }

    public static final Creator<Score> CREATOR = new Creator<Score>() {
        @Override
        public Score createFromParcel(Parcel in) {
            Score score = new Score();
            score.id = in.readString();
            score.semester = in.readString();
            score.name = in.readString();
            score.grade = in.readString();
            score.credit = in.readString();
            return score;
        }

        @Override
        public Score[] newArray(int size) {
            return new Score[size];
        }
    };
}
