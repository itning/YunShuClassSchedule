package top.itning.yunshuclassschedule.util.download.progress;

/**
 * @author ljd
 * @date 3/29/16
 */
interface ProgressListener {
    /**
     * 下载时的回调
     *
     * @param progress 已经下载或上传字节数
     * @param total    总字节数
     * @param done     是否完成
     */
    void onProgress(long progress, long total, boolean done);
}
