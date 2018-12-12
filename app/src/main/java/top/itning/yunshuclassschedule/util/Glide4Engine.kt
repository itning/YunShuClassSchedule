package top.itning.yunshuclassschedule.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView

import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.zhihu.matisse.engine.ImageEngine

/**
 * https://github.com/zhihu/Matisse/blob/master/sample/src/main/java/com/zhihu/matisse/sample/Glide4Engine.java
 * [ImageEngine] implementation using Glide.
 *
 * @author zhihu
 */

class Glide4Engine : ImageEngine {

    override fun loadThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView, uri: Uri) {
        GlideApp.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView)
    }

    override fun loadGifThumbnail(context: Context, resize: Int, placeholder: Drawable, imageView: ImageView,
                                  uri: Uri) {
        GlideApp.with(context)
                .asBitmap() // some .jpeg files are actually gif
                .load(uri)
                .apply(RequestOptions()
                        .override(resize, resize)
                        .placeholder(placeholder)
                        .centerCrop())
                .into(imageView)
    }

    override fun loadImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        GlideApp.with(context)
                .load(uri)
                .apply(RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView)
    }

    override fun loadGifImage(context: Context, resizeX: Int, resizeY: Int, imageView: ImageView, uri: Uri) {
        GlideApp.with(context)
                .asGif()
                .load(uri)
                .apply(RequestOptions()
                        .override(resizeX, resizeY)
                        .priority(Priority.HIGH)
                        .fitCenter())
                .into(imageView)
    }

    override fun supportAnimatedGif(): Boolean {
        return true
    }
}