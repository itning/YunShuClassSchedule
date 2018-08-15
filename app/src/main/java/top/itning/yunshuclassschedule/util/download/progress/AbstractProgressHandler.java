package top.itning.yunshuclassschedule.util.download.progress;

/**
 * @author ljd
 * @date 4/12/16
 */
public abstract class AbstractProgressHandler {
    protected abstract void onProgress(long progress, long total, boolean done);
}
