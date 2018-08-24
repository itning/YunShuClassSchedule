package top.itning.yunshuclassschedule.util.download.progress;

/**
 * @author ljd
 * @date 4/12/16
 */
public abstract class AbstractProgressHandler {
    /**
     * 下载时回调
     *
     * @param progress 当前进度
     * @param total    总进度
     * @param done     是否完成
     */
    protected abstract void onProgress(long progress, long total, boolean done);
}
