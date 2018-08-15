package top.itning.yunshuclassschedule;

public class ConstantPool {

    public enum Str {
        /**
         * 数据库名
         */
        DB_NAME("class-schedule.db"),
        /**
         * 课程表版本KEY
         */
        APP_CLASS_SCHEDULE_VERSION("APP_CLASS_SCHEDULE_VERSION");

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
        DOWNLOAD_CLASS_SCHEDULE_ERROR(0x001),
        /**
         * 进入主活动
         */
        ENTER_HOME_ACTIVITY(0x002);

        private int value;

        Int(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }


}
