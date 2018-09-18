package top.itning.yunshuclassschedule.util;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * 图片指纹工具类
 *
 * @author itning
 */
public class ImageHash {
    /**
     * 计算指纹
     *
     * @param fromBitmap {@link Bitmap}
     * @return 指纹数据
     */
    public static String calculateFingerPrint(Bitmap fromBitmap) {
        StringBuilder stringBuilder = new StringBuilder();
        int width = fromBitmap.getWidth();
        int height = fromBitmap.getHeight();
        // 设置想要的大小
        int newWidth = 8;
        int newHeight = 8;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(fromBitmap, 0, 0, width, height, matrix, true);
        for (int i = 0; i < newbm.getWidth(); i++) {
            for (int j = 0; j < newbm.getHeight(); j++) {
                int pixel = newbm.getPixel(i, j);
                stringBuilder.append(Math.abs(pixel));
            }
        }
        newbm.recycle();
        fromBitmap.recycle();
        return stringBuilder.toString();
    }
}
