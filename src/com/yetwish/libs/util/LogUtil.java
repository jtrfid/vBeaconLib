package com.yetwish.libs.util;


import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 日志工具类  
 * Created by yetwish on 2015-04-10
 */

public class LogUtil {
    private static final String TAG = "BeaconLibrary";
    private static boolean ENABLE_DEBUG_LOGGING = false;
    private static boolean ENABLE_FILE_LOGGING = false;
    private static BufferedWriter bw; //fixme 写入文件中有问题
    private final static String PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Log/";
    private final static String FILE_NAME = "Log.txt";
    private static int logTimes = 0;

    public static void enableDebugLogging(boolean enableDebugLogging) {
        ENABLE_DEBUG_LOGGING = enableDebugLogging;
    }

    /**
     * 设置是否允许将日志写入文件中
     * preCondition: enableDebugLogging(true)
     * @param enableFileLogging,boolean 
     */
    public static void enableFileLogging(boolean enableFileLogging) {
        ENABLE_FILE_LOGGING = enableFileLogging;
        if (ENABLE_FILE_LOGGING) {
//            if (bw == null)
//                try {
//                    File file = new File(PATH+FILE_NAME);
//                    file.mkdir();
//                    bw = new BufferedWriter(new FileWriter(file));
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    Log.w(TAG, "write error" + (bw == null));
//                }
        } else {
            if (bw != null)
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }



    public static void v(String msg) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.v(TAG, logMsg);
            if (ENABLE_FILE_LOGGING) {
                logoutOnFile(logMsg);
            }
        }
    }

    public static void t(String msg){
        if (ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "times is "+ (++logTimes)+msg);
        }
    }

    public static void d(String msg) {
        if (ENABLE_DEBUG_LOGGING) {
            String logMsg = debugInfo() + msg;
            Log.d(TAG, logMsg);
            if (ENABLE_FILE_LOGGING) {
                logoutOnFile(logMsg);
            }
        }
    }

    public static void i(String msg) {
        String logMsg = debugInfo() + msg;
        Log.i(TAG, logMsg);
        if (ENABLE_FILE_LOGGING) {
            logoutOnFile(logMsg);
        }
    }

    public static void w(String msg) {
        String logMsg = debugInfo() + msg;
        Log.w(TAG, logMsg);
        if (ENABLE_FILE_LOGGING) {
            logoutOnFile(logMsg);
        }
    }

    public static void e(String msg) {
        String logMsg = debugInfo() + msg;
        Log.e(TAG, logMsg);
        if (ENABLE_FILE_LOGGING) {
            logoutOnFile(msg);
        }
    }

    public static void e(String msg, Throwable e) {
        String logMsg = debugInfo() + msg;
        Log.e(TAG, logMsg, e);
        if (ENABLE_FILE_LOGGING) {
            logoutOnFile(msg + " " + throwableAsString(e));
        }
    }

    public static void wtf(String msg) {
        String logMsg = debugInfo() + msg;
        Log.wtf(TAG, logMsg);
        if (ENABLE_FILE_LOGGING) {
            logoutOnFile(logMsg);
        }
    }

    public static void wtf(String msg, Exception exception) {
        String logMsg = debugInfo() + msg;
        Log.wtf(TAG, logMsg, exception);
        if (ENABLE_FILE_LOGGING) {
            logoutOnFile(logMsg + " " + throwableAsString(exception));
        }
    }

    private static String debugInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String className = stackTrace[4].getClassName();
        String methodName = Thread.currentThread().getStackTrace()[4].getMethodName();
        int lineNumber = stackTrace[4].getLineNumber();
        return className + "." + methodName + ":" + lineNumber + " ";
    }

    private static String throwableAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private static void logoutOnFile(String msg) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(PATH+FILE_NAME,true));
            bw.write(msg);
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(bw != null){
                try {
                    bw.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}
