package top.itning.yunshuclassschedule.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.Nullable
import top.itning.yunshuclassschedule.R

/**
 * @author itning
 */
class RoundBackChange : View {
    private var color = -0x22000001
    private val mPaint = Paint()

    constructor(context: Context) : super(context, null)

    constructor(context: Context, @Nullable attrs: AttributeSet) : super(context, attrs) {
        //设置画笔宽度为10px
        val array = context.obtainStyledAttributes(attrs, R.styleable.RoundBackChange)
        color = array.getColor(R.styleable.RoundBackChange_self_color, color)
        array.recycle()
        //设置画笔颜色
        mPaint.color = color
        //设置画笔模式为填充
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.strokeWidth = 10f
        mPaint.isAntiAlias = true
    }

    fun setBackColor(color: Int) {
        this.color = color
        mPaint.color = color
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.TRANSPARENT)
        canvas.drawCircle((right - left - measuredWidth / 2).toFloat(), (top + measuredHeight / 2).toFloat(), (measuredWidth / 3).toFloat(), mPaint)
    }

}
