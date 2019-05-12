package top.itning.yunshuclassschedule.util

import android.content.Context
import android.location.LocationManager
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import androidx.annotation.NonNull


/**
 * https://www.jianshu.com/p/10ed9ae02775
 *
 * @author alic
 * @author itning
 */
object NetWorkUtils {
    /**
     * 判断是否有网络连接
     *
     * @param context [Context]
     * @return 有返回true
     */
    fun isNetworkConnected(@NonNull context: Context): Boolean {
        // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // 获取NetworkInfo对象
        val networkInfo = manager.activeNetworkInfo
        //判断NetworkInfo对象是否为空
        return networkInfo != null && networkInfo.isAvailable
    }

    /**
     * 判断WIFI网络是否可用
     *
     * @param context [Context]
     * @return 是否可用
     */
    fun isMobileConnected(@NonNull context: Context): Boolean {
        //获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //获取NetworkInfo对象
        val networkInfo = manager.activeNetworkInfo
        //判断NetworkInfo对象是否为空 并且类型是否为MOBILE
        return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isAvailable
    }

    /**
     * 获取当前网络连接的类型信息
     * 原生
     *
     * @param context [Context]
     * @return [ConnectivityManager]
     */
    fun getConnectedType(@NonNull context: Context): Int {
        //获取手机所有连接管理对象
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //获取NetworkInfo对象
        val networkInfo = manager.activeNetworkInfo
        return if (networkInfo != null && networkInfo.isAvailable) {
            //返回NetworkInfo的类型
            networkInfo.type
        } else -1
    }

    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-4：3G网络-3：2G网络-2
     * 自定义
     *
     * @param context [Context]
     * @return 网络状态
     */
    fun getAPNType(@NonNull context: Context): Int {
        //结果返回值
        var netType = 0
        //获取手机所有连接管理对象
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        //获取NetworkInfo对象
        val networkInfo = manager.activeNetworkInfo ?: return netType
        //NetworkInfo对象为空 则代表没有网络
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        val nType = networkInfo.type
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = 1
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            val nSubType = networkInfo.subtype
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            netType = if (nSubType == TelephonyManager.NETWORK_TYPE_LTE && !telephonyManager.isNetworkRoaming) {
                4
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0 && !telephonyManager.isNetworkRoaming) {
                3
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA && !telephonyManager.isNetworkRoaming) {
                2
            } else {
                2
            }
        }
        return netType
    }

    /**
     * 判断GPS是否打开
     * ACCESS_FINE_LOCATION权限
     *
     * @param context [Context]
     * @return 是否打开
     */
    fun isGPSEnabled(@NonNull context: Context): Boolean {
        //获取手机所有连接LOCATION_SERVICE对象
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
// 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        val gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        val network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return gps || network
    }
}
