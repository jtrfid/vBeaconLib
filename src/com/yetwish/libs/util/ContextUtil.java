package com.yetwish.libs.util;

import android.app.Application;
import android.content.Context;

/**
 * 提供全局application Context 对象 方便工具类调用APPContext
 * Created by yetwish on 2015-03-31
 */

public class ContextUtil extends Application{

    private static Context context;

    @Override
    public void onCreate(){
        context = getApplicationContext();
    }

    public static Context getGlobalApplicationContext(){
        return context;
    }

}
