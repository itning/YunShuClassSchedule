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
        FIRST_IN_APP("first_in_app");

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
        EXIT_DELAY(2000);

        private int value;

        Int(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }


}
