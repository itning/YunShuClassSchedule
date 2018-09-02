package top.itning.yunshuclassschedule.common;

/**
 * 常量池
 *
 * @author itning
 */
public class ConstantPool {

    public enum Str {
        /**
         * 数据库名
         */
        DB_NAME("class-schedule.db"),
        /**
         * sharedPreferences 文件名
         */
        SHARED_PREFERENCES_FILENAME("class-schedule"),
        /**
         * 课程表版本KEY
         */
        APP_CLASS_SCHEDULE_VERSION("APP_CLASS_SCHEDULE_VERSION"),
        /**
         * 第一次进入app
         */
        FIRST_IN_APP("first_in_app"),
        /**
         * 登陆用户名
         */
        USER_USERNAME("user_username"),
        /**
         * 登陆密码
         */
        USER_PASSWORD("user_password"),
        /**
         * 用户所在班级
         */
        USER_CLASS_ID("user_class_id"),
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
        TEACHER_INFO_STATUS("teacher_week_status");

        private String value;

        Str(String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }
    }

    public enum Int {
        /**
         * 延迟进入主活动时间
         */
        DELAY_INTO_MAIN_ACTIVITY_TIME(2000),
        /**
         * 服务器成功状态码
         */
        RESPONSE_SUCCESS_CODE(200),
        /**
         * 下载课程表信息错误
         */
        HTTP_ERROR(111),
        /**
         * 进入主活动
         */
        ENTER_HOME_ACTIVITY(112),
        /**
         * 开始检查APP是否有更新
         */
        START_CHECK_APP_UPDATE(113),
        /**
         * 外置存储权限请求码
         */
        WRITE_EXTERNAL_STORAGE_REQUEST_CODE(114),
        /**
         * 设置权限页面请求码
         */
        APP_SETTING_REQUEST_CODE(115),
        /**
         * 开始下载升级文件
         */
        START_DOWNLOAD_UPDATE_APK(116),
        /**
         * 安装APK
         */
        INSTALL_APK(117),
        /**
         * 退出间隔延迟
         */
        EXIT_DELAY(2000),
        /**
         * 开始检查课程表更新
         */
        START_CHECK_CLASS_SCHEDULE_UPDATE(118),
        /**
         * 结束检查课程表更新(数据已更新)
         */
        END_CHECK_CLASS_SCHEDULE_UPDATE(119),
        /**
         * 时间改变
         */
        TIME_TICK_CHANGE(120),
        /**
         * 登陆加载专业数据
         */
        LOGIN_LOADING_PROFESSION_DATA(121),
        /**
         * 登陆加载班级数据
         */
        LOGIN_LOADING_CLASS_DATA(122),
        /**
         * 安装APK权限请求码
         */
        INSTALL_PACKAGES_REQUEST_CODE(123),
        /**
         * 设置安装位置来源
         */
        APP_INSTALL_UNKNOWN_APK_SETTING(124),
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
         * 本周课程背景变化
         */
        NOTIFICATION_BACKGROUND_CHANGE(131),
        /**
         * 销毁Activity(在重新选择专业时需要销毁MainActivity和SettingActivity)
         */
        DESTROY_ACTIVITY(132);

        private int value;

        Int(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }


}
