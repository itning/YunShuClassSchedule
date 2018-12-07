package top.itning.yunshuclassschedule.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import top.itning.yunshuclassschedule.R;

/**
 * @author itning
 */
public class RoundBackChange extends View {
    private int color = 0xddffffff;
    private final Paint mPaint = new Paint();

    public RoundBackChange(Context context) {
        super(context, null);
    }

    public RoundBackChange(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //设置画笔宽度为10px
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoundBackChange);
        color = array.getColor(R.styleable.RoundBackChange_self_color, color);
        array.recycle();
        //设置画笔颜色
        mPaint.setColor(color);
        //设置画笔模式为填充
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(10f);
        mPaint.setAntiAlias(true);


    }

    public void setBackColor(int color) {
        this.color = color;
        mPaint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawCircle(getRight() - getLeft() - getMeasuredWidth() / 2, getTop() + getMeasuredHeight() / 2, getMeasuredWidth() / 3, mPaint);
    }

}
