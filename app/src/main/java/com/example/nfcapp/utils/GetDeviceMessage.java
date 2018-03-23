package com.example.nfcapp.utils;

import android.content.Context;
import android.provider.Settings;

/**
 * Created by 29083 on 2018/3/18.
 */

public class GetDeviceMessage {

    public static String getDeviceTD(Context context) {
        String android_id = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        return android_id;
    }


    public static String getDeviceSerialNumber(Context context) {
        String serialNumber = android.os.Build.SERIAL;
        return serialNumber;
    }

    public static String getDeviceIdAndSerialNumber(Context context) {
        return getDeviceTD(context)  + getDeviceSerialNumber(context);
    }


}
