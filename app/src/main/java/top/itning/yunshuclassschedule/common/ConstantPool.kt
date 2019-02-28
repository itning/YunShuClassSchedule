package top.itning.yunshuclassschedule.common

/**
 * 常量池
 *
 * @author itning
 */
class ConstantPool {

    enum class Str(private val value: String) {
        /**
         * 数据库名
         */
        DB_NAME("class-schedule.db"),
        /**
         * sharedPreferences 文件名
         */
        SHARED_PREFERENCES_FILENAME("class-schedule"),
        /**
         * 第一次进入app
         */
        FIRST_IN_APP("first_in_app"),
        /**
         * 周课表文字大小
         */
        WEEK_FONT_SIZE("class_font_size"),
        /**
         * 上一次的日期
         */
        LAST_DATE("last-date"),
        /**
         * 是否显示授课教师信息
         */
        TEACHER_INFO_STATUS("teacher_week_status"),
        /**
         * 下周一 日期
         */
        NEXT_WEEK_OF_MONDAY("this_week_of_sunday"),
        /**
         * 每天课程节数
         */
        CLASS_SECTION("class_section"),
        /**
         * 加入反馈群
         */
        ADD_GROUP_DIALOG_STATE("add_group_dialog_state");

        fun get(): String {
            return value
        }
    }

    enum class Int(private val value: kotlin.Int) {
        /**
         * 延迟进入主活动时间
         */
        DELAY_INTO_MAIN_ACTIVITY_TIME(1000),
        /**
         * 下载课程表信息错误
         */
        HTTP_ERROR(111),
        /**
         * 进入主活动
         */
        ENTER_HOME_ACTIVITY(112),
        /**
         * 退出间隔延迟
         */
        EXIT_DELAY(2000),
        /**
         * 时间改变
         */
        TIME_TICK_CHANGE(120),
        /**
         * 刷新本周课程fragment数据
         */
        REFRESH_WEEK_FRAGMENT_DATA(125),
        /**
         * 应用颜色改变
         */
        APP_COLOR_CHANGE(126),
        /**
         * 上课提醒
         */
        CLASS_UP_REMIND(127),
        /**
         * 下课提醒
         */
        CLASS_DOWN_REMIND(128),
        /**
         * 手机状态:取消静音
         */
        PHONE_MUTE_CANCEL(129),
        /**
         * 手机状态:开启静音
         */
        PHONE_MUTE_OPEN(130),
        /**
         * 课程信息数组更新
         */
        COURSE_INFO_ARRAY_UPDATE(131),
        /**
         * 需要重新初始化课程Fragment
         */
        REFRESH_CLASS_SCHEDULE_FRAGMENT(133),
        /**
         * 获取验证码和Cookie成功
         */
        GET_COOKIE_AND_IMAGE_OK(134),
        /**
         * 获取验证码和Cookie失败
         */
        GET_COOKIE_AND_IMAGE_FAILED(135),
        /**
         * 重新登陆
         */
        RE_LOGIN_SCORE(136),
        /**
         * 登陆时消息
         */
        SCORE_LOGIN_MSG(137),
        /**
         * 成绩查询成功
         */
        SCORE_LOGIN_SUCCESS(138),
        /**
         * 回到登陆页面
         */
        RETURN_LOGIN_FRAGMENT(139),
        /**
         * 课程周数改变(非持久)
         */
        CLASS_WEEK_CHANGE(140);

        fun get(): kotlin.Int {
            return value
        }
    }
}
