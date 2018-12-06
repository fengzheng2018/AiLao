package com.android.ailao.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;

public class CheckNetwork {

    /**
     * 跳转设置界面设置网络
     */
    public static void settingNetwork(final Activity activity){
        new AlertDialog.Builder(activity)
                .setTitle("网络状态提示")
                .setMessage("当前没有可用的网络，请设置网络！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS),404);
                    }
                })
                .setCancelable(true).create().show();
    }


    /**
     * 检查网络是否可用
     */
    public static boolean isNetworkAvailable(Context context){
        /**
         * 获取网络状态管理器
         */
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager == null){
            return false;
        }else{
            /**
             * 建立网络数组
             */
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            if(networkInfo != null){
                for(NetworkInfo info:networkInfo){
                    if(info.isConnected()){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
