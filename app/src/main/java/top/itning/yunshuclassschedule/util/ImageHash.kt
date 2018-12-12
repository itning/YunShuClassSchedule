package top.itning.yunshuclassschedule.util

import android.graphics.Bitmap
import android.graphics.Matrix

/**
 * 图片指纹工具类
 *
 * @author itning
 */
object ImageHash {
    /**
     * 计算指纹
     *
     * @param fromBitmap [Bitmap]
     * @return 指纹数据
     */
    fun calculateFingerPrint(fromBitmap: Bitmap): String {
        val stringBuilder = StringBuilder()
        val width = fromBitmap.width
        val height = fromBitmap.height
        // 设置想要的大小
        val newWidth = 8
        val newHeight = 8
        // 计算缩放比例
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // 取得想要缩放的matrix参数
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        // 得到新的图片
        val newbm = Bitmap.createBitmap(fromBitmap, 0, 0, width, height, matrix, true)
        for (i in 0 until newbm.width) {
            for (j in 0 until newbm.height) {
                val pixel = newbm.getPixel(i, j)
                stringBuilder.append(Math.abs(pixel))
            }
        }
        newbm.recycle()
        fromBitmap.recycle()
        return stringBuilder.toString()
    }
}
