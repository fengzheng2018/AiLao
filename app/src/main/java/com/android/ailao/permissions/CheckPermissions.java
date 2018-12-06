package com.android.ailao.permissions;

import android.app.Activity;
import android.content.Context;

import pub.devrel.easypermissions.EasyPermissions;

public class CheckPermissions {

    private Activity mActivity;
    private Context mContext;

    public CheckPermissions(Activity mActivity, Context mContext) {
        this.mActivity = mActivity;
        this.mContext = mContext;
    }

    /**
     * 检查权限
     */
    public boolean checkPermission(String[] permissions){
        return (EasyPermissions.hasPermissions(mContext,permissions));
    }

    /**
     * 请求权限
     */
    public void requestPermissions(String rational, int requestCode, String[] permissions){
        EasyPermissions.requestPermissions(mActivity,rational,requestCode,permissions);
    }
}
