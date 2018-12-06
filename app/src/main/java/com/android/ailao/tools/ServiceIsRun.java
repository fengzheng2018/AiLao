package com.android.ailao.tools;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class ServiceIsRun {

    /**
     * 判断服务是否运行
     * @param className 要检查的服务类名
     * @return 服务在运行，返回true
     */
    public static boolean isServiceExisted(String className, Context context){

        boolean isWork = false;

        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningServiceInfo> serviceInfoList = activityManager.getRunningServices(Integer.MAX_VALUE);

        if(serviceInfoList.size() <= 0){
            return false;
        }else {
            for(int i=0; i<serviceInfoList.size(); i++){
                String name = serviceInfoList.get(i).service.getClassName();
                if(name.equals(className)){
                    isWork = true;
                    return isWork;
                }
            }
        }
        return isWork;
    }
}
