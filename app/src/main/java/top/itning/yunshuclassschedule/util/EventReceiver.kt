package top.itning.yunshuclassschedule.util

/**
 * 事件接收
 *
 * @author itning
 */
interface EventReceiver {
    /**
     * 触发事件
     *
     * @return 已消费返回真
     */
    fun eventTrigger(): Boolean
}
