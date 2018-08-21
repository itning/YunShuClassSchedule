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
        WEEK_FONT_SIZE("class_font_size");

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
        HTTP_ERROR(0x001),
        /**
         * 进入主活动
         */
        ENTER_HOME_ACTIVITY(0x002),
        /**
         * 开始检查APP是否有更新
         */
        START_CHECK_APP_UPDATE(0x003),
        /**
         * 外置存储权限请求码
         */
        WRITE_EXTERNAL_STORAGE_REQUEST_CODE(0x004),
        /**
         * 设置权限页面请求码
         */
        APP_SETTING_REQUEST_CODE(0x005),
        /**
         * 开始下载升级文件
         */
        START_DOWNLOAD_UPDATE_APK(0x006),
        /**
         * 安装APK
         */
        INSTALL_APK(0x007),
        /**
         * 退出间隔延迟
         */
        EXIT_DELAY(2000),
        /**
         * 开始检查课程表更新
         */
        START_CHECK_CLASS_SCHEDULE_UPDATE(0x008),
        /**
         * 结束检查课程表更新(数据已更新)
         */
        END_CHECK_CLASS_SCHEDULE_UPDATE(0x009),
        /**
         * 时间改变
         */
        TIME_TICK_CHANGE(0x010),
        /**
         * 登陆加载专业数据
         */
        LOGIN_LOADING_PROFESSION_DATA(0x011),
        /**
         * 登陆加载班级数据
         */
        LOGIN_LOADING_CLASS_DATA(0x012),
        /**
         * 安装APK权限请求码
         */
        INSTALL_PACKAGES_REQUEST_CODE(0x013),
        /**
         * 设置安装位置来源
         */
        APP_INSTALL_UNKNOWN_APK_SETTING(0x014),
        /**
         * 刷新本周课程fragment数据
         */
        REFRESH_WEEK_FRAGMENT_DATA(0x015);

        private int value;

        Int(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }


}
